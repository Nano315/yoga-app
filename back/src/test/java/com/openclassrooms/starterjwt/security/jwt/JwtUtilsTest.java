package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    private static final String SECRET =
            "3f9a1c7e5b2d8f4a6c0e9b1d3f5a7c9e2b4d6f8a0c1e3b5d7f9a2c4e6b8d0f1a3c5e7b9d2f4a6c8e0b1d3f5a7c9e2b4d6f8a0c1e3b5d7f9a2c4e6b8d0f1a";
    private static final String OTHER_SECRET =
            "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2";

    private JwtUtils jwtUtils;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtUtils = createJwtUtils(SECRET, 60_000);
        UserDetailsImpl userDetails = UserDetailsImpl.builder().id(1L).username("yoga@studio.com").build();
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
    }

    private JwtUtils createJwtUtils(String secret, int expirationMs) {
        JwtUtils utils = new JwtUtils();
        ReflectionTestUtils.setField(utils, "jwtSecret", secret);
        ReflectionTestUtils.setField(utils, "jwtExpirationMs", expirationMs);
        return utils;
    }

    @Test
    @DisplayName("Un token généré est valide et contient l'email de l'utilisateur")
    void generateJwtToken_shouldCreateValidTokenWithUsername() {
        String token = jwtUtils.generateJwtToken(authentication);

        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("yoga@studio.com");
    }

    @Test
    @DisplayName("Un token signé avec une autre clé est refusé")
    void validateJwtToken_shouldReturnFalse_whenSignatureInvalid() {
        String token = createJwtUtils(OTHER_SECRET, 60_000).generateJwtToken(authentication);

        assertThat(jwtUtils.validateJwtToken(token)).isFalse();
    }

    @Test
    @DisplayName("Un token mal formé est refusé")
    void validateJwtToken_shouldReturnFalse_whenMalformed() {
        assertThat(jwtUtils.validateJwtToken("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("Un token expiré est refusé")
    void validateJwtToken_shouldReturnFalse_whenExpired() {
        String token = createJwtUtils(SECRET, -1_000).generateJwtToken(authentication);

        assertThat(jwtUtils.validateJwtToken(token)).isFalse();
    }

    @Test
    @DisplayName("Un token non signé est refusé")
    void validateJwtToken_shouldReturnFalse_whenUnsigned() {
        String unsignedToken = Jwts.builder().subject("yoga@studio.com").compact();

        assertThat(jwtUtils.validateJwtToken(unsignedToken)).isFalse();
    }

    @Test
    @DisplayName("Un token vide est refusé")
    void validateJwtToken_shouldReturnFalse_whenEmpty() {
        assertThat(jwtUtils.validateJwtToken("")).isFalse();
    }
}
