package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.models.Teacher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeacherMapperTest {

    private final TeacherMapper mapper = new TeacherMapperImpl();

    @Test
    @DisplayName("toDto puis toEntity conservent toutes les informations du professeur")
    void toDtoAndToEntity_shouldKeepAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Teacher teacher = Teacher.builder()
                .id(1L).firstName("Margot").lastName("DELAHAYE").createdAt(now).updatedAt(now).build();

        TeacherDto dto = mapper.toDto(teacher);
        Teacher back = mapper.toEntity(dto);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("Margot");
        assertThat(dto.getLastName()).isEqualTo("DELAHAYE");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(back.getId()).isEqualTo(1L);
        assertThat(back.getFirstName()).isEqualTo("Margot");
        assertThat(back.getLastName()).isEqualTo("DELAHAYE");
        assertThat(back.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Les conversions de listes appliquent la conversion à chaque élément")
    void listConversions_shouldMapEachElement() {
        Teacher teacher = Teacher.builder().id(1L).firstName("Margot").lastName("DELAHAYE").build();
        TeacherDto dto = new TeacherDto(2L, "THIERCELIN", "Hélène", null, null);

        assertThat(mapper.toDto(List.of(teacher))).extracting(TeacherDto::getId).containsExactly(1L);
        assertThat(mapper.toEntity(List.of(dto))).extracting(Teacher::getId).containsExactly(2L);
    }

    @Test
    @DisplayName("Les valeurs null donnent null")
    void nullValues_shouldReturnNull() {
        assertThat(mapper.toDto((Teacher) null)).isNull();
        assertThat(mapper.toEntity((TeacherDto) null)).isNull();
        assertThat(mapper.toDto((List<Teacher>) null)).isNull();
        assertThat(mapper.toEntity((List<TeacherDto>) null)).isNull();
    }
}
