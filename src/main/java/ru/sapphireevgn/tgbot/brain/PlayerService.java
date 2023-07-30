package ru.sapphireevgn.tgbot.brain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.sapphireevgn.tgbot.dto.Player;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PlayerService {
    private Map<Long, Player> players = new HashMap<>();
    private Player unknownPlayer = new Player(0L) {{
        this.setUserName("");
    }};

    public Player find(User user) {
        Player player = unknownPlayer;
        if (user != null) {
            player = players.get(user.getId());
            if (player == null)
                player = addPlayer(user);
        }
        return player;
    }

    private Player addPlayer(User user) {
        Long playerId = user.getId();
        log.warn("player with empty id {}", user);
        Player player = new Player(playerId);
        player.setFirstName(user.getFirstName());
        player.setUserName(user.getUserName());
        players.putIfAbsent(playerId, player);
        return player;
    }

    public Player getUnknownPlayer() {
        return unknownPlayer;
    }
}
