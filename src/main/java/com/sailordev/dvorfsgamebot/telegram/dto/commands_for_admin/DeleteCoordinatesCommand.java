package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class DeleteCoordinatesCommand implements Command {

    private final EditCoordinatesCommand editCoordinatesCommand;
    private final UserRepository userRepository;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        SendMessage sendMessage = editCoordinatesCommand.sendCommandMessage(user);
        user.setState(UserState.AWAIT_SELECT_DELETE_COORDINATE);
        userRepository.save(user);
        return sendMessage;
    }

    @Override
    public String getName() {
        return "Удалить координаты";
    }

    @Override
    public String getDescription() {
        return "Удаляет добавленные координаты";
    }
}
