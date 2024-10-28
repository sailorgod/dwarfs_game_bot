package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user;

import com.sailordev.dvorfsgamebot.model.Dwarf;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.DwarfsRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class GetDwarfsCommand implements Command {

    private final DwarfsRepository dwarfsRepository;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        String text = "";
        sendMessage.setChatId(chatId);
        Iterator<Dwarf> dwarfIterator = dwarfsRepository.findAll().iterator();
        if(!dwarfIterator.hasNext()) {
            text = "Пока не известно ни одного типа гномов. Может их ещё никто не нашёл?)";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        List<Dwarf> dwarves = StreamSupport.
                stream(Spliterators.spliteratorUnknownSize(dwarfIterator,
                        Spliterator.ORDERED), false).toList();
        StringBuilder builder = new StringBuilder();
        builder.append("Список известных гномов:\n");
        dwarves.forEach(d -> {
            String desc = ". Об этом гноме ничего не известно";
            if(d.getDescription() != null) {
                desc = "\nОписание гнома:\n" + d.getDescription();
            }
            builder.append("<b>\uD83E\uDDDD").append(" - ").
                    append(d.getName()).append("</b>").append(desc).append("\n");
        });
        text = builder.toString();
        sendMessage.setText(text);
        sendMessage.setParseMode("HTML");
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Гномы";
    }

    @Override
    public String getDescription() {
        return "Показывает разновидности гномов, их особенности и описание";
    }
}
