package com.sailordev.dvorfsgamebot.telegram.handlers;

import com.sailordev.dvorfsgamebot.telegram.dto.*;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin.CreateEventCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin.UserAwaitCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.default_commands.StartCommand;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class CommandsHandlerForAdmin implements CommandsHandler {

    private final StartCommand startCommand;
    private final UserAwaitCommand userAwaitCommand;
    private final CreateEventCommand createEventCommand;

    @Override
    public HashMap<String, Command> getCommands() {
        HashMap<String, Command> commands = new HashMap<>();
        commands.put("/start", startCommand);
        commands.put("/users_awaits", userAwaitCommand);
        commands.put("/create_event", createEventCommand);
        return commands;
    }
}
