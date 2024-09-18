package com.sailordev.dvorfsgamebot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "dwarfs")
@Getter
public class Dwarf {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Setter
    private String name;
    @Setter
    @Column(columnDefinition = "varchar")
    private String description;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dwarf")
    @Setter
    private List<Coordinates> coordinates;
}
