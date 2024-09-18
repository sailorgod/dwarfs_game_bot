package com.sailordev.dvorfsgamebot.telegram.handlers.admin;

import com.sailordev.dvorfsgamebot.model.Coordinates;
import com.sailordev.dvorfsgamebot.model.Dwarf;
import com.sailordev.dvorfsgamebot.model.Hint;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.CoordinatesRepository;
import com.sailordev.dvorfsgamebot.repositories.DwarfsRepository;
import com.sailordev.dvorfsgamebot.repositories.HintsRepository;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class AddCoordinatesHandler {

    private final UserRepository userRepository;
    private final CoordinatesRepository coordinatesRepository;
    private final DwarfsRepository dwarfsRepository;
    private final HintsRepository hintsRepository;
    private static final String COORDINATE_REGEX
            = "^(-?[0-9]{1,3}(\\.[0-9]+)?),\\s*(-?[0-9]{1,3}(\\.[0-9]+)?)$";
    @Setter
    private Coordinates lastCoordinate;
    private int lastHintId;

    public SendMessage setCoordinates(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        if(!isValidCoordinate(updateText)) {
            text = "Введены невалидные координаты. Попробуйте снова";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        lastCoordinate = new Coordinates();
        lastCoordinate.setCoordinates(updateText);
        coordinatesRepository.save(lastCoordinate);
        text = "Координаты сохранены. Желаете добавить описание к координатам, подсказку" +
                " или выбрать тип гнома, " +
                "который находится в этих координатах?\n\n";
        text += getLastCoordinate();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getSelectKeyboard());
        sendMessage.setParseMode("HTML");
        BotLogger.info(text, chatId);
        user.setState(UserState.AWAIT_SELECT_ACTION_COORDINATE);
        userRepository.save(user);
        return sendMessage;
    }

    public SendMessage getSaveDescriptionMessage(UserEntity user, String updateText) {
        lastCoordinate.setDescription(updateText);
        coordinatesRepository.save(lastCoordinate);
        String text = "Описание сохранено. Желаете ещё что-то добавить или изменить?\n\n";
        text += getLastCoordinate();
        user.setState(UserState.AWAIT_SELECT_ACTION_COORDINATE);
        userRepository.save(user);
        return getMessage(user, text);
    }

    @Transactional
    public SendMessage getSaveDwarfMessage(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        if(!Character.isDigit(updateText.charAt(0))) {
            text = "Ожидаю получить номер гнома. Получено что-то другое. Попробуйте снова";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Optional<Dwarf> optionalDwarf = dwarfsRepository.findById(Integer.parseInt(updateText));
        if(optionalDwarf.isEmpty()) {
            text = "Гном с таким номером не найден. Попробуйте снова";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        Dwarf dwarf = optionalDwarf.get();
        List<Coordinates> coordinates = new ArrayList<>();
        if(dwarf.getCoordinates() != null) {
            coordinates = dwarf.getCoordinates();
        }
        coordinates.add(lastCoordinate);
        dwarf.setCoordinates(coordinates);
        lastCoordinate.setDwarf(dwarf);
        dwarfsRepository.save(dwarf);
        coordinatesRepository.save(lastCoordinate);
        text = "Тип гнома сохранен. Желаете ещё что-то добавить или изменить?\n\n";
        text += getLastCoordinate();
        user.setState(UserState.AWAIT_SELECT_ACTION_COORDINATE);
        userRepository.save(user);
        return getMessage(user, text);
    }

    private SendMessage getMessage(UserEntity user, String text) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(getSelectKeyboard());
        sendMessage.setParseMode("HTML");
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    public SendMessage getSetDescriptionMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "Введите инструкции к этим координатам";
        String chatId = user.getUserChatId();
        sendMessage.setText(text);
        sendMessage.setChatId(chatId);
        BotLogger.info(text, chatId);
        user.setState(UserState.AWAIT_ADD_COORDINATE_DESCRIPTION);
        userRepository.save(user);
        return sendMessage;
    }

    public SendMessage getDwarfSelectMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        Iterator<Dwarf> dwarfIterator = dwarfsRepository.findAll().iterator();
        if(!dwarfIterator.hasNext()) {
            text = "Типы гномов не найдены. Создайте нового гнома командой /create_dwarf и " +
            "продолжите заполнение данных координат командой /edit_coordinates";
            sendMessage.setText(text);
            BotLogger.info(text, chatId);
            return sendMessage;
        }
        text = "Выберите гнома из списка ниже:\n";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(text);
        List<Dwarf> dwarves = StreamSupport.stream(Spliterators.
                spliteratorUnknownSize(dwarfIterator, Spliterator.ORDERED), false).toList();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        dwarves.forEach(d -> {
            InlineKeyboardButton button = new InlineKeyboardButton();
            String dwarfId = d.getId().toString();
            stringBuilder.append(dwarfId).append(" - ").append(d.getName()).append("\n");
            button.setText(dwarfId);
            button.setCallbackData(dwarfId);
            buttons.add(button);
        });
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(buttons));
        sendMessage.setText(stringBuilder.toString());
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        BotLogger.info(stringBuilder.toString(), chatId);
        user.setState(UserState.AWAIT_SELECT_DWARF_FOR_ADDED);
        userRepository.save(user);
        return sendMessage;
    }

    public SendMessage callbackDataHandler(UserEntity user, String callback) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getUserChatId());
        switch (callback) {
            case "set_desc_coord" -> {
                return getSetDescriptionMessage(user);
            }
            case "set_dwarf_coord" -> {
                return getDwarfSelectMessage(user);
            }
            case "add_hint" -> {
                return addHintMessage(user);
            }
            case "cancel" -> {
                sendMessage.setText("Координаты сохранены");
                return sendMessage;
            }
            default -> {
                sendMessage.setText("Ответ мне не понятен, попробуйте снова");
                sendMessage.setReplyMarkup(getSelectKeyboard());
                return sendMessage;
            }
        }
    }

    private SendMessage addHintMessage(UserEntity user) {
        SendMessage sendMessage = new SendMessage();
        String text = "Введите подсказку:";
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        user.setState(UserState.AWAIT_ADD_COORDINATE_HINT);
        userRepository.save(user);
        BotLogger.info(text, chatId);
        return sendMessage;
    }

    @Transactional
    public SendMessage saveHintMessage(UserEntity user, String updateText) {
        SendMessage sendMessage = new SendMessage();
        String chatId = user.getUserChatId();
        sendMessage.setChatId(chatId);
        Hint hint = new Hint();
        hint.setHintDescription(updateText);
        hint.setCoordinate(lastCoordinate);
        List<Hint> hints = new ArrayList<>();
        if(lastCoordinate.getHints() != null) {
            hints = lastCoordinate.getHints();
        }
        hints.add(hint);
        lastCoordinate.setHints(hints);
        hintsRepository.save(hint);
        coordinatesRepository.save(lastCoordinate);
        user.setState(UserState.AWAIT_SELECT_ACTION_COORDINATE);
        userRepository.save(user);
        String text = "Подсказка для данной координаты сохранена:\n"
                + getLastCoordinate() +
                "\nЖелаете ещё что-то добавить или изменить?\n\n";
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(getSelectKeyboard());
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    public InlineKeyboardMarkup getSelectKeyboard() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("Изменить описание");
        button1.setCallbackData("set_desc_coord");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Выбрать тип гнома");
        button2.setCallbackData("set_dwarf_coord");
        InlineKeyboardButton button3  = new InlineKeyboardButton("Добавить подсказку");
        button3.setCallbackData("add_hint");
        InlineKeyboardButton button4 = new InlineKeyboardButton("Выход");
        button4.setCallbackData("cancel");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button1, button2),
                List.of(button3, button4)));
        return inlineKeyboardMarkup;
    }

    public String getLastCoordinate() {
        StringBuilder coordinate = new StringBuilder();
        if(lastCoordinate == null) return coordinate.toString();
        if(lastCoordinate.getCoordinates() != null) {
            coordinate.append("<b>Текущие изменения</b>:\n" + "Координаты: ").append(lastCoordinate.getCoordinates()).append("\n");
        }
        if(lastCoordinate.getDescription() != null) {
            coordinate.append("Описание координат: ").append(lastCoordinate.getDescription());
        }
        if(lastCoordinate.getDwarf() != null) {
            coordinate.append("\nГном: ").append(lastCoordinate.getDwarf().getName());
        }
        if(lastCoordinate.getHints() != null) {
            coordinate.append("\nПодсказки:\n");
            for (int i = 0; i < lastCoordinate.getHints().size(); i++) {
                coordinate.append("- ").
                        append(lastCoordinate.getHints().get(i).getHintDescription()).
                        append("\n");
            }
        }
        return coordinate.toString();
    }

    private boolean isValidCoordinate(String coordinate) {
        Pattern pattern = Pattern.compile(COORDINATE_REGEX);
        Matcher matcher = pattern.matcher(coordinate);
        return matcher.matches();
    }
}