package com.sailordev.dvorfsgamebot.telegram.dto.default_commands;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Map;

public class HelpCommand {

    public static SendMessage getCommands(Map<String, Command> commands, UserEntity user) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Список доступных команд:</b>");
        for (Map.Entry<String, Command> command : commands.entrySet()) {
            stringBuilder.append("\n- ").
                    append(command.getKey()).
                    append(" - ").
                    append(command.getValue().getDescription());
        }
        stringBuilder.append("\n- /help - Список всех команд");
        String text = stringBuilder.toString();
        String chatId = user.getUserChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");
        BotLogger.info(text, chatId);
        return sendMessage;
    }
}
