package com.harro.goaltracker;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import dev.langchain4j.model.chat.ChatModel;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

@SpringBootApplication
@Log
public class GoaltrackerApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(GoaltrackerApplication.class, args);
	}

	@Override
	public void run(final String... args){
		
	}
}
