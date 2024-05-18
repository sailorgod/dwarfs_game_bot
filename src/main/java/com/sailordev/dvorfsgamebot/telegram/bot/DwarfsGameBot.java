package com.sailordev.dvorfsgamebot.telegram.bot;

import com.sailordev.dvorfsgamebot.model.User;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.configs.BotProperties;
import com.sailordev.dvorfsgamebot.telegram.handlers.MessageToAdminHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DwarfsGameBot extends TelegramLongPollingBot {

    @Autowired
    private final BotProperties botProperties;
    @Autowired
    private final UserRepository userRepository;
    private MessageToAdminHandler messageToAdminHandler = new MessageToAdminHandler();



    @Override
    public void onUpdateReceived(Update update) {

    }

    public void sendMessages(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            User user = new User();
            user.setUserName("");
            user.setUserChatId(chatId);
            userRepository.save(user);
            sendMessageToAdmin(update, user);
        }
    }

    public void sendMessageToAdmin(Update update, User user) {
        try {
            execute(messageToAdminHandler.sendMessageToAdmin(update, user));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }
}
