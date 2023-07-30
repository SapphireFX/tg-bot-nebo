package ru.sapphireevgn.tgbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sapphireevgn.tgbot.brain.Judge;
import ru.sapphireevgn.tgbot.configuration.BotConfig;
import ru.sapphireevgn.tgbot.dto.Reaction;
import ru.sapphireevgn.tgbot.dto.TimePeriod;

import java.util.ArrayList;
import java.util.Collection;

import static ru.sapphireevgn.tgbot.dto.Action.NOTHING;
import static ru.sapphireevgn.tgbot.dto.Action.SAY;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {
//    private static final long CHAT_ID = -1001696952328L; // мой чат для тестирования
    private static final long CHAT_ID = -1001970277739L; // чат НебоПлаза

    @Autowired
    private Judge judge;

    final BotConfig botConfig;

    public Bot(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("OnUpdateReceived: {}", update.toString());
        if (update.hasInlineQuery()) {
            if ("+".equals(update.getInlineQuery().getQuery()) || "".equals(update.getInlineQuery().getQuery())) {
                msgWithInlineKbdAvailable(update.getInlineQuery());
            } else if ("-".equals(update.getInlineQuery().getQuery())) {
                msgWithInlineKbdTaken(update.getInlineQuery());
            }
        } else if(update.hasCallbackQuery()) {
            log.info(update.getCallbackQuery().getData());
        } else if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
//            log.info("chatId {}", chatId);
            User user = update.getMessage().getFrom();
//            log.info("playerId {}", user);
            Integer msgId = update.getMessage().getMessageId();
            boolean isEditable = isEditableMessage(update.getMessage());

            Reaction reaction = judge.processMessage(chatId, user, messageText);
            react(reaction, msgId, isEditable);
        }
    }

    private boolean isEditableMessage(Message message) {
        User bot = message.getViaBot();
        return bot != null && botConfig.getBotName().equals(bot.getUserName());
    }

    private void msgWithInlineKbdAvailable(InlineQuery inlineQuery) {
        log.info(inlineQuery.toString());
        AnswerInlineQuery message = new AnswerInlineQuery();
        message.setInlineQueryId(inlineQuery.getId());
        ArrayList<InlineQueryResult> inlineQueryResults = new ArrayList<>();
        message.setResults(inlineQueryResults);
        inlineQueryResults.addAll(generatePeriodButtons());
        message.setCacheTime(0);
        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private Collection<? extends InlineQueryResult> generatePeriodButtons() {
        ArrayList<InlineQueryResult> queryResults = new ArrayList<>();
        for (TimePeriod period : judge.getAvailablePeriods(CHAT_ID)) {
            InlineQueryResultArticle article = new InlineQueryResultArticle("id" + period.getId(), period.getTime(), new InputTextMessageContent("+ " + period.getTime()));
            article.setThumbUrl("https://img.icons8.com/color/48/present.png");
            article.setThumbHeight(100);
            article.setThumbWidth(100);
            queryResults.add(article);
        }
        return queryResults;
    }

    private void msgWithInlineKbdTaken(InlineQuery inlineQuery) {
        log.info(inlineQuery.toString());
        AnswerInlineQuery message = new AnswerInlineQuery();
        message.setInlineQueryId(inlineQuery.getId());
        ArrayList<InlineQueryResult> inlineQueryResults = new ArrayList<>();
        message.setResults(inlineQueryResults);
        inlineQueryResults.addAll(generateTakenPeriodButtons(inlineQuery.getFrom()));
        message.setCacheTime(0);
        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private Collection<? extends InlineQueryResult> generateTakenPeriodButtons(User user) {
        ArrayList<InlineQueryResult> queryResults = new ArrayList<>();
        for (TimePeriod period : judge.getTakenPeriods(CHAT_ID,  user)) {
            InlineQueryResultArticle article = new InlineQueryResultArticle("id" + period.getId(), period.getTime(), new InputTextMessageContent("- " + period.getTime()));
            article.setThumbUrl("https://img.icons8.com/color/48/present.png");
            article.setThumbHeight(100);
            article.setThumbWidth(100);
            queryResults.add(article);
        }
        return queryResults;
    }

    private void react(Reaction reaction, Integer msgId, boolean isEditable) {
        if (NOTHING == reaction.getAction())
            return;
        if (SAY == reaction.getAction()) {
            if (isEditable)
                editMessage(reaction, msgId);
            else
                senMessage(reaction);
        }
    }

    private void senMessage(Reaction reaction) {
        SendMessage message = new SendMessage();
        message.setText(reaction.getMessage());
        message.setParseMode("MarkdownV2");
        message.setChatId(CHAT_ID);
        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    private void editMessage(Reaction reaction, Integer msgId) {
        EditMessageText message = new EditMessageText();
        message.setText(reaction.getMessage());
        message.setParseMode("MarkdownV2");
        message.setChatId(CHAT_ID);
        message.setMessageId(msgId);
        try {
            execute(message);
            log.info("Reply sent");
        } catch (TelegramApiException e){
            log.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }
}
