package com.sailordev.dvorfsgamebot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sailordev.dvorfsgamebot.telegram.dto.UserState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    @Column(name = "name")
    private String userName;
    @Setter
    @Column(name = "chat_id", unique = true)
    private String userChatId;
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserState state;
    @Setter
    @Column(name = "last_message")
    private String lastMessage;
    @Setter
    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;
    @Setter
    @Column(name = "warn_count")
    private int warnCount;
    @JsonIgnore
    @OneToOne
    @Setter
    private Invite invite;
}
