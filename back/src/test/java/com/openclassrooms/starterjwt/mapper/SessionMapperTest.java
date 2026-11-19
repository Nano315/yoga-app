package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.TeacherService;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionMapperTest {

    @Mock
    private TeacherService teacherService;

    @Mock
    private UserService userService;

    private SessionMapper mapper;

    private final Teacher teacher = Teacher.builder().id(1L).firstName("Margot").lastName("DELAHAYE").build();
    private final User user = new User("user@test.com", "Doe", "John", "pwd", false).setId(2L);

    @BeforeEach
    void setUp() {
        mapper = new SessionMapperImpl();
        ReflectionTestUtils.setField(mapper, "teacherService", teacherService);
        ReflectionTestUtils.setField(mapper, "userService", userService);
    }

    @Test
    @DisplayName("toEntity : récupère le professeur et les participants à partir de leurs ids")
    void toEntity_shouldResolveTeacherAndUsers() {
        Date date = new Date();
        SessionDto dto = new SessionDto(1L, "Yoga", date, 1L, "Description", List.of(2L), null, null);
        when(teacherService.findById(1L)).thenReturn(teacher);
        when(userService.findById(2L)).thenReturn(user);

        Session session = mapper.toEntity(dto);

        assertThat(session.getId()).isEqualTo(1L);
        assertThat(session.getName()).isEqualTo("Yoga");
        assertThat(session.getDate()).isEqualTo(date);
        assertThat(session.getDescription()).isEqualTo("Description");
        assertThat(session.getTeacher()).isEqualTo(teacher);
        assertThat(session.getUsers()).containsExactly(user);
    }

    @Test
    @DisplayName("toEntity : sans professeur ni participants, la session n'en a pas")
    void toEntity_shouldHandleMissingTeacherAndUsers() {
        SessionDto dto = new SessionDto(null, "Yoga", new Date(), null, "Description", null, null, null);

        Session session = mapper.toEntity(dto);

        assertThat(session.getTeacher()).isNull();
        assertThat(session.getUsers()).isEmpty();
        verifyNoInteractions(teacherService, userService);
    }

    @Test
    @DisplayName("toDto : convertit le professeur et les participants en ids")
    void toDto_shouldMapTeacherAndUsersToIds() {
        Session session = Session.builder()
                .id(1L).name("Yoga").description("Description").date(new Date())
                .teacher(teacher).users(List.of(user)).build();

        SessionDto dto = mapper.toDto(session);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Yoga");
        assertThat(dto.getTeacher_id()).isEqualTo(1L);
        assertThat(dto.getUsers()).containsExactly(2L);
    }

    @Test
    @DisplayName("toDto : sans professeur ni participants, les ids sont vides")
    void toDto_shouldHandleMissingTeacherAndUsers() {
        Session session = Session.builder().id(1L).name("Yoga").build();

        SessionDto dto = mapper.toDto(session);

        assertThat(dto.getTeacher_id()).isNull();
        assertThat(dto.getUsers()).isEmpty();
    }

    @Test
    @DisplayName("Les conversions de listes appliquent la conversion à chaque élément")
    void listConversions_shouldMapEachElement() {
        Session session = Session.builder().id(1L).name("Yoga").build();
        SessionDto dto = new SessionDto(1L, "Yoga", new Date(), null, "Description", null, null, null);

        assertThat(mapper.toDto(List.of(session))).hasSize(1);
        assertThat(mapper.toEntity(List.of(dto))).hasSize(1);
    }

    @Test
    @DisplayName("Les valeurs null donnent null")
    void nullValues_shouldReturnNull() {
        assertThat(mapper.toDto((Session) null)).isNull();
        assertThat(mapper.toEntity((SessionDto) null)).isNull();
        assertThat(mapper.toDto((List<Session>) null)).isNull();
        assertThat(mapper.toEntity((List<SessionDto>) null)).isNull();
    }
}
