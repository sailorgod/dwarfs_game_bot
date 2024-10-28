package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.Event;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.repositories.EventRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class EditEventsCommand implements Command {

    private final EventRepository eventRepository;
    private final UserCacheService userCacheService;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        String text = "Выберите ивент из списка ниже.";
        Iterator<Event> eventIterator = eventRepository.findAll().iterator();
        if(!eventIterator.hasNext()) {
            text = "Я не нашел в своей памяти созданных ивентов. Рекомендую начать работу с новым ивентом. \n" +
                    "Для этого выберите команду /create_event или выберите это действие в меню клавиатуры.";
            sendMessage.setText(text);
            BotLogger.info(text, user.getUserChatId());
            return sendMessage;
        }
        sendMessage.setText(text);
        List<Event> events = StreamSupport.
                stream(Spliterators.spliteratorUnknownSize(eventIterator, Spliterator.ORDERED), false).toList();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (Event event : events) {
            InlineKeyboardButton button = new InlineKeyboardButton(event.getName());
            button.setCallbackData(event.getId().toString());
            buttons.add(button);
        }
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(buttons));
        sendMessage.setReplyMarkup(keyboardMarkup);
        user.setState(UserState.AWAIT_FIRST_SELECT_EDIT_EVENT);
        userCacheService.save(user);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Редактировать ивент";
    }

    @Override
    public String getDescription() {
        return "Редактирует ивент из списка";
    }
}
