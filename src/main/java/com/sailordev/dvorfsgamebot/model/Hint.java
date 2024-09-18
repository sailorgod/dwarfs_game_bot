package com.sailordev.dvorfsgamebot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "hints")
@Entity
@Getter
public class Hint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Setter
    private String hintDescription;
    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    private Coordinates coordinate;
}
