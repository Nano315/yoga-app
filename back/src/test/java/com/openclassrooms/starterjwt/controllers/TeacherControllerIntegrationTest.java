package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.Teacher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeacherControllerIntegrationTest extends AbstractIntegrationTest {

    private String token;

    @BeforeEach
    void login() throws Exception {
        createUser("user@test.com", false);
        token = bearerTokenFor("user@test.com");
    }

    @Test
    @DisplayName("GET /api/teacher : renvoie la liste des professeurs")
    void findAll_shouldReturnTeachers() throws Exception {
        createTeacher("Margot", "DELAHAYE");
        createTeacher("Hélène", "THIERCELIN");

        mockMvc.perform(get("/api/teacher").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Margot"));
    }

    @Test
    @DisplayName("GET /api/teacher/{id} : renvoie le professeur")
    void findById_shouldReturnTeacher() throws Exception {
        Teacher teacher = createTeacher("Margot", "DELAHAYE");

        mockMvc.perform(get("/api/teacher/" + teacher.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("DELAHAYE"));
    }

    @Test
    @DisplayName("GET /api/teacher/{id} : renvoie 404 quand le professeur n'existe pas")
    void findById_shouldReturn404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/teacher/999").header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/teacher/{id} : renvoie 400 quand l'id n'est pas un nombre")
    void findById_shouldReturn400_whenIdInvalid() throws Exception {
        mockMvc.perform(get("/api/teacher/abc").header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/teacher : renvoie 401 sans token")
    void findAll_shouldReturn401_withoutToken() throws Exception {
        mockMvc.perform(get("/api/teacher"))
                .andExpect(status().isUnauthorized());
    }
}
