package com.sailordev.dvorfsgamebot.repositories;

import com.sailordev.dvorfsgamebot.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    Optional<User> findByChatId(String chatId);
}
