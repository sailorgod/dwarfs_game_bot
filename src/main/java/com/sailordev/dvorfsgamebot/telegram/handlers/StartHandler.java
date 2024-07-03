package com.sailordev.dvorfsgamebot.telegram.handlers;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import com.sailordev.dvorfsgamebot.telegram.dto.keyboard.KeyboardForAdmin;
import com.sailordev.dvorfsgamebot.telegram.dto.keyboard.KeyboardForUser;
import com.sailordev.dvorfsgamebot.telegram.dto.keyboard.SelectKeyboard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StartHandler {

    private static final String WELL_COME_MESSAGE = "Добро пожаловать в игру! Набор в охотники скоро начнется." +
            "\nМы оповестим вас о начале охоты";
    private final UserRepository userRepository;
    private final KeyboardForUser keyboardForUser;
    private final KeyboardForAdmin keyboardForAdmin;
    private final SendMessage sendMessage = new SendMessage();

    public SendMessage getFirstWarning(UserEntity user) {
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Ты уверен?");
        sendMessage.setReplyMarkup(SelectKeyboard.getKeyboard(2, 2));
        return sendMessage;
    }

    public SendMessage getPositiveWarning(UserEntity user) {
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Так ты всё таки хочешь присоединиться к игре?");
        sendMessage.setReplyMarkup(SelectKeyboard.getKeyboard(1, 3));
        return sendMessage;
    }

    public SendMessage getSecondWarning(UserEntity user) {
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Ты уверен? Если откажешься, доступ будет закрыт навсегда.");
        sendMessage.setReplyMarkup(SelectKeyboard.getKeyboard(3, 2));
        return sendMessage;
    }

    public SendMessage getBanMessage(String callbackQuery, UserEntity user) {
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Доступ закрыт.");
        sendMessage.setReplyMarkup(null);
        user.setState(UserState.BAN);
        userRepository.save(user);
        return sendMessage;
    }

    public SendMessage wellComeMessageForAdmin(UserEntity user) {
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Добро пожаловать, повелитель");
        sendMessage.setReplyMarkup(keyboardForAdmin.getKeyboard());
        user.setState(UserState.SLEEP);
        userRepository.save(user);
        return sendMessage;
    }

    public SendMessage wellComeMessage(UserEntity user) {
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText(WELL_COME_MESSAGE);
        sendMessage.setReplyMarkup(keyboardForUser.getKeyboard());
        return sendMessage;
    }

    public SendMessage getUniformMessage(UserEntity user) {
        sendMessage.setChatId(user.getUserChatId());
        sendMessage.setText("Первое задание будет на получение униформы охотника. \n" +
                "Для этого потребуется связаться с администратором. \n" +
                "Когда будешь готов, нажми Получить униформу.");
        sendMessage.setParseMode("HTML");
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Получить униформу");
        inlineKeyboardButton.setCallbackData("get_uniform");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(inlineKeyboardButton)));
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

}
