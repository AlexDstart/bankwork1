package com.skypro.simplebanking;

import com.skypro.simplebanking.initializer.Postgres;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
@ContextConfiguration(initializers = {Postgres.Initializer.class})
public abstract class IntegrationTestBase {

    public static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public static final String ADMIN_TOKEN = "SUPER_SECRET_KEY_FROM_ADMIN";
    public static final String ADMIN_HEADER = "X-SECURITY-ADMIN-KEY";
    public static final String USER_CREDENTIALS = "test_user:2236";
    public static final String HASHED_PASSWORD = passwordEncoder.encode("2236");

    @BeforeAll
    public static void init() {
        Postgres.container.start();
    }
}