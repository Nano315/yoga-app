package com.openclassrooms.starterjwt.security.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    private final UserDetailsImpl user = UserDetailsImpl.builder().id(1L).username("user@test.com").build();

    @Test
    @DisplayName("Le compte est toujours actif, non expiré et non verrouillé, sans rôle")
    void accountFlags_shouldAllBeTrue() {
        assertThat(user.getAuthorities()).isEmpty();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("equals : deux utilisateurs avec le même id sont égaux")
    void equals_shouldCompareOnId() {
        UserDetailsImpl sameId = UserDetailsImpl.builder().id(1L).username("autre@test.com").build();
        UserDetailsImpl otherId = UserDetailsImpl.builder().id(2L).username("user@test.com").build();

        assertThat(user.equals(user)).isTrue();
        assertThat(user.equals(sameId)).isTrue();
        assertThat(user.equals(otherId)).isFalse();
        assertThat(user.equals(null)).isFalse();
        assertThat(user.equals("pas un utilisateur")).isFalse();
    }
}
