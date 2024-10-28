package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.model.Dwarf;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.repositories.DwarfsRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
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
public class SelectDwarfsHandler {

    private final DwarfsRepository dwarfsRepository;
    private final UserCacheService userCacheService;

    public SendMessage getDwarfsMessage(UserEntity user, UserState state){
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";
        Iterator<Dwarf> dwarfIterator = dwarfsRepository.findAll().iterator();
        if(!dwarfIterator.hasNext()) {
            text = "Сохраненные гномы отсуствуют. Создайте нового гнома командой /create_dwarf";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        List<Dwarf> dwarves = StreamSupport.
                stream(Spliterators.spliteratorUnknownSize(dwarfIterator, Spliterator.ORDERED),
                        false).toList();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Выберите гнома из списка ниже, которого вы хотите изменить:\n");
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        dwarves.forEach(d -> {
            String id = d.getId().toString();
            InlineKeyboardButton button = new InlineKeyboardButton(id);
            button.setCallbackData(id);
            buttons.add(button);
            stringBuilder.append(id).append(" - ").append(d.getName()).append("\n");
        });
        text = stringBuilder.toString();
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(buttons));
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        BotLogger.info(text, chatId);
        user.setState(state);
        userCacheService.save(user);
        return sendMessage;
    }
}
