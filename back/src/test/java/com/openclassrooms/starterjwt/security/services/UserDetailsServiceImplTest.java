package com.openclassrooms.starterjwt.security.services;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("loadUserByUsername : construit le UserDetails depuis l'utilisateur en base")
    void loadUserByUsername_shouldReturnUserDetails() {
        User user = new User("user@test.com", "Doe", "John", "encoded", false);
        user.setId(3L);
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        UserDetails result = service.loadUserByUsername("user@test.com");

        UserDetailsImpl details = (UserDetailsImpl) result;
        assertThat(details.getId()).isEqualTo(3L);
        assertThat(details.getUsername()).isEqualTo("user@test.com");
        assertThat(details.getFirstName()).isEqualTo("John");
        assertThat(details.getLastName()).isEqualTo("Doe");
        assertThat(details.getPassword()).isEqualTo("encoded");
    }

    @Test
    @DisplayName("loadUserByUsername : lève UsernameNotFoundException quand l'email est inconnu")
    void loadUserByUsername_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);

        assertThatThrownBy(() -> service.loadUserByUsername("unknown@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
