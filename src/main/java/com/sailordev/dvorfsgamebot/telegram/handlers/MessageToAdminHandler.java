package com.sailordev.dvorfsgamebot.telegram.handlers;

import com.sailordev.dvorfsgamebot.model.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MessageToAdminHandler {

    private final String adminChatId = "777166751";

    public SendMessage sendMessageToAdmin(Update update, User user) {
        String chatId = update.getMessage().getChatId().toString();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(adminChatId);
        sendMessage.setText("Получено новое сообщение от " + user.getUserName() + "\n" +
                    update.getMessage().getText());

        return sendMessage;
    }

    public SendMessage sendMessageToUser(Update update, User user) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId("1992181657");
        sendMessage.setText("Новое сообщение от админа: \n" + update.getMessage().getText());
        return sendMessage;
    }
}
