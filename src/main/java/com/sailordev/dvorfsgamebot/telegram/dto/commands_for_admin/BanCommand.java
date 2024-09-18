package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
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
public class BanCommand implements Command {

    private final UserRepository userRepository;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "Список пользователей пуст.";
        Iterator<UserEntity> usersIterator = userRepository.findAll().iterator();
        if(!usersIterator.hasNext()) {
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        List<UserEntity> users =
                StreamSupport.stream(Spliterators
                                .spliteratorUnknownSize(usersIterator,
                                        Spliterator.ORDERED), false).toList();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("Выберите пользователя из списка ниже:");
        users.forEach(u -> {
            if(!u.getState().equals(UserState.BAN)) {
                builder.append("\n").append(u.getId()).append(" - ").append(u.getUserName());
                InlineKeyboardButton button = new InlineKeyboardButton(u.getId().toString());
                button.setCallbackData(u.getId().toString());
                buttons.add(button);
            }
        });
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(buttons));
        text = builder.toString();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        user.setState(UserState.AWAIT_SELECT_USER_BAN);
        userRepository.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Бан пользователя";
    }

    @Override
    public String getDescription() {
        return "Блокирует выбранного пользователя";
    }
}
