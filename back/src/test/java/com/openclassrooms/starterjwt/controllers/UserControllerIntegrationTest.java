package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private User user;
    private String token;

    @BeforeEach
    void login() throws Exception {
        user = createUser("user@test.com", false);
        token = bearerTokenFor("user@test.com");
    }

    @Test
    @DisplayName("Account : GET /api/user/{id} renvoie les informations de l'utilisateur sans le mot de passe")
    void findById_shouldReturnUserInformation() throws Exception {
        mockMvc.perform(get("/api/user/" + user.getId()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/user/{id} : renvoie 404 quand l'utilisateur n'existe pas")
    void findById_shouldReturn404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/user/999").header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/user/{id} : renvoie 400 quand l'id n'est pas un nombre")
    void findById_shouldReturn400_whenIdInvalid() throws Exception {
        mockMvc.perform(get("/api/user/abc").header("Authorization", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/user/{id} : l'utilisateur supprime son propre compte")
    void delete_shouldDeleteOwnAccount() throws Exception {
        mockMvc.perform(delete("/api/user/" + user.getId()).header("Authorization", token))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/user/{id} : renvoie 401 quand on supprime le compte d'un autre")
    void delete_shouldReturn401_whenDeletingOtherAccount() throws Exception {
        User other = createUser("other@test.com", false);

        mockMvc.perform(delete("/api/user/" + other.getId()).header("Authorization", token))
                .andExpect(status().isUnauthorized());

        assertThat(userRepository.findById(other.getId())).isPresent();
    }

    @Test
    @DisplayName("DELETE /api/user/{id} : renvoie 404 quand l'utilisateur n'existe pas")
    void delete_shouldReturn404_whenMissing() throws Exception {
        mockMvc.perform(delete("/api/user/999").header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Sécurité : un en-tête Authorization sans « Bearer » est refusé (401)")
    void request_shouldReturn401_whenHeaderNotBearer() throws Exception {
        mockMvc.perform(get("/api/user/" + user.getId()).header("Authorization", "Basic abc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sécurité : un token invalide est refusé (401)")
    void request_shouldReturn401_whenTokenInvalid() throws Exception {
        mockMvc.perform(get("/api/user/" + user.getId()).header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Sécurité : le token d'un utilisateur supprimé est refusé (401)")
    void request_shouldReturn401_whenUserNoLongerExists() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(get("/api/user/" + user.getId()).header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }
}
