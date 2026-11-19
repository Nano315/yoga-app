package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SessionControllerIntegrationTest extends AbstractIntegrationTest {

    private User user;
    private Teacher teacher;
    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUpData() throws Exception {
        createUser("yoga@studio.com", true);
        user = createUser("user@test.com", false);
        teacher = createTeacher("Margot", "DELAHAYE");
        adminToken = bearerTokenFor("yoga@studio.com");
        userToken = bearerTokenFor("user@test.com");
    }

    private Session createSession(String name) {
        return sessionRepository.save(Session.builder()
                .name(name)
                .description("Description de " + name)
                .date(new Date())
                .teacher(teacher)
                .users(new ArrayList<>())
                .build());
    }

    private Map<String, Object> sessionBody(String name, Long teacherId) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("date", "2026-12-01");
        body.put("teacher_id", teacherId);
        body.put("description", "Une séance de yoga");
        return body;
    }

    // ---------- Affichage ----------

    @Test
    @DisplayName("Sessions : GET /api/session renvoie la liste des sessions")
    void findAll_shouldReturnSessions() throws Exception {
        createSession("Yoga du matin");

        mockMvc.perform(get("/api/session").header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Yoga du matin"));
    }

    @Test
    @DisplayName("Informations session : GET /api/session/{id} renvoie le détail de la session")
    void findById_shouldReturnSession() throws Exception {
        Session session = createSession("Yoga du matin");

        mockMvc.perform(get("/api/session/" + session.getId()).header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga du matin"))
                .andExpect(jsonPath("$.description").value("Description de Yoga du matin"))
                .andExpect(jsonPath("$.teacher_id").value(teacher.getId()))
                .andExpect(jsonPath("$.users").isEmpty());
    }

    @Test
    @DisplayName("GET /api/session/{id} : renvoie 404 quand la session n'existe pas")
    void findById_shouldReturn404_whenMissing() throws Exception {
        mockMvc.perform(get("/api/session/999").header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/session/{id} : renvoie 400 quand l'id n'est pas un nombre")
    void findById_shouldReturn400_whenIdInvalid() throws Exception {
        mockMvc.perform(get("/api/session/abc").header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }

    // ---------- Création ----------

    @Test
    @DisplayName("Création session : la session est créée en base")
    void create_shouldCreateSession() throws Exception {
        mockMvc.perform(post("/api/session")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionBody("Nouvelle session", teacher.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Nouvelle session"))
                .andExpect(jsonPath("$.teacher_id").value(teacher.getId()));

        assertThat(sessionRepository.findAll()).extracting(Session::getName).containsExactly("Nouvelle session");
    }

    @Test
    @DisplayName("Création session : un champ obligatoire manquant renvoie 400")
    void create_shouldReturn400_whenFieldMissing() throws Exception {
        Map<String, Object> body = sessionBody("Nouvelle session", teacher.getId());
        body.remove("name");

        mockMvc.perform(post("/api/session")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        assertThat(sessionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Création session : un professeur inexistant renvoie 404")
    void create_shouldReturn404_whenTeacherMissing() throws Exception {
        mockMvc.perform(post("/api/session")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionBody("Nouvelle session", 999L))))
                .andExpect(status().isNotFound());
    }

    // ---------- Modification ----------

    @Test
    @DisplayName("Modification session : la session est modifiée en base")
    void update_shouldUpdateSession() throws Exception {
        Session session = createSession("Ancien nom");

        mockMvc.perform(put("/api/session/" + session.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionBody("Nouveau nom", teacher.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nouveau nom"));

        assertThat(sessionRepository.findById(session.getId())).get()
                .extracting(Session::getName).isEqualTo("Nouveau nom");
    }

    @Test
    @DisplayName("Modification session : un champ obligatoire manquant renvoie 400")
    void update_shouldReturn400_whenFieldMissing() throws Exception {
        Session session = createSession("Ancien nom");
        Map<String, Object> body = sessionBody("Nouveau nom", teacher.getId());
        body.remove("description");

        mockMvc.perform(put("/api/session/" + session.getId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        assertThat(sessionRepository.findById(session.getId())).get()
                .extracting(Session::getName).isEqualTo("Ancien nom");
    }

    @Test
    @DisplayName("Modification session : renvoie 400 quand l'id n'est pas un nombre")
    void update_shouldReturn400_whenIdInvalid() throws Exception {
        mockMvc.perform(put("/api/session/abc")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionBody("Nouveau nom", teacher.getId()))))
                .andExpect(status().isBadRequest());
    }

    // ---------- Suppression ----------

    @Test
    @DisplayName("Suppression session : la session est supprimée de la base")
    void delete_shouldDeleteSession() throws Exception {
        Session session = createSession("A supprimer");

        mockMvc.perform(delete("/api/session/" + session.getId()).header("Authorization", adminToken))
                .andExpect(status().isOk());

        assertThat(sessionRepository.findById(session.getId())).isEmpty();
    }

    @Test
    @DisplayName("Suppression session : renvoie 404 quand la session n'existe pas")
    void delete_shouldReturn404_whenMissing() throws Exception {
        mockMvc.perform(delete("/api/session/999").header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    // ---------- Participation ----------

    @Test
    @DisplayName("Participation : l'utilisateur est ajouté à la session")
    void participate_shouldAddUser() throws Exception {
        Session session = createSession("Yoga du matin");

        mockMvc.perform(post("/api/session/" + session.getId() + "/participate/" + user.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        assertThat(sessionRepository.findById(session.getId()).orElseThrow().getUsers())
                .extracting(User::getId).containsExactly(user.getId());
    }

    @Test
    @DisplayName("Participation : renvoie 400 quand l'utilisateur participe déjà")
    void participate_shouldReturn400_whenAlreadyParticipating() throws Exception {
        Session session = createSession("Yoga du matin");
        session.setUsers(new ArrayList<>(List.of(user)));
        sessionRepository.save(session);

        mockMvc.perform(post("/api/session/" + session.getId() + "/participate/" + user.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Participation : renvoie 404 quand l'utilisateur n'existe pas")
    void participate_shouldReturn404_whenUserMissing() throws Exception {
        Session session = createSession("Yoga du matin");

        mockMvc.perform(post("/api/session/" + session.getId() + "/participate/999")
                        .header("Authorization", userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Désinscription : l'utilisateur est retiré de la session")
    void noLongerParticipate_shouldRemoveUser() throws Exception {
        Session session = createSession("Yoga du matin");
        session.setUsers(new ArrayList<>(List.of(user)));
        sessionRepository.save(session);

        mockMvc.perform(delete("/api/session/" + session.getId() + "/participate/" + user.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isOk());

        assertThat(sessionRepository.findById(session.getId()).orElseThrow().getUsers()).isEmpty();
    }

    @Test
    @DisplayName("Désinscription : renvoie 400 quand l'utilisateur ne participe pas")
    void noLongerParticipate_shouldReturn400_whenNotParticipating() throws Exception {
        Session session = createSession("Yoga du matin");

        mockMvc.perform(delete("/api/session/" + session.getId() + "/participate/" + user.getId())
                        .header("Authorization", userToken))
                .andExpect(status().isBadRequest());
    }
}
