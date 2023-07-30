package ru.sapphireevgn.tgbot.dto;

import lombok.Data;

@Data
public class Slot {
    private TimePeriod timePeriod;
    private Player player;

    public Slot(Player player, short idPeriod) {
        this.player = player;
        this.timePeriod = TimePeriod.fromId(idPeriod);
    }

    @Override
    public String toString() {
        return "Slot{" +
                "timePeriod=" + timePeriod +
                ", player=" + player.printableName() +
                '}';
    }
}
