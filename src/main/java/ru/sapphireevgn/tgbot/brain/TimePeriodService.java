package ru.sapphireevgn.tgbot.brain;

import lombok.extern.slf4j.Slf4j;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.springframework.stereotype.Service;
import ru.sapphireevgn.tgbot.dto.TimePeriod;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.*;

import static java.time.DayOfWeek.*;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Service
public class TimePeriodService {
    private final List<TimePeriod> allPeriods = new ArrayList<>();
    private final Map<DayOfWeek, List<TimePeriod>> locked = new HashMap<>();
    {
        for (short i = 16; i < 48; i++) {
            allPeriods.add(TimePeriod.fromId(i));
        }
        // Время зарезервированное под фитнес
        //11:30-14:00 пн вт ср чт пт
        //17:00-18:00 пт
        //17:00-19:00 пн чт
        //18:00-19:30 вт ср
        java.io.File fileToParse = new java.io.File("PingPongBot.ini");
        Ini ini = null;
        try {
            ini = new Ini(fileToParse);
        } catch (IOException e) {
            log.error("Cant read ini file.", e);
        }
        if (ini != null) {
            Map<String, Profile.Section> collect = ini.entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
            Profile.Section lockedFromIni = collect.get("LOCKED");
            collectLoadLockedPeriodByDay(lockedFromIni, MONDAY);
            collectLoadLockedPeriodByDay(lockedFromIni, TUESDAY);
            collectLoadLockedPeriodByDay(lockedFromIni, WEDNESDAY);
            collectLoadLockedPeriodByDay(lockedFromIni, THURSDAY);
            collectLoadLockedPeriodByDay(lockedFromIni, FRIDAY);
            collectLoadLockedPeriodByDay(lockedFromIni, SATURDAY);
            collectLoadLockedPeriodByDay(lockedFromIni, SUNDAY);
        }
    }

    private void collectLoadLockedPeriodByDay(Profile.Section fromIni, DayOfWeek day) {
        ArrayList<TimePeriod> listLockedPeriod = new ArrayList<>();
        locked.put(day, listLockedPeriod);
        String dayBlock = fromIni.get(day.toString());
        if (dayBlock == null)
            return;
        String[] split = dayBlock.split(",");
        for (String asStringPeriod : split) {
            TimePeriod timePeriod = getPeriodFromString(asStringPeriod);
            listLockedPeriod.add(timePeriod);
        }
    }

    public List<TimePeriod> getAllPeriods() {
        return allPeriods;
    }

    public List<TimePeriod> getLockedPeriods(DayOfWeek day) {
        return locked.get(day);
    }

    public TimePeriod getPeriodFromString(String asString) {
        if (asString == null) {
            throw new RuntimeException("Дай не null строку чтобы найти период!");
        }
        if (asString.isBlank()) {
            throw new RuntimeException("Дай не пустую строку чтобы найти период!");
        }
        TimePeriod finded = allPeriods.stream()
                .filter(timePeriod -> asString.equals(timePeriod.getTime()))
                .findFirst()
                .orElse(null);
        if (finded == null) {
            throw new RuntimeException("Не нашел период для строки {}!"+asString);
        }
        return finded;
    }

    /**
     * Схлопывает рядом стоящие периоды, индекс дает наименьшего, текстовой представление от наименьшего до максимального
     * @param periods
     * @return
     */
    public static List<TimePeriod> zipPeriods(List<TimePeriod> periods) {
        ArrayList<TimePeriod> zipped = new ArrayList<>();
        TimePeriod min = null;
        TimePeriod max = null;
        for (int i=0; i<periods.size(); i++) {
            if (min == null && max == null) {
                min = periods.get(i);
                max = periods.get(i);
            } else if (max.getId()+1 == periods.get(i).getId()) {
                max = periods.get(i);
            } else {
                zipped.add(buildFromMin2Max(min, max));
                min = periods.get(i);
                max = periods.get(i);
            }
        }
        if (min != null && max != null) {
            zipped.add(buildFromMin2Max(min, max));
        }
        return zipped;
    }

    private static TimePeriod buildFromMin2Max(TimePeriod min, TimePeriod max) {
        TimePeriod mid = TimePeriod.fromId(min.getId());
        String begin = min.getTime().split("-")[0];
        String end = max.getTime().split("-")[1];
        mid.setTime(begin+"-"+end);
        return mid;
    }
}
