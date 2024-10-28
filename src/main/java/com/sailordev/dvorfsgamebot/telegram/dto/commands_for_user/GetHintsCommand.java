package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user;

import com.sailordev.dvorfsgamebot.model.Invite;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.redis.UserCacheService;
import com.sailordev.dvorfsgamebot.repositories.InviteRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GetHintsCommand implements Command {

    private final UserCacheService userCacheService;
    private final InviteRepository inviteRepository;

    @Transactional
    @Override

    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";
        Optional<Invite> optionalInvite = inviteRepository.findByUserId(user);
        if(optionalInvite.isEmpty()) {
            text = "Произошла ошибка при регистрации. Нажмите /start для ее исправления.";
            BotLogger.info(text, chatId);
            sendMessage.setText(text);
            return sendMessage;
        }
        Invite invite = optionalInvite.get();
        if(invite.getHintsCount() > 0) {
            text = "<b>Вам доступны подсказки: " + invite.getHintsCount() +
                    "</b>\nЖелаете получить подсказку?";
            InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkup();
            sendMessage.setText(text);
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            sendMessage.setParseMode("HTML");
            user.setState(UserState.AWAIT_SELECT_ACTION_HINT);
            userCacheService.save(user);
            return sendMessage;
        }
        text = "<b>Пригласи друга для получения подсказок.</b>\n" +
                "Сделать это можно, отправив ему уникальную ссылку:\n"
                + invite.getUniqueUrl() + "\n" +
                "После того, как друг перейдет по ссылке-приглашению, " +
                "вам автоматически начислится 1 подсказка. \n\n" +
                "Количество доступных подсказок: " + invite.getHintsCount() + "\n";
        sendMessage.setParseMode("HTML");
        sendMessage.setText(text);
        return sendMessage;
    }

    private static InlineKeyboardMarkup getInlineKeyboardMarkup() {
        InlineKeyboardButton button = new InlineKeyboardButton("Да");
        button.setCallbackData("yes");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Нет");
        button2.setCallbackData("no");
        InlineKeyboardButton button3 = new InlineKeyboardButton("Индивидуальная ссылка");
        button3.setCallbackData("get_link");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button, button2), List.of(button3)));
        return inlineKeyboardMarkup;
    }

    @Override
    public String getName() {
        return "Подсказки";
    }

    @Override
    public String getDescription() {
        return "Показывает подсказки по поиску гномов. Подсказки можно получить, пригласив " +
                "друзей в охотники. Для этого вам будет выдана уникальная ссылка-приглашение. " +
                "После того, как друг перейдет по вашей ссылке, вам автоматически начислится " +
                "одна подсказка.";
    }
}
