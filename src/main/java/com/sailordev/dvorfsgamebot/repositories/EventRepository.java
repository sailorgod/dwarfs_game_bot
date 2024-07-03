package com.sailordev.dvorfsgamebot.repositories;

import com.sailordev.dvorfsgamebot.model.Event;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, Integer> {
}
