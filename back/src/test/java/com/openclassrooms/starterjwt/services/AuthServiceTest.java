package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Authentication mockAuthentication() {
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(1L).username("yoga@studio.com").firstName("Admin").lastName("ADMIN").password("pwd").build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        return authentication;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("yoga@studio.com");
        request.setPassword("test!1234");
        return request;
    }

    @Test
    @DisplayName("login : renvoie le token et les infos d'un admin")
    void login_shouldReturnJwtResponse_forAdmin() {
        Authentication authentication = mockAuthentication();
        User admin = new User("yoga@studio.com", "ADMIN", "Admin", "pwd", true);
        when(userRepository.findByEmail("yoga@studio.com")).thenReturn(Optional.of(admin));

        JwtResponse response = authService.login(loginRequest());

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("yoga@studio.com");
        assertThat(response.getAdmin()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
    }

    @Test
    @DisplayName("login : admin vaut false quand l'utilisateur n'est pas retrouvé en base")
    void login_shouldReturnNonAdmin_whenUserNotFound() {
        mockAuthentication();
        when(userRepository.findByEmail("yoga@studio.com")).thenReturn(Optional.empty());

        JwtResponse response = authService.login(loginRequest());

        assertThat(response.getAdmin()).isFalse();
    }

    @Test
    @DisplayName("login : propage l'erreur quand l'email ou le mot de passe est faux")
    void login_shouldThrow_whenBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest())).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("register : crée un utilisateur non admin avec le mot de passe chiffré")
    void register_shouldSaveUser() {
        SignupRequest request = new SignupRequest();
        request.setEmail("new@test.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("password");
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new@test.com");
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getPassword()).isEqualTo("encoded");
        assertThat(saved.isAdmin()).isFalse();
    }

    @Test
    @DisplayName("register : lève BadRequestException quand l'email est déjà utilisé")
    void register_shouldThrowBadRequest_whenEmailTaken() {
        SignupRequest request = new SignupRequest();
        request.setEmail("taken@test.com");
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Error: Email is already taken!");
        verify(userRepository, never()).save(any());
    }
}
