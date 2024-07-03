package com.sailordev.dvorfsgamebot.telegram.dto.keyboard;

import com.sailordev.dvorfsgamebot.telegram.dto.CommandsHandler;
import com.sailordev.dvorfsgamebot.telegram.dto.Keyboard;
import com.sailordev.dvorfsgamebot.telegram.handlers.CommandsHandlerForUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class KeyboardForUser implements Keyboard {

    private final CommandsHandlerForUser commandsHandlerForUser;

    @Override
    public CommandsHandler getCommandsHandler() {
        return commandsHandlerForUser;
    }
}
