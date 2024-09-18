package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_user;

import com.sailordev.dvorfsgamebot.model.Coordinates;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.CoordinatesRepository;
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
public class GetCoordinatesCommand implements Command {

    private final CoordinatesRepository coordinatesRepository;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        String text = "";
        Iterator<Coordinates> coordinatesIterator = coordinatesRepository.findAll().iterator();
        if(!coordinatesIterator.hasNext()) {
            text = "В данный момент координаты гномов отсутсвуют. " +
                    "Дождитесь, пока администратор опубликует их.";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        List<Coordinates> coordinates = StreamSupport.stream(Spliterators
                .spliteratorUnknownSize(coordinatesIterator, Spliterator.ORDERED), false).toList();
        StringBuilder builder = new StringBuilder();
        builder.append("Координаты известных гномов:\n");
        coordinates.forEach(c -> {
            String desc = "Описание отсуствует. ";
            String dwarfName = "Гном неизвестен.";
            if(c.getDescription() != null) {
                desc = c.getDescription() + ". ";
            }
            if (c.getDwarf() != null) {
                dwarfName = "\nЗдесь обитает гном: " + c.getDwarf().getName();
            }
            String coordinate = "`" + c.getCoordinates() + "`";
            builder.append("\n\uD83D\uDCCD").append(coordinate).append(" - ").append(desc).
                    append(dwarfName).append("\n");
        });
        text = builder.toString();
        sendMessage.setText(text);
        sendMessage.setParseMode("MARKDOWN");
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Координаты";
    }

    @Override
    public String getDescription() {
        return "Если охота стартовала, показывает примерное местоположение всех известных гномов";
    }
}
