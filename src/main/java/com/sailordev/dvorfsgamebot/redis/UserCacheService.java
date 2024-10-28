package com.sailordev.dvorfsgamebot.redis;

import com.sailordev.dvorfsgamebot.model.UserEntity;
import com.sailordev.dvorfsgamebot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, UserEntity> redisUserTemplate;
    private final RedisTemplate<String, String> redisKeysTemplate;
    private static final String CHAT_ID_CACHE_PREFIX = "userChatId:";
    private static final String ID_CACHE_PREFIX = "id:";

    public Optional<UserEntity> findById(Integer id) {
        UserEntity user = redisUserTemplate.opsForValue().get(ID_CACHE_PREFIX + id);
        if(user != null) {
            user.setLastMessageTime(LocalDateTime.now());
            return Optional.of(user);
        }
        return userRepository.findById(id);
    }

    public Optional<UserEntity> findByUserChatId(String chatId) {
        String userId = redisKeysTemplate.opsForValue().get(CHAT_ID_CACHE_PREFIX + chatId);
        UserEntity user = redisUserTemplate.opsForValue().get(ID_CACHE_PREFIX + userId);
        if(user != null) {
            return Optional.of(user);
        }
        return userRepository.findByUserChatId(chatId);
    }

    public void save(UserEntity user){
        String userIdKey = ID_CACHE_PREFIX + user.getId();
        String chatIdKey = CHAT_ID_CACHE_PREFIX + user.getUserChatId();
        redisUserTemplate.opsForValue().
                set(userIdKey, user, 1, TimeUnit.MINUTES);
        redisKeysTemplate.opsForValue().
                set(chatIdKey, userIdKey, 1, TimeUnit.MINUTES);
        userRepository.save(user);
    }

    public Iterable<UserEntity> findAll() {
        return userRepository.findAll();
    }

}
