package ru.sapphireevgn.tgbot.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class GameCommand {
    private Long chatId;
    private Command command;
    private short idPeriod;

    public GameCommand(Command command, Long chatId) {
        this.command = command;
        this.chatId = chatId;
        idPeriod = -1;
    }

    public void setIdPeriod(short idPeriod) {
        this.idPeriod = idPeriod;
    }
}
