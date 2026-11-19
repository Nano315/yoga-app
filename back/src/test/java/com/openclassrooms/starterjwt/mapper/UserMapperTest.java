package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapperImpl();

    @Test
    @DisplayName("toDto puis toEntity conservent toutes les informations de l'utilisateur")
    void toDtoAndToEntity_shouldKeepAllFields() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User("user@test.com", "Doe", "John", "pwd", true)
                .setId(1L).setCreatedAt(now).setUpdatedAt(now);

        UserDto dto = mapper.toDto(user);
        User back = mapper.toEntity(dto);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("user@test.com");
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.isAdmin()).isTrue();
        assertThat(back.getEmail()).isEqualTo("user@test.com");
        assertThat(back.getPassword()).isEqualTo("pwd");
        assertThat(back.isAdmin()).isTrue();
        assertThat(back.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Les conversions de listes appliquent la conversion à chaque élément")
    void listConversions_shouldMapEachElement() {
        User user = new User("user@test.com", "Doe", "John", "pwd", false).setId(1L);
        UserDto dto = new UserDto(2L, "other@test.com", "Smith", "Jane", false, "pwd", null, null);

        assertThat(mapper.toDto(List.of(user))).extracting(UserDto::getId).containsExactly(1L);
        assertThat(mapper.toEntity(List.of(dto))).extracting(User::getId).containsExactly(2L);
    }

    @Test
    @DisplayName("Les valeurs null donnent null")
    void nullValues_shouldReturnNull() {
        assertThat(mapper.toDto((User) null)).isNull();
        assertThat(mapper.toEntity((UserDto) null)).isNull();
        assertThat(mapper.toDto((List<User>) null)).isNull();
        assertThat(mapper.toEntity((List<UserDto>) null)).isNull();
    }
}
