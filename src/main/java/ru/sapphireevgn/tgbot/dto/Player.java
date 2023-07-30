package ru.sapphireevgn.tgbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Player {
    private long id;
    private String firstName;
    private String userName;

    public Player(long id) {
        this.id = id;
    }

    public String printableName() {
        return firstName != null
                ? firstName
                : userName != null
                   ? userName
                   : String.valueOf(id);
    }
}
