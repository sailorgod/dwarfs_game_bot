package com.sailordev.dvorfsgamebot.repositories;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Integer> {
    Optional<UserEntity> findByUserChatId(String userChatId);
    Optional<UserEntity> findByUserName(String userName);
 }
