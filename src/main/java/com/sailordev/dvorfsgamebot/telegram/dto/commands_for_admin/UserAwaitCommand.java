package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class UserAwaitCommand implements Command {

    private final UserCacheService userCacheService;
    private static final String USER_AWAIT_DESCRIPTION = "Выводит список пользователей, " +
            "которые ждут вашего ответа";
    private static final String NAME = "Ответ пользователям";
    @Getter
    private InlineKeyboardMarkup lastMarkup;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        Iterable<UserEntity> iterable = userCacheService.findAll();
        Set<UserEntity> userSet = StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toSet());
        int userCount = (int) userSet.stream().
                filter(u -> u.getState().equals(UserState.AWAIT_ADMINS_RESPONSE)).count();
        if(userCount == 0){
            text = "Пользователи, которые ждут вашего ответа, отсутствуют";
            sendMessage.setText(text);
            sendMessage.setChatId(chatId);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        StringBuilder stringBuilder = new StringBuilder();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        stringBuilder.append("<b>Список юзеров, которые ждут вашего ответа:</b>\n");
        for (UserEntity u : userSet) {
            if(u.getState().equals(UserState.AWAIT_ADMINS_RESPONSE)){
                String id = u.getId().toString();
                stringBuilder.append(id).
                        append(" - ").
                        append(u.getUserName()).
                        append(" ✉\uFE0F");
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(id);
                button.setCallbackData(id);
                buttons.add(button);
            }
        }
        lastMarkup = new InlineKeyboardMarkup();
        lastMarkup.setKeyboard(List.of(buttons));
        stringBuilder.append("\nВведите номер пользователя, чтобы увидеть его собщение");
        text = stringBuilder.toString();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");
        sendMessage.setReplyMarkup(lastMarkup);
        user.setState(UserState.AWAIT_USER_SELECTION);
        BotLogger.info(text, chatId);
        userCacheService.save(user);
        return sendMessage;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return USER_AWAIT_DESCRIPTION;
    }
}