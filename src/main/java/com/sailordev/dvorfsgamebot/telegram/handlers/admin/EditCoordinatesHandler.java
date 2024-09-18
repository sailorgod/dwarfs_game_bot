package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.model.Coordinates;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.CoordinatesRepository;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EditCoordinatesHandler {

    private final CoordinatesRepository coordinatesRepository;
    private final UserRepository userRepository;
    private final AddCoordinatesHandler addCoordinatesHandler;

    public SendMessage selectCoordinate(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";

        if(!Character.isDigit(updateText.charAt(0))) {
            text = "Ожидается число в качестве ответа. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }

        Optional<Coordinates> coordinatesOptional =
                coordinatesRepository.findById(Integer.parseInt(updateText));
        if(coordinatesOptional.isEmpty()) {
            text = "Координаты с таким id не найдены. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Coordinates coordinates = coordinatesOptional.get();
        addCoordinatesHandler.setLastCoordinate(coordinates);
        text = "Выберите, что вы хотите изменить.";
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(addCoordinatesHandler.getSelectKeyboard());
        user.setState(UserState.AWAIT_SELECT_ACTION_COORDINATE);
        userRepository.save(user);
        return sendMessage;
    }

    public SendMessage deleteCoordinate(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";

        if(!Character.isDigit(updateText.charAt(0))) {
            text = "Ожидается число в качестве ответа. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }

        Optional<Coordinates> coordinatesOptional =
                coordinatesRepository.findById(Integer.parseInt(updateText));
        if(coordinatesOptional.isEmpty()) {
            text = "Координаты с таким id не найдены. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        text = "Координаты удалены.";
        Coordinates coordinates = coordinatesOptional.get();
        addCoordinatesHandler.setLastCoordinate(coordinates);
        coordinatesRepository.delete(coordinates);
        sendMessage.setText(text);
        user.setState(UserState.SLEEP);
        userRepository.save(user);
        return sendMessage;
    }

}
