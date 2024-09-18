package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.Hint;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.HintsRepository;
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
public class EditHintCommand implements Command {

    private final UserRepository userRepository;
    private final HintsRepository hintsRepository;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        Iterator<Hint> hintIterator = hintsRepository.findAll().iterator();
        if(!hintIterator.hasNext()) {
            text = "Сохраненных подсказок не найдено. Создайте новую подсказку командой" +
                    " /add_hint";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        List<Hint> hints = StreamSupport.stream(Spliterators.
                spliteratorUnknownSize(hintIterator, Spliterator.ORDERED), false).toList();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        StringBuilder builder = new StringBuilder("Выберите подсказку из списка ниже:\n\n");
        hints.forEach(h -> {
            builder.append(h.getId()).append(" : ").append(h.getHintDescription()).append("\n");
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(h.getId().toString());
            button.setCallbackData(h.getId().toString());
            buttons.add(button);
        });
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(buttons));
        text = builder.toString();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        user.setState(UserState.AWAIT_SELECT_EDIT_HINT);
        userRepository.save(user);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Редактировать подсказку";
    }

    @Override
    public String getDescription() {
        return "Редактировать одну подсказку из списка";
    }
}
