package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.Coordinates;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.repositories.CoordinatesRepository;
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
public class EditCoordinatesCommand implements Command {

    private final CoordinatesRepository coordinatesRepository;
    private final UserCacheService userCacheService;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        Iterator<Coordinates> coordinatesIterator = coordinatesRepository.findAll().iterator();
        if(!coordinatesIterator.hasNext()) {
            text = "Сохраненные координаты не найдены. Добавьте их командой /add_coordinates" +
                " или выберите это действие с клавиатуры бота.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        List<Coordinates> coordinates = StreamSupport.stream(Spliterators.
                spliteratorUnknownSize(coordinatesIterator, Spliterator.ORDERED), false)
                .toList();
        StringBuilder builder = new StringBuilder();
        builder.append("Выберите координаты из списка ниже: \n");
        coordinates.forEach(c -> {
            builder.append(c.getId().toString()).append(" - ").
                    append(c.getCoordinates()).append("\n");
        });
        text = builder.toString();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getKeyboard(coordinates));
        BotLogger.info(text, chatId);
        user.setState(UserState.AWAIT_SELECT_EDIT_COORDINATE);
        userCacheService.save(user);
        return sendMessage;
    }

    private InlineKeyboardMarkup getKeyboard(List<Coordinates> coordinates) {
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
        coordinates.forEach(c -> {
            InlineKeyboardButton button = new InlineKeyboardButton(c.getId().toString());
            button.setCallbackData(c.getId().toString());
            inlineKeyboardButtons.add(button);
        });
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtons));
        return inlineKeyboardMarkup;
    }

    @Override
    public String getName() {
        return "Редактировать координаты";
    }

    @Override
    public String getDescription() {
        return "Редактирует уже созданные координаты";
    }
}
