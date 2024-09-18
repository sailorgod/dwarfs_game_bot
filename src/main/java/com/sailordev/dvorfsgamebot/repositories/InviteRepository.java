package com.sailordev.dvorfsgamebot.repositories;

import com.sailordev.dvorfsgamebot.model.Invite;
import com.sailordev.dvorfsgamebot.model.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface InviteRepository extends CrudRepository<Invite, Integer> {

    public Optional<Invite> findByUserId(UserEntity user);
}
