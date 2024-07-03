package com.sailordev.dvorfsgamebot.telegram.handlers;

import com.sailordev.dvorfsgamebot.model.Event;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.EventRepository;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
public class EventsHandler {
    private static final Logger log = LoggerFactory.getLogger(EventsHandler.class);
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    private Event lastEvent;

    public SendMessage setEventDateTime(String updateText, UserEntity user) {
        LocalDateTime eventTime = null;
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        try {
            eventTime = LocalDateTime.parse(updateText,
                    DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm"));
            lastEvent = new Event();
            lastEvent.setEventDateTime(eventTime);
            eventRepository.save(lastEvent);
            user.setState(UserState.AWAIT_SET_NAME_OR_DESCRIPTION_EVENT);
            userRepository.save(user);
            text = "Желаете назначить имя или описание для ивента?";
            sendMessage.setText(text);
            sendMessage.setChatId(chatId);
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
}
