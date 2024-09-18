package com.sailordev.dvorfsgamebot.telegram.dto.commands_for_admin;

import com.sailordev.dvorfsgamebot.model.Dwarf;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.DwarfsRepository;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import com.sailordev.dvorfsgamebot.telegram.dto.BotLogger;
import com.sailordev.dvorfsgamebot.telegram.dto.Command;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import com.sailordev.dvorfsgamebot.telegram.handlers.admin.SelectDwarfsHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class DeleteDwarfCommand implements Command {

    private final SelectDwarfsHandler selectDwarfsHandler;
    
    @Override
    public SendMessage sendCommandMessage(UserEntity user) {
        return selectDwarfsHandler.getDwarfsMessage(user, UserState.AWAIT_SELECT_DELETE_DWARF);
    }

    @Override
    public String getName() {
        return "Удалить гнома";
    }

    @Override
    public String getDescription() {
        return "Удаляет ненужный тип гнома";
    }
}
