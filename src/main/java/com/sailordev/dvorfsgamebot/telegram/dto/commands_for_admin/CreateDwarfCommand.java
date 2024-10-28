package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;


import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class CreateDwarfCommand implements Command {

    private final UserCacheService userCacheService;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "Введите имя для нового гнома:";
        String chatId = user.getUserChatId();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        user.setState(UserState.AWAIT_SET_DWARF_NAME);
        userCacheService.save(user);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Создать гнома";
    }

    @Override
    public String getDescription() {
        return "Создание нового типа гномов";
    }
}
