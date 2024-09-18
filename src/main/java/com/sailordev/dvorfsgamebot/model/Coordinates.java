package com.sailordev.dvorfsgamebot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Setter
    private String coordinates;
    @Setter
    private String description;
    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dwarf_id")
    private Dwarf dwarf;
    @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "coordinate", fetch = FetchType.EAGER)
    private List<Hint> hints;
}
