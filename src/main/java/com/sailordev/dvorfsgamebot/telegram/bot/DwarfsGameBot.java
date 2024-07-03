package com.sailordev.dvorfsgamebot.telegram.bot;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.CommandsHandler;
import com.sailordev.dvorfsgamebot.telegram.handlers.*;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

;import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DwarfsGameBot extends TelegramLongPollingBot {

    private final TransactionsHandler transactionsHandler;
    private final UserRepository userRepository;
    private String lastUserId;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            UserEntity user = getUser(chatId, update.getMessage().getChat());
            if(user.getState().equals(UserState.BAN)) {
                return;
            }
            String updateText = update.getMessage().getText();
            log.info("{} : {}", user.getUserName(), updateText);
            update(updateText, update, user);
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            Chat chat = update.getCallbackQuery().getMessage().getChat();
            UserEntity user = getUser(chatId, chat);
            if(user.getState().equals(UserState.BAN)) {
                return;
            }
            update(data, update, user);
        }
        else {
            try {
                defaultMessage(update.getMessage().getText(),
                        update.getMessage().getChatId().toString());
            } catch (TelegramApiException ex) {
                log.error(ex.toString());
            }
        }
    }

    private void update(String updateText, Update update, UserEntity user) {
        CommandsHandler commandsHandler = null;
        if(user.getUserChatId().equals(transactionsHandler.getAdminUserProperty().getChatId())) {
            commandsHandler = transactionsHandler.getCommandsHandlerForAdmin();
        } else {
            commandsHandler = transactionsHandler.getCommandsHandlerForUser();
        }
        Optional<SendMessage> optionalSendMessage =
                commandsHandler.commandsTransaction(updateText, user);

        if(optionalSendMessage.isPresent()) {
            try {
                execute(optionalSendMessage.get());
                log.info(optionalSendMessage.get().getText());
                return;
            } catch (TelegramApiException ex) {
                log.error(ex.toString());
            }
        }
        try {
            states(updateText, update, user);
        } catch (TelegramApiException exception) {
            log.error(exception.toString());
        }
    }

    private void states(String updateText, Update update, UserEntity user) throws TelegramApiException {
        if(user.getUserChatId().equals(transactionsHandler.getAdminUserProperty().getChatId())) {
            adminStates(updateText, user, update);
            return;
        }
        switch (user.getState()){
            case AWAIT_START -> {
                if(update.hasCallbackQuery()) {
                    callbackQueryHandlerForUser(update, updateText, user);
                    return;
                }
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(user.getUserChatId());
                sendMessage.setText("Ответ не понятен мне, попробуйте снова");
                execute(sendMessage);
            }
            case AWAIT_MESSAGE_TO_ADMIN -> {
                sendMessagesToAdmin(updateText, user);
            }
            case AWAIT_ADMINS_RESPONSE -> {
                execute(transactionsHandler.getMessageToAdminHandler().sendAnswerToUser(user));
            }
            default -> defaultMessage(updateText, user.getUserChatId());
        }
    }

    private void adminStates(String updateText, UserEntity user, Update update) throws TelegramApiException {
        switch (user.getState()) {
            case AWAIT_SET_KEYBOARD -> {
                if(updateText.equals("/set_keyboard")) {
                    execute(transactionsHandler.getStartHandler().wellComeMessageForAdmin(user));
                    return;
                }
                //TODO: Исправить в дальнейшем на другое решение команды start
                user.setState(UserState.SLEEP);
                userRepository.save(user);
                onUpdateReceived(update);
            }
            case AWAIT_USER_SELECTION -> {
                execute(transactionsHandler.getLastMessageHandler()
                        .getLastUserMessage(updateText, user));
                lastUserId = updateText;
                if(update.hasCallbackQuery()) {
                    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                    String chatId = update.getCallbackQuery().getMessage().
                            getChatId().toString();
                    execute(new DeleteMessage(chatId, messageId));
                }
            }
            case AWAIT_SELECT_ACTION -> {
                if(update.hasCallbackQuery()) {
                    callbackQueryHandlerForAdmin(update, updateText, user);
                }
            }
            case AWAIT_RESPONSE_TO_USER -> {
                user.setState(UserState.SLEEP);
                userRepository.save(user);
                sendMessageToUser(updateText, user);
            }
            case AWAIT_SET_EVENT_DATE -> {
                execute(transactionsHandler.getEventsHandler().setEventDateTime(updateText, user));
            }
            default -> defaultMessage(updateText, user.getUserChatId());
        }
    }

    private void sendMessageToUser(String updateText, UserEntity user) throws TelegramApiException{
        execute(transactionsHandler.getMessageToAdminHandler().sendAnswerToAdmin());
        execute(transactionsHandler.getMessageToAdminHandler().sendMessageToUser(updateText, lastUserId, user));
    }

    private void sendMessagesToAdmin(String updateText, UserEntity user) throws TelegramApiException {
        user.setState(UserState.AWAIT_ADMINS_RESPONSE);
        user.setLastMessage(updateText);
        userRepository.save(user);
        execute(transactionsHandler.getMessageToAdminHandler().sendAnswerToUser(user));
        execute(transactionsHandler.getMessageToAdminHandler().
                sendMessageToAdmin(updateText, user));
    }

    private void callbackQueryHandlerForUser(Update update, String updateText, UserEntity user)
            throws TelegramApiException{
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String chatId = update.getCallbackQuery().getMessage().
                getChatId().toString();
        BotLogger.info(updateText);
        execute(deleteKeyboard(messageId, chatId));
        StartHandler startHandler = transactionsHandler.getStartHandler();
        switch (updateText) {
            case "YES_1" ->{
                execute(startHandler.wellComeMessage(user));
                execute(startHandler.getUniformMessage(user));
            }
            case "NO_1" -> execute(startHandler.getFirstWarning(user));
            case "YES_2" -> execute(startHandler.getSecondWarning(user));
            case "NO_2" -> execute(startHandler.getPositiveWarning(user));
            case "YES_3", "NO_3" -> {
                execute(startHandler.getBanMessage(updateText, user));
            }
            case "get_uniform" -> {
                sendMessagesToAdmin("Ожидаю получение униформы", user);
                user.setState(UserState.AWAIT_ADMINS_RESPONSE);
                userRepository.save(user);
            }
        }
    }

    private EditMessageReplyMarkup deleteKeyboard(Integer messageId, String chatId) {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setReplyMarkup(null);
        return editMessageReplyMarkup;
    }

    private void callbackQueryHandlerForAdmin(Update update, String updateText, UserEntity user)
            throws TelegramApiException{
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String chatId = update.getCallbackQuery().getMessage().
                getChatId().toString();
        switch (updateText) {
            case "back_to_users_select" -> {
                SendMessage sendMessage = transactionsHandler.getUserAwaitCommand()
                        .sendCommandMessage(user);
                execute(sendMessage);
                execute(new DeleteMessage(chatId, messageId));
                user.setState(UserState.AWAIT_USER_SELECTION);
                userRepository.save(user);
            }
            case "response_to_user" -> {
                execute(deleteKeyboard(messageId, chatId));
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Введите ответ на сообщение:");
                user.setState(UserState.AWAIT_RESPONSE_TO_USER);
                userRepository.save(user);
                execute(sendMessage);
            }
        }
    }

    private void defaultMessage(String updateText, String userId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        String text = "Команда не идентифицирована. " +
                "\nПосмотреть список доступных команд можно командой /help";
        sendMessage.setText(text);
        BotLogger.info(text,userId);
        sendMessage.setChatId(userId);
        execute(sendMessage);
    }

    private UserEntity getUser(String chatId, Chat chat) {
        Optional<UserEntity> optional = userRepository.findByUserChatId(chatId);
        if (optional.isPresent()){
            return optional.get();
        }
        UserEntity user = new UserEntity();
        String firstName = chat.getFirstName();
        String lastName = chat.getLastName();
        if(firstName == null) firstName = "";
        if(lastName == null) lastName = "";
        user.setUserName(firstName
                    + " " + lastName);
        user.setUserChatId(chatId);
        user.setState(UserState.AWAIT_REGISTRATION);
        userRepository.save(user);
        return user;
    }

    @Override
    public String getBotUsername() {
        return transactionsHandler.getBotProperties().getName();
    }

    @Override
    public String getBotToken() {
        return transactionsHandler.getBotProperties().getToken();
    }
}
