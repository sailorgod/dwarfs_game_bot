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
public class UnblockCommand implements Command {

   private final UserRepository userRepository;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        Iterator<UserEntity> userIterator = userRepository.findAll().iterator();
        if(!userIterator.hasNext()) {
            text = "Список пользователей пуст.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        List<UserEntity> users
                = StreamSupport.stream(Spliterators
                .spliteratorUnknownSize(userIterator, Spliterator.ORDERED), false).toList();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("Список заблокированных пользователей:");
        users.forEach(u -> {
            if(u.getState().equals(UserState.BAN)) {
                builder.append("\n").append(u.getId()).append(" - ").append(u.getUserName());
                InlineKeyboardButton button = new InlineKeyboardButton(u.getId().toString());
                button.setCallbackData(u.getId().toString());
                buttons.add(button);
            }
        });
        if(buttons.isEmpty()) {
            text = "Список заблокированных пользователей пуст.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(buttons));
        sendMessage.setText(builder.toString());
        sendMessage.setReplyMarkup(keyboardMarkup);
        user.setState(UserState.AWAIT_SELECT_UNBLOCK_USER);
        userRepository.save(user);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Разблокировать пользователя";
    }

    @Override
    public String getDescription() {
        return "Выводит из бана пользователя из списка";
    }
}
