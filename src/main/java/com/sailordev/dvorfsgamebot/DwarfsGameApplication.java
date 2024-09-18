package com.sailordev.dvorfsgamebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DwarfsGameApplication {

	public static void main(String[] args) {
		System.setProperty("proxy", "true");
		System.setProperty("http.proxyHost", "127.0.0.1");
		System.setProperty("http.proxyPort", "9050");
		SpringApplication.run(DwarfsGameApplication.class, args);
	}

}
