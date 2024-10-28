package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BanHandler {

    private final UserCacheService userCacheService;
    private UserEntity lastUser;

    public SendMessage banUserMessage(UserEntity user, String updateText) {
        return banHandle(" заблокирован.", updateText, user, UserState.BAN);
    }

    public SendMessage unblockUserMessage(UserEntity user, String updateText) {
        return banHandle(" разблокирован.", updateText, user, UserState.SLEEP);
    }

    private SendMessage banHandle(String text, String updateText, UserEntity user, UserState state){
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        if(!Character.isDigit(updateText.charAt(0))) {
            text = "Ожидаю в качестве ответа id пользователя. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<UserEntity> optionalUser = userCacheService.findById(Integer.parseInt(updateText));
        if(optionalUser.isEmpty()) {
            text = "Пользователь с таким id не найден. Попробуйте снова";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        lastUser = optionalUser.get();
        lastUser.setState(state);
        userCacheService.save(lastUser);
        sendMessage.setText("Пользователь " + lastUser.getUserName() + text);
        user.setState(UserState.SLEEP);
        userCacheService.save(user);
        return sendMessage;
    }

    public SendMessage sendMessageBanUser() {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(lastUser.getUserChatId());
        String text = "";
        if(lastUser.getState().equals(UserState.BAN)) {
            text = "Вы заблокированы по решению администратора.";
        } else {
            text = "Доступ восстановлен, добро пожаловать :)";
        }
        sendMessage.setText(text);
        return sendMessage;
    }

    public SendMessage sendMessageBanUserAfterWarn(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Вы заблокированы за спам.");
        return sendMessage;
    }

    public SendMessage sendBanWarning(UserEntity user) {
        String tryCount = String.valueOf(4 - user.getWarnCount());
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Слишком частое обращение ко мне. " +
                "Предупреждаю, спам чреват блокировкой. Осталось попыток: "
                + tryCount);
        return sendMessage;
    }
}
