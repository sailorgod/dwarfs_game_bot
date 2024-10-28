package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.Event;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.repositories.EventRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateEventCommand implements Command {

    private final UserCacheService userCacheService;
    private final EventRepository eventRepository;
    private static final String NAME = "Создать ивент";
    private static final String CREATE_EVENT_DESCRIPTION = "Создать новый ивент";

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        String text = "";
        Iterator<Event> eventIterator = eventRepository.findAll().iterator();
        if(eventIterator.hasNext()) {
            text = "Прошу прощения, ивент уже создан. Остановите текущий ивент командой /stop_event" +
                    "или выберите это действие с клавиатуры";
            sendMessage.setText(text);
            return sendMessage;
        }
        sendMessage.setChatId(chatId);
        user.setState(UserState.AWAIT_SET_EVENT_DATE);
        userCacheService.save(user);
        text = "Отлично. Введите дату и время начала ивента в формате - дд.мм.гггг чч:мм";
        sendMessage.setText(text);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return CREATE_EVENT_DESCRIPTION;
    }
}
