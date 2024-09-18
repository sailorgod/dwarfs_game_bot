package com.sailordev.dvorfsgamebot.telegram.handlers.user;

import com.sailordev.dvorfsgamebot.telegram.dto.*;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user.GetCoordinatesCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user.GetDwarfsCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user.GetHintsCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user.MessageToAdminCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.default_commands.StartCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;


@Component
@RequiredArgsConstructor
public class CommandsHandlerForUser implements CommandsHandler {

    private final StartCommand startCommand;
    private final MessageToAdminCommand messageToAdminCommand;
    private final GetDwarfsCommand getDwarfsCommand;
    private final GetHintsCommand getHintsCommand;
    private final GetCoordinatesCommand getCoordinatesCommand;

    @Override
    public HashMap<String, Command> getCommands() {
        HashMap<String, Command> commands = new HashMap<>();
        commands.put("/start", startCommand);
        commands.put("/message_to_admin", messageToAdminCommand);
        commands.put("/get_dwarfs", getDwarfsCommand);
        commands.put("/get_hints", getHintsCommand);
        commands.put("/get_coordinates", getCoordinatesCommand);
        return commands;
    }
}
