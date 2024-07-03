package com.sailordev.dvorfsgamebot.telegram.dto.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class SelectKeyboard {

    public static InlineKeyboardMarkup getKeyboard(Integer positiveNum, Integer negativeNum) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
        InlineKeyboardButton buttonYES = new InlineKeyboardButton();
        InlineKeyboardButton buttonNO = new InlineKeyboardButton();
        buttonYES.setText("Да");
        buttonYES.setCallbackData("YES_" + Integer.toString(positiveNum));
        buttonNO.setText("Нет");
        buttonNO.setCallbackData("NO_" + Integer.toString(negativeNum));
        inlineKeyboardButtons.add(buttonYES);
        inlineKeyboardButtons.add(buttonNO);
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtons));
        return inlineKeyboardMarkup;
    }
}
