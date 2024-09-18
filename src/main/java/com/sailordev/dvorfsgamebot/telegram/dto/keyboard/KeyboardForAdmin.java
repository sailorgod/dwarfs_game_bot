package com.sailordev.dvorfsgamebot.telegram.dto.keyboard;

import com.sailordev.dvorfsgamebot.telegram.dto.CommandsHandler;
import com.sailordev.dvorfsgamebot.telegram.dto.Keyboard;
import com.sailordev.dvorfsgamebot.telegram.handlers.admin.CommandsHandlerForAdmin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeyboardForAdmin implements Keyboard {

    private final CommandsHandlerForAdmin commandsHandlerForAdmin;

    @Override
    public CommandsHandler getCommandsHandler() {
        return commandsHandlerForAdmin;
    }
}
