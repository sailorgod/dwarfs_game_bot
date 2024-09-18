package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import com.sailordev.dvorfsgamebot.telegram.handlers.admin.SelectDwarfsHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class EditDwarfCommand implements Command {

    private final SelectDwarfsHandler selectDwarfsHandler;

    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        return selectDwarfsHandler.getDwarfsMessage(user, UserState.AWAIT_SELECT_EDIT_DWARF);
    }

    @Override
    public String getName() {
        return "Редактировать гнома";
    }

    @Override
    public String getDescription() {
        return "Отредактировать гнома из списка";
    }
}
