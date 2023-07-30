package ru.sapphireevgn.tgbot.brain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.sapphireevgn.tgbot.dto.Command;
import ru.sapphireevgn.tgbot.dto.GameCommand;
import ru.sapphireevgn.tgbot.dto.TimePeriod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class LanguageProcessor {
    private final Pattern patternAdd = Pattern.compile("^\\+\\s*(\\d*:\\d\\d-\\d*:\\d\\d)\\s*$");
    private final Pattern patternRemove = Pattern.compile("^-\\s*(\\d*:\\d\\d-\\d*:\\d\\d)*\\s*$");
    private final Pattern patternLock = Pattern.compile("^#\\s*(\\d*:\\d\\d-\\d*:\\d\\d)\\s*$");
    private final Pattern patternUnlock = Pattern.compile("^\\*\\s*(\\d*:\\d\\d-\\d*:\\d\\d)\\s*$");

    @Autowired
    private TimePeriodService timePeriodService;

    public @NonNull GameCommand parse(String message, Long chatId) {
        GameCommand gCommand = new GameCommand(Command.UNKNOWN, chatId);
        Matcher matcherAdd = patternAdd.matcher(message);
        Matcher matcherRemove = patternRemove.matcher(message);
        Matcher matcherLock = patternLock.matcher(message);
        Matcher matcherUnlock = patternUnlock.matcher(message);
        if (matcherAdd.find()) {
            gCommand.setCommand(Command.ADD);
            String timePeriodAsString = matcherAdd.group(1);
            TimePeriod timePeriod = timePeriodService.getPeriodFromString(timePeriodAsString);
            gCommand.setIdPeriod(timePeriod.getId());
        } else if (matcherRemove.find()) {
            gCommand.setCommand(Command.REMOVE);
            String timePeriodAsString = matcherRemove.group(1);
            if (timePeriodAsString != null) {
                TimePeriod timePeriod = timePeriodService.getPeriodFromString(timePeriodAsString);
                gCommand.setIdPeriod(timePeriod.getId());
            } else {
                gCommand.setIdPeriod((short) -1);
            }
        } else if ("=".equals(message))
            gCommand.setCommand(Command.TOTAL);
        else  if (matcherLock.find()) {
            gCommand.setCommand(Command.LOCK);
            String timePeriodAsString = matcherLock.group(1);
            TimePeriod timePeriod = timePeriodService.getPeriodFromString(timePeriodAsString);
            gCommand.setIdPeriod(timePeriod.getId());
        } else if (matcherUnlock.find()) {
            gCommand.setCommand(Command.UNLOCK);
            String timePeriodAsString = matcherUnlock.group(1);
            TimePeriod timePeriod = timePeriodService.getPeriodFromString(timePeriodAsString);
            gCommand.setIdPeriod(timePeriod.getId());
        }
        return gCommand;
    }
}
