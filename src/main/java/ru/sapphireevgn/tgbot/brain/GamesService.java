package ru.sapphireevgn.tgbot.brain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.sapphireevgn.tgbot.configuration.BotConfig;
import ru.sapphireevgn.tgbot.dto.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GamesService {
    private String ERROR_NOGAME = "Сначала создайте игру вызвав бота ";

    @Autowired
    private BotConfig botConfig;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private TimePeriodService timePeriodService;



    private Map<Long, Game> games = new HashMap<>();

    public Game findGame(long id) {
        Game game = games.get(id);
        LocalDate now = LocalDate.now();
        if (game == null) {
            game = addGame(id, null);
        } else if (!now.isEqual(game.getTime())) {
            log.warn("Игровой день "+now.toString()+"  окончен.");
            games.remove(id);
            game = addGame(id, null);
        }
        return game;
    }

    private Game addGame(long id, String name) {
        log.info("Создаю новую игру для "+id);
        Game game = new Game(id, timePeriodService.getLockedPeriods(LocalDate.now().getDayOfWeek() ));
        game.setName(name);
        games.put(id, game);
        return game;
    }

    public @Nullable String doCommand(GameCommand gCommand, Player player) {
        String response = null;
        switch (gCommand.getCommand()){
            case UNKNOWN -> {
                log.debug("Нету команды");
            }
            case ADD -> {
                log.info("add player {}", player.printableName());
                Game game = findGame(gCommand.getChatId());
                response = addPlayerForGame(game, player, gCommand.getIdPeriod());
            }
            case REMOVE -> {
                log.info("delete player {}", player.printableName());
                Game game = findGame(gCommand.getChatId());
                response = removePlayerFromGame(game, player, gCommand.getIdPeriod());
            }
            case TOTAL -> {
                Game game = findGame(gCommand.getChatId());
                response = game != null ? game.total(null) : getErrorNoGame();
            }
            case LOCK -> {
                Game game = findGame(gCommand.getChatId());
            }
            case UNLOCK -> {
                Game game = findGame(gCommand.getChatId());
            }
        }
        return response;
    }

    private String addPlayerForGame(Game game, Player player, short idPeriod) {
        Slot slot = null;
        if (game == null)
            return getErrorNoGame();
        if (isAvailableSlot(game.getSlots(), idPeriod)) {
            slot = new Slot(player, idPeriod);
            game.getSlots().add(slot);
            log.info("Добавил игрока слот {} на игру {}", slot, game.getDescription());
        } else {
            log.info("Не могу пока добавить игрока {} в занятый слот", player.printableName());
        }
        return game.total(slot);
    }

    private boolean isAvailableSlot(List<Slot> slots, short idPeriod) {
        boolean available = true;
        for (Slot slot : slots) {
            if (slot.getTimePeriod().getId() == idPeriod) {
                available = false;
                log.info("Слот "+slot.getTimePeriod().getTime()+" занят!");
                break;
            }
        }
        return available;
    }

    private String removePlayerFromGame(Game game, Player player, short idGame) {
        if (game == null)
            return ERROR_NOGAME;
        List<Slot> slots4Remove = game.getSlots().stream()
                .filter(slot -> slot.getPlayer().getId() == player.getId())
                .filter(slot -> idGame == -1 || idGame == slot.getTimePeriod().getId())
                .toList();
        if (slots4Remove.size() == 0) {
            log.info("cant find slot of player {} fo remove", player.printableName());
        } else if (slots4Remove.size() == 1) {
            Slot slot = slots4Remove.get(0);
            game.getSlots().remove(slot);
            log.info("Удалил слот {} для игры {}", slot, game.getDescription());
        }else {
            slots4Remove.forEach(slot -> removePlayerFromGame(game, player, slot.getTimePeriod().getId()));
        }
        return game.total(null);
    }

    private String getErrorNoGame() {
        return ERROR_NOGAME + "@" + botConfig.getBotName();
    }

    public List<TimePeriod> getAvailablePeriods(long chatId) {
        Game game = findGame(chatId);
        List<TimePeriod> lockedPeriods = timePeriodService.getLockedPeriods(game.getTime().getDayOfWeek());
        return timePeriodService.getAllPeriods().stream().filter(period -> {
            boolean notExist = true;
            for (Slot slot : game.getSlots()) {
                if (period.getId() == slot.getTimePeriod().getId()) {
                    notExist = false;
                    break;
                }
            }
            for (TimePeriod lockedPeriod : lockedPeriods) {
                if (lockedPeriod.getId() == period.getId()) {
                    notExist = false;
                    break;
                }
            }
            return notExist;
        }).toList();
    }

    public List<TimePeriod> getTakenPeriods(long idChat, User user) {
        Game game = findGame(idChat);
        return game.getSlots().stream()
                .filter(slot -> slot.getPlayer().getId() == user.getId())
                .map(Slot::getTimePeriod)
                .toList();
    }
}
