package com.sailordev.dvorfsgamebot.telegram.dto.default_commands;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.telegram.configs.AdminUserProperty;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import com.sailordev.dvorfsgamebot.telegram.dto.keyboard.SelectKeyboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartCommand implements Command {

    private final UserCacheService userCacheService;
    private final AdminUserProperty adminUserProperty;
    private static final String NAME = "Старт";
    private static final String START_DESCRIPTION = "Начать игру заново";

    @Override
    public SendMessage sendCommandMessage(UserEntity user)  {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("HTML");
        if(user.getUserChatId().equals(adminUserProperty.getChatId())) {
            text = "<b>Приветсвую, повелитель!</b> Для вызова клавиатуры жмякни -> /set_keyboard";
            sendMessage.setText(text);
            user.setState(UserState.AWAIT_SET_KEYBOARD);
        } else {
            text = "<b>Приветствую тебя, странник. Хочешь вступить в наши ряды?</b>";
            sendMessage.setText(text);
            sendMessage.setReplyMarkup(SelectKeyboard.getKeyboard(1, 1));
            user.setState(UserState.AWAIT_START);
        }
        userCacheService.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return START_DESCRIPTION;
    }

}
