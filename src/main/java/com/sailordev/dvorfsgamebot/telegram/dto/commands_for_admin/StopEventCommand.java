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
public class StopEventCommand implements Command {

    private final EventRepository eventRepository;
    private final UserCacheService userCacheService;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        Iterator<Event> eventIterator = eventRepository.findAll().iterator();
        if(!eventIterator.hasNext()) {
            text = "Ивент для остановки не найден. Создайте новый ивент командой /create_event"
                    + " или выберите это действие с клавиатуры.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        text = "Выберите ивент для остановки.";
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(stopEventKeyboard(eventIterator));
        user.setState(UserState.AWAIT_SELECT_DELETE_EVENT);
        userCacheService.save(user);
        return sendMessage;
    }

    private InlineKeyboardMarkup stopEventKeyboard(Iterator<Event> iterator){
        List<Event> events = StreamSupport.stream(Spliterators.
                        spliteratorUnknownSize(iterator, Spliterator.ORDERED), false).toList();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        events.forEach(e -> {
            InlineKeyboardButton button = new InlineKeyboardButton(e.getId().toString());
            button.setCallbackData(e.getId().toString());
            buttons.add(button);
        });
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(buttons));
        return inlineKeyboardMarkup;
    }


    @Override
    public String getName() {
        return "Остановить ивент";
    }

    @Override
    public String getDescription() {
        return "Останавливает ивент, если он уже запущен";
    }
}
