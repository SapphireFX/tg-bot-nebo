package ru.sapphireevgn.tgbot.dto;

import lombok.Data;

@Data
public class Reaction {
    private Action action;
    private String message;
    private Player player;
    private long chatId;

    public Reaction(Action action) {
        this.action = action;
    }
}
