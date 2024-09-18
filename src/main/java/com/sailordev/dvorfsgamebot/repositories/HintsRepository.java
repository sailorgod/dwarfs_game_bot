package com.sailordev.dvorfsgamebot.repositories;

import com.sailordev.dvorfsgamebot.model.Coordinates;
import com.sailordev.dvorfsgamebot.model.Hint;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface HintsRepository extends CrudRepository<Hint, Integer> {
    public Optional<Coordinates> findByCoordinate(Coordinates coordinate);
}
