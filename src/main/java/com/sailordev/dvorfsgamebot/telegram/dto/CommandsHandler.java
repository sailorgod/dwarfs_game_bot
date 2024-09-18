package com.sailordev.dvorfsgamebot.telegram.dto;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.telegram.dto.default_commands.HelpCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.*;

public interface CommandsHandler {

    HashMap<String, Command> getCommands();

    default Optional<SendMessage> commandsTransaction(String updateText, UserEntity user) {
        HashMap<String, Command> commands = getCommands();
        String command = updateText.trim().toLowerCase();
        if(command.equals("/help")) {
            return Optional.of(HelpCommand.getCommands(commands, user));
        }
        if(commands.containsKey(command)) {
            return Optional.of(commands.get(command).sendCommandMessage(user));
        }
        Collection<Command> commandsCollection = commands.values();
        List<Command> commandList = new ArrayList<>(commandsCollection.stream().toList());
        commandList.sort(Comparator.comparing(Command::getName));
        for(Command c : commandList) {
            if(updateText.equals(c.getName()) && !c.getName().equals("Старт")) {
                return Optional.of(c.sendCommandMessage(user));
            }
        }
        return Optional.empty();
    };
}
