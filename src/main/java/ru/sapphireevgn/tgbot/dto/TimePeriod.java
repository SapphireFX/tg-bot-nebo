package ru.sapphireevgn.tgbot.dto;

import lombok.Data;

/**
 * Квант времени в сутках по 30 минут
 */
@Data
public class TimePeriod {
    private short id; // id для сравнения и упорядочивания
    private String time; // человекочитаемое описание кванта типа 8:00-8:30

    private TimePeriod(short id, String time) {
        this.id = id;
        this.time = time;
    }

    public static TimePeriod fromId(short id) {
        if (id < 0 || id > 47)
            throw new RuntimeException("id не может быть меньше 0 и больше 47. Используйте от 0 до 47.");
        int hour = id / 2;
        boolean isStartHalfHour = id % 2 == 1;
        String time = isStartHalfHour
                ? hour+":30-"+(hour+1)+":00"
                : hour+":00-"+hour+":30";
        return new TimePeriod(id, time);
    }
}
