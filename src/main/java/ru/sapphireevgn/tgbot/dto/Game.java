package ru.sapphireevgn.tgbot.dto;

import lombok.Data;
import ru.sapphireevgn.tgbot.brain.TimePeriodService;

import java.time.LocalDate;
import java.util.*;

@Data
public class Game {
    private long id;
    private String name;
    private LocalDate time;
    private String place;
    private List<Slot> slots;
    private List<Slot> lockedPeriods;

    private static Comparator<Slot> slotComparator = (o1, o2) -> {
        if (o1 == null && o2 == null)
            return 0;
        if (o1 != null && o2 != null
                && o1.getTimePeriod() == null && o2.getTimePeriod() == null)
            return 0;
        if(o1 == null || o1.getTimePeriod() == null)
            return -1;
        if(o2 == null || o2.getTimePeriod() == null)
            return 1;
        return Short.compare(o1.getTimePeriod().getId(), o2.getTimePeriod().getId());
    };

    public Game(long id, List<TimePeriod> locked) {
        this.id = id;
        time = LocalDate.now();
        this.slots = new ArrayList<>();
        this.lockedPeriods = new ArrayList<>();
        if (locked != null) {
            Player stubPlayer = new Player(0L);
            stubPlayer.setFirstName("не доступно");
            List<TimePeriod> zipped = TimePeriodService.zipPeriods(locked);
            for (TimePeriod timePeriod : zipped) {
                Slot lockedSlot = new Slot(stubPlayer, timePeriod.getId());
                lockedSlot.setTimePeriod(timePeriod);
                lockedPeriods.add(lockedSlot);
            }
        }
    }

    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append(Objects.requireNonNullElse(name, ""));
        desc.append(time);
        return desc.toString();
    }

    /**
     * Итог по игре с подсветкой определенного слота
     * @param highLightedSlot слот для выделения (подсвечивания)
     * @return
     */
    public String total(Slot highLightedSlot) {
        StringBuilder slotString = new StringBuilder();
        slots.sort(slotComparator);
        lockedPeriods.sort(slotComparator);
        List<Slot> result = new ArrayList<>();
        result.addAll(slots);
        result.addAll(lockedPeriods);
        result.sort(slotComparator);
        for (Slot slot : result) {
            slotString.append("\n");
            if (highLightedSlot != null && highLightedSlot.getTimePeriod().getId() == slot.getTimePeriod().getId())
                slotString.append("*\\+");
            slotString.append(slot.getTimePeriod().getTime());
            slotString.append(": ");
            slotString.append(slot.getPlayer().printableName());
            if (highLightedSlot != null && highLightedSlot.getTimePeriod().getId() == slot.getTimePeriod().getId())
                slotString.append("*");
            id++;
        }
        String rawResult = "Игра " + getDescription() + slotString.toString();
        return rawResult.replace("-", "\\-");
    }
}
