package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.model.Coordinates;
import com.sailordev.dvorfsgamebot.model.Hint;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.CoordinatesRepository;
import com.sailordev.dvorfsgamebot.repositories.HintsRepository;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HintsHandler {

    private final UserRepository userRepository;
    private final HintsRepository hintsRepository;
    private final CoordinatesRepository coordinatesRepository;
    private Coordinates lastCoordinate;
    private Hint lastHint;

    public SendMessage saveHintMessage(UserEntity user, String updateText) {
        String text = "<b>Подсказка сохранена:</b> \n\n" + updateText;
        Hint hint = new Hint();
        hint.setHintDescription(updateText);
        hint.setCoordinate(lastCoordinate);
        List<Hint> hints = new ArrayList<>();
        if(lastCoordinate.getHints() != null) {
            hints = lastCoordinate.getHints();
        }
        hints.add(hint);
        lastCoordinate.setHints(hints);
        hintsRepository.save(hint);
        coordinatesRepository.save(lastCoordinate);
        user.setState(UserState.SLEEP);
        return getMessage(user, updateText, text);
    }

    public SendMessage addHintMessage(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";
        if(!Character.isDigit(updateText.charAt(0))) {
            text = "Ожидаю получить ответ в виде id координат, попробуйте снова";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<Coordinates> coordinatesOptional
                = coordinatesRepository.findById(Integer.parseInt(updateText));
        if(coordinatesOptional.isEmpty()) {
            text = "Координата с таким id не найдена. Попробуйте снова";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        lastCoordinate = coordinatesOptional.get();
        text = "Введите подсказку для данной координаты:";
        sendMessage.setText(text);
        user.setState(UserState.AWAIT_ADD_HINT);
        userRepository.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage selectEditHintMessage(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";
        if(!Character.isDigit(updateText.charAt(0))) {
            text = "Ожидаю получить ответ в виде id подсказки, попробуйте снова";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<Hint> hintOptional = hintsRepository.findById(Integer.parseInt(updateText));
        if(hintOptional.isEmpty()) {
            text = "Подсказка с таким id не найдена, попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        lastHint = hintOptional.get();
        text = "Введите новое описание для данной подсказки:";
        sendMessage.setText(text);
        user.setState(UserState.AWAIT_EDIT_HINT);
        userRepository.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage editHint(UserEntity user, String updateText) {
        String text = "Подсказка изменена.";
        lastHint.setHintDescription(updateText);
        hintsRepository.save(lastHint);
        return getMessage(user, updateText, text);
    }

    public SendMessage deleteHint(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        String text = "";
        sendMessage.setChatId(chatId);
        if(!Character.isDigit(updateText.charAt(0))){
            text = "Ожидаю получить id подсказки. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<Hint> hintOptional = hintsRepository.findById(Integer.parseInt(updateText));
        if(hintOptional.isEmpty()) {
            text = "Подсказка с таким id отсутсвует. Выберите номер подсказки из списка выше";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        text = "Подсказка удалена.";
        hintsRepository.delete(hintOptional.get());
        user.setState(UserState.SLEEP);
        userRepository.save(user);
        sendMessage.setText(text);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    private SendMessage getMessage(UserEntity user, String updateText, String text){
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("HTML");
        user.setState(UserState.SLEEP);
        userRepository.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }
}
