package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.telegram.dto.*;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin.*;
import com.sailordev.dvorfsgamebot.telegram.dto.default_commands.StartCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class CommandsHandlerForAdmin implements CommandsHandler {

    private final StartCommand startCommand;
    private final UserAwaitCommand userAwaitCommand;
    private final CreateEventCommand createEventCommand;
    private final EditEventsCommand editEventsCommand;
    private final StopEventCommand stopEventCommand;
    private final AddCoordinatesCommand addCoordinatesCommand;
    private final EditCoordinatesCommand editCoordinatesCommand;
    private final DeleteCoordinatesCommand deleteCoordinatesCommand;
    private final AddHintCommand addHintCommand;
    private final EditHintCommand editHintCommand;
    private final DeleteHintCommand deleteHintCommand;
    private final CreateDwarfCommand createDwarfCommand;
    private final EditDwarfCommand editDwarfCommand;
    private final DeleteDwarfCommand deleteDwarfCommand;
    private final BanCommand banCommand;
    private final UnblockCommand unblockCommand;

    @Override
    public HashMap<String, Command> getCommands() {
        HashMap<String, Command> commands = new HashMap<>();
        commands.put("/start", startCommand);
        commands.put("/users_awaits", userAwaitCommand);
        commands.put("/create_event", createEventCommand);
        commands.put("/edit_event", editEventsCommand);
        commands.put("/stop_event", stopEventCommand);
        commands.put("/add_coordinates", addCoordinatesCommand);
        commands.put("/edit_coordinates", editCoordinatesCommand);
        commands.put("/delete_coordinates", deleteCoordinatesCommand);
        commands.put("/add_hint", addHintCommand);
        commands.put("/edit_hint", editHintCommand);
        commands.put("/delete_hint", deleteHintCommand);
        commands.put("/create_dwarf", createDwarfCommand);
        commands.put("/edit_dwarf", editDwarfCommand);
        commands.put("/delete_dwarf", deleteDwarfCommand);
        commands.put("/ban", banCommand);
        commands.put("/unblock", unblockCommand);
        return commands;
    }
}
