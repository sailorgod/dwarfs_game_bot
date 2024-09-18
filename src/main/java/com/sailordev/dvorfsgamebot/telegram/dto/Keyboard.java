package com.sailordev.dvorfsgamebot.telegram.dto;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

public interface Keyboard {

    CommandsHandler getCommandsHandler();

    default ReplyKeyboardMarkup getKeyboard() {
        KeyboardRow keyboardRow = new KeyboardRow();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        HashMap<String, Command> commands = getCommandsHandler().getCommands();
        Collection<Command> commandCollection = commands.values();
        List<Command> commandList = new ArrayList<>(commandCollection.stream().toList());
        commandList.sort(Comparator.comparing(Command::getName));
        for(Command command : commandList){
            if(command.getName().equals("Старт")) {
                continue;
            }
            KeyboardButton keyboardButton = new KeyboardButton();
            keyboardButton.setText(command.getName());
            keyboardRow.add(keyboardButton);
            if(keyboardRow.size() == 2) {
                keyboardRows.add(keyboardRow);
                keyboardRow = new KeyboardRow();
            }
        }
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        return replyKeyboardMarkup;
    }
}
