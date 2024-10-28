package com.sailordev.dvorfsgamebot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invites")
@Getter
public class Invite {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id")
    @Setter
    private UserEntity userId;
    @Column(name = "unique_url")
    @Setter
    private String uniqueUrl;
    @Column(name = "hints_count")
    @Setter
    private int hintsCount;
}
