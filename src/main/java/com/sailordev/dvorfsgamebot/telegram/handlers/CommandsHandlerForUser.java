package com.sailordev.dvorfsgamebot.telegram.handlers;

import com.sailordev.dvorfsgamebot.telegram.dto.*;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user.MessageToAdminCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.default_commands.StartCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;


@Component
@RequiredArgsConstructor
public class CommandsHandlerForUser implements CommandsHandler {

    private final StartCommand startCommand;
    private final MessageToAdminCommand messageToAdminCommand;

    @Override
    public HashMap<String, Command> getCommands() {
        HashMap<String, Command> commands = new HashMap<>();
        commands.put("/start", startCommand);
        commands.put("/message_to_admin", messageToAdminCommand);
        return commands;
    }

}
