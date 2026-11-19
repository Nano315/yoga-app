package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.exception.UnauthorizedException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("user@test.com", "Doe", "John", "password", false);
        user.setId(1L);
    }

    @Test
    @DisplayName("findById : renvoie l'utilisateur quand il existe")
    void findById_shouldReturnUser_whenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(userService.findById(1L)).isEqualTo(user);
    }

    @Test
    @DisplayName("findById : lève NotFoundException quand l'utilisateur n'existe pas")
    void findById_shouldThrowNotFound_whenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("delete : supprime le compte quand c'est celui de l'utilisateur connecté")
    void delete_shouldDeleteUser_whenOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L, "user@test.com");

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete : lève UnauthorizedException quand ce n'est pas le compte de l'utilisateur connecté")
    void delete_shouldThrowUnauthorized_whenNotOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.delete(1L, "other@test.com"))
                .isInstanceOf(UnauthorizedException.class);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("delete : lève NotFoundException quand l'utilisateur n'existe pas")
    void delete_shouldThrowNotFound_whenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(1L, "user@test.com"))
                .isInstanceOf(NotFoundException.class);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
