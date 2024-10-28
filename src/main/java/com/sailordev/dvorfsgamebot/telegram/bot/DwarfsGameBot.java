package com.sailordev.dvorfsgamebot.telegram.bot;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.telegram.dto.*;
import com.sailordev.dvorfsgamebot.telegram.handlers.*;
import lombok.Getter;
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


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Slf4j
@Component
@RequiredArgsConstructor
public class DwarfsGameBot extends TelegramLongPollingBot {

    @Getter
    private final TransactionsHandler transactionsHandler;
    private final UserCacheService userCacheService;
    private final ScheduledExecutorService scheduledExecutorService
            = Executors.newScheduledThreadPool(1);
    private Interval interval;
    private String notificationDuration;
    private LocalDateTime startEventDate;
    private String lastUserId;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            String updateText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();
            UserEntity user = getUser(chatId, update.getMessage().getChat());
            if(user.getState().equals(UserState.BAN)) {
                return;
            }
            if(LocalDateTime.now().isAfter(
                    user.getLastMessageTime().plusSeconds(2))){
                try{
                    if(user.getWarnCount() == 4) {
                        user.setState(UserState.BAN);
                        userCacheService.save(user);
                        execute(transactionsHandler
                                .getBanHandler().sendMessageBanUserAfterWarn(user));
                        return;
                    }
                    user.setWarnCount(user.getWarnCount() + 1);
                    userCacheService.save(user);
                    execute(transactionsHandler.getBanHandler().sendBanWarning(user));
                    return;
                } catch (TelegramApiException e) {
                    log.error(e.toString());
                }
            }
            if(updateText != null && updateText.startsWith("/start") && updateText
                    .replace("/start", "").trim().matches("[0-9]+")){
                try {
                    execute(transactionsHandler.
                            getInviteHandler().setHintToUser(user, updateText));
                    if(!transactionsHandler.getInviteHandler().isFalseUser()) {
                        execute(transactionsHandler.
                                getInviteHandler().sendWellComeMessageAfterInvite(user));
                    }
                    return;
                } catch (TelegramApiException e) {
                    log.error(e.toString());
                    throw new RuntimeException(e);
                }
            }
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
        if(updateText != null) {
            //TODO: Пофиксить команду старт так, чтобы не возникало повторного создания юзера
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
        }
        try {
            states(updateText, update, user);
        } catch (TelegramApiException exception) {
            log.error(exception.toString());
        }
    }

    private void states(String updateText, Update update, UserEntity user)
            throws TelegramApiException {
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
            case AWAIT_SELECT_ACTION_HINT -> {
                if(update.hasCallbackQuery()){
                    deleteKeyboardHasCallbackQuery(update, user);
                    execute(transactionsHandler.getInviteHandler()
                            .selectCoordinates(update.getCallbackQuery().getData(), user));
                }
            }
            case AWAIT_USER_SELECT_HINT -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.getInviteHandler().
                        selectHint(user, updateText));
            }

            default -> defaultMessage(updateText, user.getUserChatId());
        }
    }

    private void adminStates(String updateText, UserEntity user, Update update)
            throws TelegramApiException {
        SendMessage unknown = new SendMessage();
        unknown.setChatId(user.getUserChatId());
        unknown.setText("Ваш ответ не понятен. Выберите одну из кнопок в моем сообщении выше");
        switch (user.getState()) {
            case SLEEP -> {
                if(update.hasCallbackQuery()
                        && update.getCallbackQuery().getData().contains("answer_")) {
                    deleteKeyboardHasCallbackQuery(update, user);
                    lastUserId = update
                            .getCallbackQuery().getData().replace("answer_", "");
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(transactionsHandler.
                            getAdminUserProperty().getChatId());
                    sendMessage.setText("Введите ответ на сообщение:");
                    user.setState(UserState.AWAIT_RESPONSE_TO_USER);
                    userCacheService.save(user);
                    execute(sendMessage);
                }
            }
            case AWAIT_SET_KEYBOARD -> {
                if(updateText.equals("/set_keyboard")) {
                    execute(transactionsHandler.getStartHandler().wellComeMessageForAdmin(user));
                    return;
                }
                //TODO: Исправить в дальнейшем на другое решение команды start
                user.setState(UserState.SLEEP);
                userCacheService.save(user);
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
            case AWAIT_SELECT_ACTION, AWAIT_SET_NAME_OR_DESCRIPTION_EVENT -> {
                if(update.hasCallbackQuery()) {
                    callbackQueryHandlerForAdmin(update, updateText, user);
                    return;
                }
                execute(unknown);
            }
            case AWAIT_RESPONSE_TO_USER -> {
                user.setState(UserState.SLEEP);
                userCacheService.save(user);
                sendMessageToUser(updateText, user);
            }
            case AWAIT_SET_EVENT_DATE -> {
                execute(transactionsHandler.getEventsHandler().setEventDateTime(updateText, user));
            }

            case AWAIT_EVENT_SET_NAME -> {
                execute(transactionsHandler.getEventsHandler().saveEventName(updateText, user));
            }
            case AWAIT_EVENT_SET_DESCRIPTION -> {
                execute(transactionsHandler.getEventsHandler().saveEventDescription(user, updateText));
                execute(transactionsHandler.getEventsHandler().saveEvent(user));
                startEventNotifications();
            }
            case AWAIT_SET_NOTIFICATION_TIME -> {
                if(update.hasCallbackQuery()) {
                    firstNotificationHandler(updateText, user, update);
                    return;
                }
                execute(unknown);
            }
            case AWAIT_FIRST_SELECT_EDIT_EVENT -> {
                if (update.hasCallbackQuery()) {
                    execute(deleteKeyboard(
                            update.getCallbackQuery().getMessage().getMessageId(), user.getUserChatId()));
                    execute(transactionsHandler.getEditEventHandler().selectEvent(user, updateText));
                    return;
                }
                execute(unknown);
            }
            case AWAIT_SELECT_EDIT_EVENT -> {
                if(update.hasCallbackQuery()) {
                    execute(deleteKeyboard(
                            update.getCallbackQuery().getMessage().getMessageId(), user.getUserChatId()));
                    execute(transactionsHandler.getEditEventHandler().getEditHandler(user, updateText));
                    return;
                }
                execute(unknown);
            }
            case AWAIT_EDIT_EVENT_NAME -> {
                execute(transactionsHandler.getEditEventHandler().editEventName(updateText, user));
            }
            case AWAIT_EDIT_EVENT_DESCRIPTION -> {
                execute(transactionsHandler.getEditEventHandler().editEventDescription(updateText, user));
            }
            case AWAIT_EDIT_EVENT_DATE -> {
                execute(transactionsHandler.getEditEventHandler().editEventDate(updateText, user));
                LocalDateTime actualDate = transactionsHandler
                        .getEditEventHandler().getLastEvent().getEventDateTime();
                if(!actualDate.equals(startEventDate)) {
                    startEventDate = actualDate;
                    transactionsHandler.getEventsHandler().setEventDateTime(updateText, user);
                    editEventNotifications();
                }
            }
            case AWAIT_SELECT_DELETE_EVENT -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.getStopEventHandler().stopEvent(user, updateText));
                stopEventNotifications();
            }
            case AWAIT_ADD_COORDINATES -> {
                execute(transactionsHandler.
                        getAddCoordinatesHandler().setCoordinates(user, updateText));
            }
            case AWAIT_SELECT_ACTION_COORDINATE -> {
                if(update.hasCallbackQuery()) {
                    deleteKeyboardHasCallbackQuery(update, user);
                    execute(transactionsHandler.
                            getAddCoordinatesHandler().callbackDataHandler(user, updateText));
                }
            }
            case AWAIT_ADD_COORDINATE_DESCRIPTION -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getAddCoordinatesHandler().getSaveDescriptionMessage(user, updateText));
            }
            case AWAIT_SELECT_DWARF_FOR_ADDED -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.getAddCoordinatesHandler()
                        .getSaveDwarfMessage(user, updateText));
            }
            case AWAIT_SELECT_EDIT_COORDINATE -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.getEditCoordinatesHandler().
                        selectCoordinate(user, updateText));
            }
            case AWAIT_SELECT_DELETE_COORDINATE -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getEditCoordinatesHandler().deleteCoordinate(user, updateText));
            }
            case AWAIT_SET_DWARF_NAME -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.getCreateDwarfHandler().saveNameMessage(user, updateText));
            }
            case AWAIT_DWARF_DESCRIPTION -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getCreateDwarfHandler().saveDescriptionMessage(user, updateText));
            }
            case AWAIT_EDIT_DWARF_NAME -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getCreateDwarfHandler().saveEditNameMessage(user, updateText));
            }
            case AWAIT_SELECT_DWARF_ACTION -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getCreateDwarfHandler().callbackQueryHandler(user, updateText));
            }
            case AWAIT_SELECT_EDIT_DWARF -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getEditDwarfHandler().editDwarfActions(user, updateText));
            }
            case AWAIT_SELECT_DELETE_DWARF -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getEditDwarfHandler().deleteDwarf(user, updateText));
            }
            case AWAIT_SELECT_COORDINATE_FOR_ADD_HINT -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getHintsHandler().addHintMessage(user, updateText));
            }
            case AWAIT_ADD_HINT -> {
                execute(transactionsHandler.
                        getHintsHandler().saveHintMessage(user, updateText));
            }
            case AWAIT_SELECT_EDIT_HINT -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getHintsHandler().selectEditHintMessage(user, updateText));
            }
            case AWAIT_EDIT_HINT -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getHintsHandler().editHint(user, updateText));
            }
            case AWAIT_DELETE_HINT -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.
                        getHintsHandler().deleteHint(user, updateText));
            }
            case AWAIT_SELECT_USER_BAN -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.getBanHandler().banUserMessage(user, updateText));
                execute(transactionsHandler.getBanHandler().sendMessageBanUser());
            }
            case AWAIT_SELECT_UNBLOCK_USER -> {
                deleteKeyboardHasCallbackQuery(update, user);
                execute(transactionsHandler.getBanHandler().unblockUserMessage(user, updateText));
                execute(transactionsHandler.getBanHandler().sendMessageBanUser());
            }
            case AWAIT_ADD_COORDINATE_HINT ->  {
                execute(transactionsHandler.
                        getAddCoordinatesHandler().saveHintMessage(user, updateText));
            }

            default -> defaultMessage(updateText, user.getUserChatId());
        }
    }

    private void deleteKeyboardHasCallbackQuery(Update update, UserEntity user)
    throws TelegramApiException {
        if(update.hasCallbackQuery()) {
            execute(deleteKeyboard(update.getCallbackQuery().getMessage().getMessageId(),
                    user.getUserChatId()));
        }
    }

    private void sendMessageToUser(String updateText, UserEntity user)
            throws TelegramApiException{
        execute(transactionsHandler.getMessageToAdminHandler().sendAnswerToAdmin());
        execute(transactionsHandler.
                getMessageToAdminHandler().sendMessageToUser(updateText, lastUserId, user));
    }

    private void sendMessagesToAdmin(String updateText, UserEntity user) throws TelegramApiException {
        user.setState(UserState.AWAIT_ADMINS_RESPONSE);
        user.setLastMessage(updateText);
        userCacheService.save(user);
        execute(transactionsHandler.getMessageToAdminHandler().sendAnswerToUser(user));
        execute(transactionsHandler.getMessageToAdminHandler().
                sendMessageToAdmin(updateText, user));
    }

    private void callbackQueryHandlerForUser(Update update, String updateText, UserEntity user)
            throws TelegramApiException{
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String chatId = update.getCallbackQuery().getMessage().
                getChatId().toString();
        BotLogger.info(updateText, user.getUserChatId());
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
                execute(startHandler.getBanMessage(user));
            }
            case "get_uniform" -> {
                sendMessagesToAdmin("Ожидаю получение униформы", user);
                user.setState(UserState.AWAIT_ADMINS_RESPONSE);
                userCacheService.save(user);
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
        execute(deleteKeyboard(messageId, chatId));
        switch (updateText) {
            case "back_to_users_select" -> {
                SendMessage sendMessage = transactionsHandler.getUserAwaitCommand()
                        .sendCommandMessage(user);
                execute(sendMessage);
                execute(new DeleteMessage(chatId, messageId));
                user.setState(UserState.AWAIT_USER_SELECTION);
                userCacheService.save(user);
            }
            case "response_to_user" -> {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Введите ответ на сообщение:");
                user.setState(UserState.AWAIT_RESPONSE_TO_USER);
                userCacheService.save(user);
                execute(sendMessage);
            }
            case "set_event_name" -> {
                execute(transactionsHandler.getEventsHandler().eventNameSetter(user));
            }
            case "set_event_disc" -> {
                execute(transactionsHandler.getEventsHandler().eventDescriptionSetter(user));
            }
            case "cancel_name" -> {
                execute(transactionsHandler.getEventsHandler().saveEvent(user));
                startEventNotifications();
            }
        }
    }

    private void startEventNotifications() {
        List<UserEntity> users = getUsersList();
        users.forEach(u -> {
            try {
                execute(transactionsHandler.getEventsHandler().startEventNotifications(u));
            } catch (TelegramApiException e) {
                log.error(e.toString());
                throw new RuntimeException(e);
            }
        });
    }

    private void editEventNotifications() {
        List<UserEntity> users = getUsersList();
        users.forEach(u -> {
            try {
                execute(transactionsHandler.getEditEventHandler().getEditNotification(u));
            } catch (TelegramApiException ex) {
                log.error(ex.toString());
            }
        });
    }

    private void stopEventNotifications() {
        List<UserEntity> users = getUsersList();
        users.forEach(u -> {
            try {
                execute(transactionsHandler.getStopEventHandler().stopEventNotification(u));
            } catch (TelegramApiException e) {
                log.error(e.toString());
                throw new RuntimeException(e);
            }
        });
    }

    private void notificationScheduler() {
        List<UserEntity> users = getUsersList();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if(LocalDateTime.now().isAfter(startEventDate)) {
                if(!scheduledExecutorService.isShutdown()) {
                    scheduledExecutorService.shutdown();
                }
                users.forEach(u -> {
                    try {
                        execute(transactionsHandler.getEventsHandler().getStartEventNotification(u));
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
                return;
            }
            users.forEach(u -> {
                try {
                    SendMessage sendMessage = transactionsHandler.getEventsHandler().getNotification(u);
                    execute(sendMessage);
                    BotLogger.info( sendMessage.getText(), u.getUserChatId() );
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                interval = notificationsInterval();
            });
        }, 0, interval.period(), interval.timeUnit());
    }

    private List<UserEntity> getUsersList() {
        Iterator<UserEntity> usersIterator = userCacheService.findAll().iterator();
        return StreamSupport.
                stream(Spliterators.
                        spliteratorUnknownSize(usersIterator, Spliterator.ORDERED), false).toList();
    }

    private void firstNotificationHandler(String updateText, UserEntity user, Update update)
            throws TelegramApiException {
        execute(deleteKeyboard(update.getCallbackQuery().getMessage().getMessageId(), user.getUserChatId()));
        notificationDuration = updateText;
        startEventDate = transactionsHandler.getEventsHandler().getLastEvent().getEventDateTime();
        interval = notificationsInterval();
        execute(transactionsHandler.getEventsHandler().saveNotifications(user));
        notificationScheduler();
    }

    public Interval notificationsInterval() {
        switch (notificationDuration) {
            case "one_day" -> {
                return new Interval(TimeUnit.DAYS, 1);
            }
            case "two_days" -> {
                return new Interval(TimeUnit.DAYS, 2);
            }
            case "three_days" -> {
                return new Interval(TimeUnit.DAYS, 3);
            }
            case "week" -> {
                return new Interval(TimeUnit.DAYS, 7);
            }
            case "two_week" -> {
                return new Interval(TimeUnit.DAYS, 14);
            }
            default -> {
                return new Interval(TimeUnit.DAYS, 10);
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
        Optional<UserEntity> optional = userCacheService.findByUserChatId(chatId);
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
        user.setLastMessageTime(LocalDateTime.now());
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
