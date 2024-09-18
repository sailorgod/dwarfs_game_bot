package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.Coordinates;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.CoordinatesRepository;
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
public class AddHintCommand implements Command {

    private final UserRepository userRepository;
    private final CoordinatesRepository coordinatesRepository;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";
        Iterator<Coordinates> coordinatesIterator = coordinatesRepository.findAll().iterator();
        if(!coordinatesIterator.hasNext()){
            text = "Не найдено ни одной координаты, для которой можно добавить подсказку.\n" +
                    "Создайте новую координату командой /add_coordinates";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        List<Coordinates> coordinates = StreamSupport.
                stream(Spliterators.spliteratorUnknownSize(coordinatesIterator, Spliterator.ORDERED),
                        false).toList();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append("Выберите номер координаты из списка ниже, к которой желаете добавить подсказку");
        coordinates.forEach(c -> {
            String coordinateId = c.getId().toString();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(coordinateId);
            inlineKeyboardButton.setCallbackData(coordinateId);
            buttons.add(inlineKeyboardButton);
            builder.append("\n").append(coordinateId).append(" - ").append(c.getCoordinates());
        });
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(List.of(buttons));
        text = builder.toString();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboardMarkup);
        user.setState(UserState.AWAIT_SELECT_COORDINATE_FOR_ADD_HINT);
        userRepository.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Добавить подсказку";
    }

    @Override
    public String getDescription() {
        return "Добавить новую подсказку по гномам для пользователей";
    }
}
