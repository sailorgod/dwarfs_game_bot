package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.model.Dwarf;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.repositories.DwarfsRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CreateDwarfHandler {

    private final UserCacheService userCacheService;
    private final DwarfsRepository dwarfsRepository;
    @Setter
    private Dwarf lastDwarf;

    public SendMessage saveNameMessage(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        Dwarf dwarf = new Dwarf();
        dwarf.setName(updateText);
        lastDwarf = dwarf;
        dwarfsRepository.save(dwarf);
        user.setState(UserState.AWAIT_SELECT_DWARF_ACTION);
        userCacheService.save(user);
        String text = "Имя гнома успешно сохранено. Хотите ещё что-то добавить или изменить?\n\n";
        text += getLastDwarf();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getSelectKeyboard());
        sendMessage.setParseMode("HTML");
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage setDescriptionMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        user.setState(UserState.AWAIT_DWARF_DESCRIPTION);
        userCacheService.save(user);
        String text = "Введите описание гнома.";
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage saveDescriptionMessage(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        String text = "Описание сохранено. Желаете ещё что-то добавить или изменить?\n\n";
        lastDwarf.setDescription(updateText);
        text += getLastDwarf();
        dwarfsRepository.save(lastDwarf);
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getSelectKeyboard());
        sendMessage.setParseMode("HTML");
        user.setState(UserState.AWAIT_SELECT_DWARF_ACTION);
        userCacheService.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage exitMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        String text = "Гном сохранен.\n\n";
        text += getLastDwarf();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("HTML");
        user.setState(UserState.SLEEP);
        userCacheService.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage editNameMessage(UserEntity user) {
        String text = "Введите новое имя для гнома";
        SendMessage sendMessage = getSendMessage(user, text);
        user.setState(UserState.AWAIT_EDIT_DWARF_NAME);
        userCacheService.save(user);
        return sendMessage;
    }

    public SendMessage saveEditNameMessage(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        String text = "Имя изменено. Желаете что-то ещё изменить?\n\n";
        text += getLastDwarf();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getSelectKeyboard());
        sendMessage.setParseMode("HTML");
        lastDwarf.setName(updateText);
        dwarfsRepository.save(lastDwarf);
        user.setState(UserState.AWAIT_SELECT_DWARF_ACTION);
        userCacheService.save(user);
        return sendMessage;
    }

    private SendMessage getSendMessage(UserEntity user, String text){
        String chatId = user.getUserChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage callbackQueryHandler(UserEntity user, String updateText) {
        switch (updateText) {
            case "set_name" -> {
                return editNameMessage(user);
            }
            case "set_desc" -> {
                return setDescriptionMessage(user);
            }
            case "cancel" -> {
                return exitMessage(user);
            }
            default -> {
                return defaultMessage(user);
            }
        }
    }

    private SendMessage defaultMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        String text = "Ответ мне не понятен. Попробуйте снова";
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getSelectKeyboard());
        return sendMessage;
    }

    public InlineKeyboardMarkup getSelectKeyboard() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("Изменить имя");
        button1.setCallbackData("set_name");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Добавить описание");
        button2.setCallbackData("set_desc");
        InlineKeyboardButton button3 = new InlineKeyboardButton("Выход");
        button3.setCallbackData("cancel");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button1, button2), List.of(button3)));
        return inlineKeyboardMarkup;
    }

    public String getLastDwarf() {
        String dwarf = "";
        if(lastDwarf == null) return dwarf;
        if(lastDwarf.getName() != null) {
            dwarf += "<b>Последние изменения:</b>\n" + "Имя гнома: " + lastDwarf.getName() + "\n";
        }
        if(lastDwarf.getDescription() != null) {
            dwarf += "Описание гнома: " + lastDwarf.getDescription();
        }
        return dwarf;
    }

}
