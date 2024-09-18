package com.sailordev.dvorfsgamebot.repositories;

import com.sailordev.dvorfsgamebot.model.Dwarf;
import org.springframework.data.repository.CrudRepository;

public interface DwarfsRepository extends CrudRepository<Dwarf, Integer> {
}
