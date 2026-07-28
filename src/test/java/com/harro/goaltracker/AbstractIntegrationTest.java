package com.harro.goaltracker;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final MySQLContainer mysql = new MySQLContainer("mysql:8.0");

    static {
        // Singleton container pattern: started once for the whole JVM and never
        // explicitly stopped, since JUnit's @Container lifecycle would stop it
        // after each test class's afterAll, breaking it for the next class that
        // shares this static field. Ryuk reaps it when the JVM exits.
        mysql.start();
    }
}
