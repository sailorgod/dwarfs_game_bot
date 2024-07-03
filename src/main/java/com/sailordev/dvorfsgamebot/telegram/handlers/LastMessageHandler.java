package com.sailordev.dvorfsgamebot.telegram.handlers;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;


import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LastMessageHandler {

    private final UserRepository userRepository;
    private final SendMessage sendMessage = new SendMessage();

    public SendMessage getLastUserMessage(String updateText, UserEntity user) {
        String text = "";
        String chatId = user.getUserChatId();
        if(!updateText.matches("[0-9]+")) {
            text = "В качестве ответа ожидается id пользователя, выберите его из списка выше";
            sendMessage.setText(text);
            sendMessage.setChatId(user.getUserChatId());
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<UserEntity> optional = userRepository.findById(Integer.parseInt(updateText));
        SendMessage sendMessage = new SendMessage();
        if(optional.isEmpty() || !optional.get().getState().equals(UserState.AWAIT_ADMINS_RESPONSE)) {
            text = "Пользователь с таким айди не найден или не ожидает вашего ответа";
            sendMessage.setText(text);
            sendMessage.setChatId(user.getUserChatId());
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkup();
        UserEntity waitingUser = optional.get();
        text = "<b>Последнее сообщение от </b>" + waitingUser.getUserName() + ":\n\n" +
                waitingUser.getLastMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setParseMode("HTML");
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        user.setState(UserState.AWAIT_SELECT_ACTION);
        userRepository.save(user);
        return sendMessage;
    }

    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        InlineKeyboardButton responseButton = new InlineKeyboardButton();
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        responseButton.setText("Ответить");
        responseButton.setCallbackData("response_to_user");
        backButton.setText("Назад");
        backButton.setCallbackData("back_to_users_select");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(responseButton, backButton)));
        return inlineKeyboardMarkup;
    }
}
