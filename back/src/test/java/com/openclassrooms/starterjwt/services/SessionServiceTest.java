package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionService sessionService;

    private Session session;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("user@test.com", "Doe", "John", "password", false);
        user.setId(2L);

        session = Session.builder()
                .id(1L)
                .name("Yoga")
                .description("Session de yoga")
                .users(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("create : enregistre la session et la renvoie")
    void create_shouldSaveSession() {
        when(sessionRepository.save(session)).thenReturn(session);

        Session result = sessionService.create(session);

        assertThat(result).isEqualTo(session);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("findAll : renvoie toutes les sessions")
    void findAll_shouldReturnAllSessions() {
        when(sessionRepository.findAll()).thenReturn(List.of(session));

        List<Session> result = sessionService.findAll();

        assertThat(result).containsExactly(session);
    }

    @Test
    @DisplayName("getById : renvoie la session quand elle existe")
    void getById_shouldReturnSession_whenFound() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThat(sessionService.getById(1L)).isEqualTo(session);
    }

    @Test
    @DisplayName("getById : lève NotFoundException quand la session n'existe pas")
    void getById_shouldThrowNotFound_whenMissing() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getById(1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("update : force l'id puis enregistre la session")
    void update_shouldSetIdAndSave() {
        Session newSession = Session.builder().name("Nouveau nom").build();
        when(sessionRepository.save(newSession)).thenReturn(newSession);

        Session result = sessionService.update(5L, newSession);

        assertThat(result.getId()).isEqualTo(5L);
        verify(sessionRepository).save(newSession);
    }

    @Test
    @DisplayName("delete : supprime la session quand elle existe")
    void delete_shouldDeleteSession_whenFound() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.delete(1L);

        verify(sessionRepository).delete(session);
    }

    @Test
    @DisplayName("delete : lève NotFoundException et ne supprime rien quand la session n'existe pas")
    void delete_shouldThrowNotFound_whenMissing() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.delete(1L)).isInstanceOf(NotFoundException.class);
        verify(sessionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("participate : ajoute l'utilisateur à la session")
    void participate_shouldAddUser() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        sessionService.participate(1L, 2L);

        assertThat(session.getUsers()).containsExactly(user);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("participate : lève NotFoundException quand la session n'existe pas")
    void participate_shouldThrowNotFound_whenSessionMissing() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> sessionService.participate(1L, 2L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("participate : lève NotFoundException quand l'utilisateur n'existe pas")
    void participate_shouldThrowNotFound_whenUserMissing() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.participate(1L, 2L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("participate : lève BadRequestException quand l'utilisateur participe déjà")
    void participate_shouldThrowBadRequest_whenAlreadyParticipating() {
        session.getUsers().add(user);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> sessionService.participate(1L, 2L)).isInstanceOf(BadRequestException.class);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("noLongerParticipate : retire l'utilisateur de la session")
    void noLongerParticipate_shouldRemoveUser() {
        session.getUsers().add(user);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        sessionService.noLongerParticipate(1L, 2L);

        assertThat(session.getUsers()).isEmpty();
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("noLongerParticipate : lève NotFoundException quand la session n'existe pas")
    void noLongerParticipate_shouldThrowNotFound_whenSessionMissing() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.noLongerParticipate(1L, 2L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("noLongerParticipate : lève BadRequestException quand l'utilisateur ne participe pas")
    void noLongerParticipate_shouldThrowBadRequest_whenNotParticipating() {
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> sessionService.noLongerParticipate(1L, 2L)).isInstanceOf(BadRequestException.class);
        verify(sessionRepository, never()).save(any());
    }
}
