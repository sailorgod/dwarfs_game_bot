package com.sailordev.dvorfsgamebot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    @Column(name = "event_date_time", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime eventDateTime;
}
