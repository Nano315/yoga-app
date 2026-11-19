package com.openclassrooms.starterjwt.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base des tests d'intégration : l'application démarre entièrement (Spring Boot)
 * avec une base H2 en mémoire (voir src/test/resources/application.yml).
 * La base est vidée avant chaque test pour que les tests soient indépendants.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    protected static final String PASSWORD = "test!1234";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TeacherRepository teacherRepository;

    @Autowired
    protected SessionRepository sessionRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        sessionRepository.deleteAll();
        teacherRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User createUser(String email, boolean admin) {
        return userRepository.save(new User(email, "Doe", "John", passwordEncoder.encode(PASSWORD), admin));
    }

    protected Teacher createTeacher(String firstName, String lastName) {
        return teacherRepository.save(Teacher.builder().firstName(firstName).lastName(lastName).build());
    }

    /** Se connecte via l'API et renvoie l'en-tête Authorization à utiliser. */
    protected String bearerTokenFor(String email) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", PASSWORD));
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return "Bearer " + json.get("token").asText();
    }
}
