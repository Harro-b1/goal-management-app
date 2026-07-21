package com.harro.goaltracker;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.java.Log;

@SpringBootApplication
@Log
public class GoaltrackerApplication implements CommandLineRunner{

	private final DataSource dataSource;

	public GoaltrackerApplication(final DataSource dataSource){this.dataSource = dataSource;}

	public static void main(String[] args) {
		SpringApplication.run(GoaltrackerApplication.class, args);
	}

	@Override
	public void run(final String... args){
		log.info("Datasource " + dataSource.toString());
		final JdbcTemplate restTemplate = new JdbcTemplate(dataSource);
		restTemplate.execute("select 1");
	}
	
}
