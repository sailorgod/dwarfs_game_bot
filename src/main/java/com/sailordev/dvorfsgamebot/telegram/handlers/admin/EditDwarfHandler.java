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

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EditDwarfHandler {

    private final UserCacheService userCacheService;
    private final DwarfsRepository dwarfsRepository;
    private final CreateDwarfHandler createDwarfHandler;

    public SendMessage editDwarfActions(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        if(!Character.isDigit(updateText.charAt(0))){
            text = "Ожидаю в качестве ответа id гнома из списка. Попробуйте снова";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<Dwarf> dwarfOptional = dwarfsRepository.findById(Integer.parseInt(updateText));
        if(dwarfOptional.isEmpty()) {
            text = "Гном с таким id не найден. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        text = "Выберите, что вы хотите добавить или изменить";
        Dwarf dwarf = dwarfOptional.get();
        createDwarfHandler.setLastDwarf(dwarf);
        text += createDwarfHandler.getLastDwarf();
        dwarfsRepository.save(dwarf);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(createDwarfHandler.getSelectKeyboard());
        sendMessage.setParseMode("HTML");
        user.setState(UserState.AWAIT_SELECT_DWARF_ACTION);
        userCacheService.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage deleteDwarf(UserEntity user, String updateText){
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        if(!Character.isDigit(updateText.charAt(0))){
            text = "Ожидаю в качестве ответа id гнома из списка. Попробуйте снова";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<Dwarf> dwarfOptional = dwarfsRepository.findById(Integer.parseInt(updateText));
        if(dwarfOptional.isEmpty()) {
            text = "Гном с таким id не найден. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Dwarf dwarf = dwarfOptional.get();
        text = "Гном " + dwarf.getName() + " удален.";
        sendMessage.setText(text);
        dwarfsRepository.delete(dwarf);
        user.setState(UserState.SLEEP);
        userCacheService.save(user);
        return sendMessage;
    }
}
