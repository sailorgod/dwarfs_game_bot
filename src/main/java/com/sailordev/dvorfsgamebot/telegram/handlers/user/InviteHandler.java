package com.sailordev.dvorfsgamebot.telegram.handlers.user;

import com.sailordev.dvorfsgamebot.model.Coordinates;
import com.sailordev.dvorfsgamebot.model.Hint;
import com.sailordev.dvorfsgamebot.model.Invite;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.repositories.CoordinatesRepository;
import com.sailordev.dvorfsgamebot.repositories.InviteRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import com.sailordev.dvorfsgamebot.telegram.dto.keyboard.KeyboardForUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class InviteHandler {

    private final UserCacheService userCacheService;
    private final InviteRepository inviteRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final KeyboardForUser keyboardForUser;
    private UserEntity lastUser;
    private static final String BOT_URL = "t.me/dworfsgamebot?start=";
    @Getter
    private boolean isFalseUser;

    public Invite createInvite(UserEntity user) {
        Invite invite = new Invite();
        invite.setUserId(user);
        String uniqueUrl = BOT_URL + user.getUserChatId();
        invite.setUniqueUrl(uniqueUrl);
        invite.setHintsCount(0);
        inviteRepository.save(invite);
        return invite;
    }

    public SendMessage setHintToUser(UserEntity user, String updateText) {
        isFalseUser = false;
        SendMessage sendMessage = new SendMessage();
        String chatId = updateText.replace("/start", "").trim();
        String text = "";
        Optional<UserEntity> userFalseOptional = userCacheService.findByUserChatId(user.getUserChatId());
        if(userFalseOptional.isPresent()) {
            isFalseUser = true;
            text = "Подсказки можно получить только за новых пользователей ;)";
            chatId = user.getUserChatId();
            sendMessage.setText(text);
            sendMessage.setChatId(chatId);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<UserEntity> userOptional = userCacheService.findByUserChatId(chatId);
        if(userOptional.isEmpty()) {
            chatId = user.getUserChatId();
            text = "Прошу прощения, у меня не вышло найти пользователя, который вас пригласил. " +
                    "Возможно, вам отправили некорректную ссылку.";
            sendMessage.setText(text);
            sendMessage.setChatId(chatId);
            user.setState(UserState.SLEEP);
            userCacheService.save(user);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        if(chatId.equals(user.getUserChatId())) {
            text = "За приглашение самого себя подсказки не начисляются ;)";
            sendMessage.setChatId(chatId);
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        UserEntity user1 = userOptional.get();
        Invite invite = user1.getInvite();
        invite.setHintsCount(invite.getHintsCount() + 1);
        inviteRepository.save(invite);
        user1.setInvite(invite);
        userCacheService.save(user1);
        userCacheService.save(user);
        lastUser = user1;
        text = "Вам начислена 1 подсказка за приглашение.";
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage sendWellComeMessageAfterInvite(UserEntity user){
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "<b>Вас пригласил " + lastUser.getUserName() + ". Добро пожаловать!</b>";
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(keyboardForUser.getKeyboard());
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    public SendMessage selectCoordinates(String callbackData, UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";
        if(callbackData.contains("yes")) {
            Iterator<Coordinates> coordinatesIterator = coordinatesRepository.findAll().iterator();
            if(!coordinatesIterator.hasNext()) {
                text = "Приношу извинения, но на данный момент администратор не сохранил ни " +
                        "одной координаты и подсказки.\nДождитесь, пока администратор" +
                        "объявит начало охоты и сохранит подсказки.";
                sendMessage.setText(text);
                BotLogger.info(text, chatId);
                user.setState(UserState.SLEEP);
                userCacheService.save(user);
                return sendMessage;
            }
            List<Coordinates> coordinates = StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(
                            coordinatesIterator, Spliterator.ORDERED), false).toList();
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            StringBuilder builder = new StringBuilder();
            builder.append("Выберите номер координаты, по которой вы хотите получить подсказку:\n");
            coordinates.forEach(c -> {
                String id = c.getId().toString();
                builder.append(id).append(" - ").append(c.getCoordinates()).append("\n");
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(id);
                inlineKeyboardButton.setCallbackData(id);
                buttons.add(inlineKeyboardButton);
            });
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(List.of(buttons));
            text = builder.toString();
            sendMessage.setText(text);
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            user.setState(UserState.AWAIT_USER_SELECT_HINT);
            userCacheService.save(user);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        if(callbackData.contains("no")) {
            text = "Отменено.";
            sendMessage.setText(text);
            user.setState(UserState.SLEEP);
            userCacheService.save(user);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        if(callbackData.contains("get_link")) {
            text = "Ваша индивидуальная ссылка для приглашения:\n" + user.getInvite().getUniqueUrl();
            sendMessage.setText(text);
            user.setState(UserState.SLEEP);
            userCacheService.save(user);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        text = "Команда мне не понятна. Повторите это действие позже.";
        user.setState(UserState.SLEEP);
        userCacheService.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage selectHint(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";
        if(!Character.isDigit(updateText.charAt(0))) {
            text = "Ожидаю в качестве ответа номер координаты из списка. Попробуйте снова.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<Coordinates> coordinatesOptional
                = coordinatesRepository.findById(Integer.parseInt(updateText));
        if(coordinatesOptional.isEmpty()) {
            text = "Координата с таким номером не найдена. " +
                    "Введите номер координаты из списка выше.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Coordinates coordinates = coordinatesOptional.get();
        if(coordinates.getHints() == null) {
            text = "У данной координаты отсутствуют подсказки. Выберите другую " +
                    "координату и введите ее номер в сообщении ниже:";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        List<Hint> hints = coordinates.getHints();
        Random random = new Random();
        text = "Подсказка для данной координаты:\n"
                + hints.get(random.nextInt(0, hints.size())).getHintDescription();
        sendMessage.setText(text);
        Invite invite = user.getInvite();
        int hintCount = user.getInvite().getHintsCount();
        invite.setHintsCount(--hintCount);
        inviteRepository.save(invite);
        user.setInvite(invite);
        user.setState(UserState.SLEEP);
        userCacheService.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }
}
