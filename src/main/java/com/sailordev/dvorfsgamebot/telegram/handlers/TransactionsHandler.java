package com.sailordev.dvorfsgamebot.telegram.handlers;

import com.sailordev.dvorfsgamebot.telegram.configs.AdminUserProperty;
import com.sailordev.dvorfsgamebot.telegram.configs.BotProperties;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin.AddCoordinatesCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin.EditCoordinatesCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin.EditDwarfCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin.UserAwaitCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user.MessageToAdminCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.keyboard.KeyboardForAdmin;
import com.sailordev.dvorfsgamebot.telegram.dto.keyboard.KeyboardForUser;
import com.sailordev.dvorfsgamebot.telegram.handlers.admin.*;
import com.sailordev.dvorfsgamebot.telegram.handlers.user.CommandsHandlerForUser;
import com.sailordev.dvorfsgamebot.telegram.handlers.user.InviteHandler;
import com.sailordev.dvorfsgamebot.telegram.handlers.user.MessageToAdminHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class TransactionsHandler {
    private final BotProperties botProperties;
    private final AdminUserProperty adminUserProperty;
    private final MessageToAdminHandler messageToAdminHandler;
    private final LastMessageHandler lastMessageHandler;
    private final CommandsHandlerForUser commandsHandlerForUser;
    private final CommandsHandlerForAdmin commandsHandlerForAdmin;
    private final CreateEventsHandler eventsHandler;
    private final UserAwaitCommand userAwaitCommand;
    private final KeyboardForAdmin keyboardForAdmin;
    private final KeyboardForUser keyboardForUser;
    private final StartHandler startHandler;
    private final MessageToAdminCommand messageToAdminCommand;
    private final EditEventHandler editEventHandler;
    private final StopEventHandler stopEventHandler;
    private final AddCoordinatesHandler addCoordinatesHandler;
    private final EditCoordinatesHandler editCoordinatesHandler;
    private final CreateDwarfHandler createDwarfHandler;
    private final EditDwarfHandler editDwarfHandler;
    private final HintsHandler hintsHandler;
    private final BanHandler banHandler;
    private final InviteHandler inviteHandler;
}
