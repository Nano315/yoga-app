package com.openclassrooms.starterjwt.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    private String json(Map<String, String> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    @Test
    @DisplayName("Login : un admin se connecte et reçoit un token")
    void login_shouldReturnToken_forValidAdmin() throws Exception {
        createUser("yoga@studio.com", true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "yoga@studio.com", "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("yoga@studio.com"))
                .andExpect(jsonPath("$.admin").value(true));
    }

    @Test
    @DisplayName("Login : un utilisateur simple est renvoyé avec admin = false")
    void login_shouldReturnNonAdmin_forSimpleUser() throws Exception {
        createUser("user@test.com", false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "user@test.com", "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    @DisplayName("Login : un mauvais mot de passe renvoie 401")
    void login_shouldReturn401_whenBadPassword() throws Exception {
        createUser("yoga@studio.com", true);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "yoga@studio.com", "password", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login : un email inconnu renvoie 401")
    void login_shouldReturn401_whenUnknownEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "unknown@test.com", "password", PASSWORD))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login : un champ obligatoire manquant renvoie 400")
    void login_shouldReturn400_whenFieldMissing() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "yoga@studio.com"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Register : le compte est créé en base")
    void register_shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "new@test.com",
                                "firstName", "John",
                                "lastName", "Doe",
                                "password", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        assertThat(userRepository.findByEmail("new@test.com")).isPresent();
    }

    @Test
    @DisplayName("Register : un email déjà utilisé renvoie 400 avec un message")
    void register_shouldReturn400_whenEmailTaken() throws Exception {
        createUser("taken@test.com", false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "email", "taken@test.com",
                                "firstName", "John",
                                "lastName", "Doe",
                                "password", "password"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already taken!"));
    }

    @Test
    @DisplayName("Register : un champ obligatoire manquant renvoie 400")
    void register_shouldReturn400_whenFieldMissing() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "new@test.com", "password", "password"))))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findByEmail("new@test.com")).isEmpty();
    }
}
