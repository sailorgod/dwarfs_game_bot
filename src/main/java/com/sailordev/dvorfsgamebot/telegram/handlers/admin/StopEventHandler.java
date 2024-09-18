package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.model.Event;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.EventRepository;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StopEventHandler {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public SendMessage stopEvent(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String text = "Ивент успешно остановлен.";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        if(!Character.isDigit(updateText.charAt(0))) {
            text = "Передано некорректный номер ивента. Просьба выбрать число с клавиатуры под сообщением.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        int eventId = Integer.parseInt(updateText);
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        if(eventOptional.isEmpty()) {
            text = "Ивент с таким номером не найден. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        sendMessage.setText(text);
        eventRepository.deleteById(eventId);
        user.setState(UserState.SLEEP);
        userRepository.save(user);
        return sendMessage;
    }

    public SendMessage stopEventNotification(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "<b>ОХОТА ОСТАНОВЛЕНА</b>\n" +
                "Дождитесь начала новой охоты или инструкций от администратора";
        String chatId = user.getUserChatId();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("HTML");
        BotLogger.info(text, chatId);
        return sendMessage;
    }
}
