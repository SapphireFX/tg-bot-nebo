package ru.sapphireevgn.tgbot.brain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.sapphireevgn.tgbot.dto.*;

import java.util.List;

import static ru.sapphireevgn.tgbot.dto.Action.NOTHING;
import static ru.sapphireevgn.tgbot.dto.Action.SAY;

@Slf4j
@Service
public class Judge {

    @Autowired
    private LanguageProcessor languageProcessor;

    @Autowired
    private GamesService gamesService;

    @Autowired
    private PlayerService playerService;

    public Reaction processMessage(long chatId, User user, String message) {
        Reaction reaction = new Reaction(NOTHING);
        GameCommand gCommand = languageProcessor.parse(message, chatId);
        Player player = playerService.find(user);
        String done = gamesService.doCommand(gCommand, player);
        if (done != null) {
            reaction.setAction(SAY);
            reaction.setMessage(done);
            reaction.setChatId(chatId);
        }
        log.info(done);
        return reaction;
    }

    public List<TimePeriod> getAvailablePeriods(long idChat) {
        return gamesService.getAvailablePeriods(idChat);
    }

    public List<TimePeriod> getTakenPeriods(long idChat, User user) {
        return gamesService.getTakenPeriods(idChat, user);
    }
}
