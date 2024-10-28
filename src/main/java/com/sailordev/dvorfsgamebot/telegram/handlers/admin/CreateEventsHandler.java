package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.model.Event;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.repositories.EventRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Interval;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class CreateEventsHandler {
    private static final Logger log = LoggerFactory.getLogger(CreateEventsHandler.class);
    public Interval interval;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserCacheService userCacheService;
    @Getter
    private Event lastEvent;

    public SendMessage setEventDateTime(String updateText, UserEntity user) {
        LocalDateTime eventTime = null;
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        try {
            eventTime = LocalDateTime.parse(updateText,
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            if(LocalDateTime.now().isAfter(eventTime)) {
                text = "Вы указали прошедшее время. Попробуйте снова.";
                sendMessage.setText(text);
                sendMessage.setChatId(user.getUserChatId());
                BotLogger.info(text, chatId);
                return sendMessage;
            }
            lastEvent = new Event();
            lastEvent.setEventDateTime(eventTime);
            eventRepository.save(lastEvent);
            user.setState(UserState.AWAIT_SET_NAME_OR_DESCRIPTION_EVENT);
            userCacheService.save(user);
            text = "Желаете назначить имя или описание для ивента?";
            sendMessage.setText(text);
            sendMessage.setChatId(chatId);
            sendMessage.setReplyMarkup(getEventKeyboard());
            BotLogger.info(text, chatId);
            return sendMessage;
        } catch (DateTimeParseException exception) {
            log.error(exception.toString());
        }
        text ="Передана не дата или неверный формат даты и времени. Попробуйте снова.";
        sendMessage.setText(text);
        sendMessage.setChatId(user.getUserChatId());
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage eventNameSetter(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Введите название для текущего ивента:");
        sendMessage.setChatId(user.getUserChatId());
        user.setState(UserState.AWAIT_EVENT_SET_NAME);
        userCacheService.save(user);
        return sendMessage;
    }

    public SendMessage eventDescriptionSetter(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Введите описание текущего ивента:");
        sendMessage.setChatId(user.getUserChatId());
        user.setState(UserState.AWAIT_EVENT_SET_DESCRIPTION);
        userCacheService.save(user);
        return sendMessage;
    }

    public SendMessage saveEventName(String eventName, UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        lastEvent.setName(eventName);
        eventRepository.save(lastEvent);
        sendMessage.setText("Ивент с именем " + eventName + " успешно сохранён." +
                "\nЖелаете добавить описание ивента?");
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button1.setText("Добавить");
        button1.setCallbackData("set_event_disc");
        button2.setText("Не добавлять");
        button2.setCallbackData("cancel_name");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button1, button2)));
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        user.setState(UserState.AWAIT_SET_NAME_OR_DESCRIPTION_EVENT);
        userCacheService.save(user);
        return sendMessage;
    }

    public SendMessage saveEvent(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Ивент успешно сохранён. Укажите интервал оповещения пользователей");
        user.setState(UserState.AWAIT_SET_NOTIFICATION_TIME);
        InlineKeyboardMarkup inlineKeyboardMarkup = getIntervalsKeyboard();
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        userCacheService.save(user);
        return sendMessage;
    }

    private static InlineKeyboardMarkup getIntervalsKeyboard() {
        InlineKeyboardButton day = new InlineKeyboardButton("1 день");
        day.setCallbackData("one_day");
        InlineKeyboardButton twoDays = new InlineKeyboardButton("2 дня");
        twoDays.setCallbackData("tho_days");
        InlineKeyboardButton threeDays = new InlineKeyboardButton("3 дня");
        threeDays.setCallbackData("three_days");
        InlineKeyboardButton week = new InlineKeyboardButton("1 неделя");
        week.setCallbackData("week");
        InlineKeyboardButton twoWeeks = new InlineKeyboardButton("2 недели");
        twoWeeks.setCallbackData("two_weeks");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(day, twoDays),
                List.of( threeDays, week), List.of(twoWeeks)));
        return inlineKeyboardMarkup;
    }

    public SendMessage saveNotifications(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Интервал оповещений сохранен");
        sendMessage.setChatId(user.getUserChatId());
        user.setState(UserState.SLEEP);
        userCacheService.save(user);
        return sendMessage;
    }

    public SendMessage saveEventDescription(UserEntity user, String description) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        lastEvent.setDescription(description);
        eventRepository.save(lastEvent);
        sendMessage.setText("Описание сохранено.");
        return sendMessage;
    }

    public SendMessage startEventNotifications(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String description = "";
        if(lastEvent.getDescription() != null) {
            description += "\nОзнакомьтесь с инструкциями к данному ивенту:\n";
            description += lastEvent.getDescription();
        }
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("<b>Ивент " + lastEvent.getName() + " стартовал и начнется "
                + lastEvent.getEventDateTime().
                format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + ".</b>" +
                description);
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    public SendMessage getNotification(UserEntity user) {
        Duration duration = Duration.between(LocalDateTime.now(), lastEvent.getEventDateTime());
        long totalMinutes = duration.toMinutes();
        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60) / 60);
        long minutes = totalMinutes % 60;
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        String text = "<b>До старта ивента " + lastEvent.getName() +
                " остается " + days + " дней, " + hours + " часов, " +
                minutes + " минут. </b>";
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    private InlineKeyboardMarkup getEventKeyboard() {
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button1.setText("Назначить имя");
        button1.setCallbackData("set_event_name");
        button2.setText("Добавить описание");
        button2.setCallbackData("set_event_disc");
        button3.setText("Отмена");
        button3.setCallbackData("cancel_name");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button1, button2), List.of(button3)));
        return inlineKeyboardMarkup;
    }

    public SendMessage getStartEventNotification(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("<b>ОХОТА НА ГНОМОВ ОТКРЫТА</b>\n" +
                "Получить координаты, подсказки и виды гномов вы сможете в меню бота");
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

}
