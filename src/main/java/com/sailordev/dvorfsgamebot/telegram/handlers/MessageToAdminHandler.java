package com.sailordev.dvorfsgamebot.telegram.handlers;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageToAdminHandler {

    private final String adminChatId;
    private final SendMessage sendMessage = new SendMessage();
    private final UserRepository userRepository;
    private UserEntity lastUser;

    public SendMessage sendMessageToAdmin(String updateText, UserEntity user) {
        String chatId = user.getUserChatId();
        String text = "<b>Получено новое сообщение от</b> " + user.getUserName() + "\n\n" +
                updateText;
        sendMessage.setChatId(adminChatId);
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");
        BotLogger.info(chatId, text);
        return sendMessage;
    }

    public SendMessage sendMessageToUser(String updateText, String userId, UserEntity user) {
        String text = "";
        String chatId = user.getUserChatId();
        Optional<UserEntity> userEntityOptional =
                userRepository.findById(Integer.parseInt(userId));
        if(userEntityOptional.isEmpty()) {
            text = "Пользователь с таким id не найден";
            sendMessage.setChatId(user.getUserChatId());
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        text = "<b>Новое сообщение от админа:</b> \n\n" + updateText;
        BotLogger.info(text.replace("</b>", ""), updateText);
        sendMessage.setChatId(userEntityOptional.get().getUserChatId());
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    public SendMessage sendAnswerToAdmin(){
        String text = "Ваш ответ отправлен, повелитель";
        sendMessage.setChatId(adminChatId);
        sendMessage.setText(text);
        BotLogger.info(text, adminChatId);
        return sendMessage;
    }

    public SendMessage sendAnswerToUser(UserEntity user) {
        String text = "Ваше сообщение отправлено, дождитесь ответа администратора";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        BotLogger.info(text, chatId);
        return sendMessage;
    }
}
