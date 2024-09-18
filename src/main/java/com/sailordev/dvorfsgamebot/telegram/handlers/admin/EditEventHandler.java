package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.model.Event;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.EventRepository;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EditEventHandler {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    @Getter
    private Event lastEvent;

    public SendMessage selectEvent(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";
        if(!Character.isDigit(updateText.charAt(0))) {
            text = "Прошу прощения, передано не число или номер, который отсуствует в списке. Попробуйте снова";
            BotLogger.info(text, chatId);
            sendMessage.setText(text);
            return sendMessage;
        }
        Optional<Event> optionalEvent = eventRepository.findById(Integer.parseInt(updateText));
        if(optionalEvent.isEmpty()) {
            text = "Ивент с таким номером не найден. Попробуйте снова.";
            BotLogger.info(text, chatId);
            sendMessage.setText(text);
            return sendMessage;
        }
        lastEvent = optionalEvent.get();
        text = "Отлично. Что вы хотели бы изменить?";
        return editMessage(text, user);
    }

    public SendMessage editEventName(String updateText, UserEntity user) {
        lastEvent.setName(updateText);
        eventRepository.save(lastEvent);
        String text = "Имя ивента успешно изменено на <b>" + updateText + "</b>. Хотите ещё что-то изменить?";
        return editMessage(text, user);
    }

    public SendMessage editEventDescription(String updateText, UserEntity user) {
        lastEvent.setDescription(updateText);
        eventRepository.save(lastEvent);
        String text = "Описание ивента успешно изменено на <b>" + updateText + "</b>. Хотите ещё что-то изменить?";
        return editMessage(text, user);
    }

    public SendMessage editEventDate(String updateText, UserEntity user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String text = "";
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(updateText, formatter);
            if(LocalDateTime.now().isAfter(localDateTime)) {
                text = "Вы указали прошедшее время. Попробуйте снова.";
                SendMessage sendMessage = new SendMessage();
                String chatId = user.getUserChatId();
                sendMessage.setText(text);
                sendMessage.setChatId(chatId);
                BotLogger.info(text, chatId);
                return sendMessage;
            }
            lastEvent.setEventDateTime(localDateTime);
            eventRepository.save(lastEvent);
            text = "Новая дата ивента - <b>" + localDateTime +"</b>." +
                    "Хотите ещё что-то изменить?";
            return editMessage(text, user);
        } catch (DateTimeParseException exception) {
            log.error(exception.toString());
            text = "Передан неверный формат даты, попробуйте снова";
            return getSendMessage(user, text);
        }
    }

    public SendMessage editMessage(String text, UserEntity user) {
        user.setState(UserState.AWAIT_SELECT_EDIT_EVENT);
        userRepository.save(user);
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        BotLogger.info(text, chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getSelectActionKeyboard());
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    public SendMessage messageEditName(UserEntity user) {
        String text = "Введите новое имя для ивента";
        user.setState(UserState.AWAIT_EDIT_EVENT_NAME);
        userRepository.save(user);
        return getSendMessage(user, text);
    }

    public SendMessage messageEditDescription(UserEntity user) {
        String text = "Введите новое описание для ивента";
        user.setState(UserState.AWAIT_EDIT_EVENT_DESCRIPTION);
        userRepository.save(user);
        return getSendMessage(user, text);
    }

    public SendMessage messageEditDate(UserEntity user) {
        String text = "Введите новую дату старта в формате дд.мм.гггг чч:мм";
        user.setState(UserState.AWAIT_EDIT_EVENT_DATE);
        userRepository.save(user);
        return getSendMessage(user, text);
    }

    public SendMessage getEditHandler(UserEntity user, String updateText){
        switch (updateText) {
            case "edit_event_name" ->  {
                return messageEditName(user);
            }
            case "edit_event_description" -> {
                return messageEditDescription(user);
            }
            case "edit_event_date" -> {
                return messageEditDate(user);
            }
            case "cancel_edit_event" -> {
                return cancelEditEvent(user);
            }
            default -> {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(user.getUserChatId());
                sendMessage.setText("Ответ мне не понятен, попробуйте снова");
                return sendMessage;
            }
        }
    }

    private SendMessage cancelEditEvent(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Отменено");
        user.setState(UserState.SLEEP);
        userRepository.save(user);
        return sendMessage;
    }

    private SendMessage getSendMessage(UserEntity user, String text) {
        String chatId = user.getUserChatId();
        BotLogger.info(text, chatId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        return sendMessage;
    }

    private InlineKeyboardMarkup getSelectActionKeyboard() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("Изменить имя ивента");
        button1.setCallbackData("edit_event_name");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Изменить описание");
        button2.setCallbackData("edit_event_description");
        InlineKeyboardButton button3 = new InlineKeyboardButton("Изменить дату начала");
        button3.setCallbackData("edit_event_date");
        InlineKeyboardButton button4 = new InlineKeyboardButton("Отмена");
        button4.setCallbackData("cancel_edit_event");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button1, button2), List.of(button3, button4)));
        return inlineKeyboardMarkup;
    }

    public SendMessage getEditNotification(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("<b>Новая дата ивента " + lastEvent.getName() +
                " - " + lastEvent.getEventDateTime() + "</b>");
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }
}
