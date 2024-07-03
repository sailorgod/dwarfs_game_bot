package com.sailordev.dvorfsgamebot.telegram.handlers;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.configs.AdminUserProperty;
import com.sailordev.dvorfsgamebot.telegram.configs.BotProperties;
import com.sailordev.dvorfsgamebot.telegram.dto.CommandsHandler;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin.UserAwaitCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user.MessageToAdminCommand;
import com.sailordev.dvorfsgamebot.telegram.dto.keyboard.KeyboardForAdmin;
import com.sailordev.dvorfsgamebot.telegram.dto.keyboard.KeyboardForUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

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
    private final EventsHandler eventsHandler;
    private final UserAwaitCommand userAwaitCommand;
    private final KeyboardForAdmin keyboardForAdmin;
    private final KeyboardForUser keyboardForUser;
    private final StartHandler startHandler;
    private final MessageToAdminCommand messageToAdminCommand;
}
