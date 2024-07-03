package com.sailordev.dvorfsgamebot.telegram.dto;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Keyboard {

    CommandsHandler getCommandsHandler();

    default ReplyKeyboardMarkup getKeyboard() {
        KeyboardRow keyboardRow = new KeyboardRow();
        HashMap<String, Command> commands = getCommandsHandler().getCommands();
        for(Map.Entry<String, Command> entry : commands.entrySet()){
            KeyboardButton keyboardButton = new KeyboardButton();
            keyboardButton.setText(entry.getValue().getName());
            keyboardRow.add(keyboardButton);
        }
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(List.of(keyboardRow));
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }
}
