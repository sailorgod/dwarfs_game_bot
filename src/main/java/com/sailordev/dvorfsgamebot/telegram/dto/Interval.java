package com.sailordev.dvorfsgamebot.telegram.dto;

import lombok.Data;

import java.util.concurrent.TimeUnit;

public record Interval(TimeUnit timeUnit, int period) {
}
