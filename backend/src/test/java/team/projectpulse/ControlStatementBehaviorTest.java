package team.projectpulse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import team.projectpulse.section.Section;
import team.projectpulse.student.Student;
import team.projectpulse.system.UserUtils;
import team.projectpulse.team.Team;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ControlStatementBehaviorTest {

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void missingAuthenticationHasNoUserId() {
        SecurityContextHolder.clearContext();
        assertThat(new UserUtils(null, null).getUserId()).isNull();
    }

    @Test
    void unauthenticatedPrincipalHasNoUserId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("principal", "credentials"));
        assertThat(new UserUtils(null, null).getUserId()).isNull();
    }

    @Test
    void authenticatedNonJwtPrincipalHasNoUserId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("principal", "credentials", List.of()));
        assertThat(new UserUtils(null, null).getUserId()).isNull();
    }

    @TestFactory
    Stream<DynamicTest> jwtClaimTypesPreserveTheExistingUserIdContract() {
        return Arrays.asList(7, 7L, "7", true, 7.0, null).stream()
                .map(claim -> DynamicTest.dynamicTest("userId claim: " + claim + " ("
                        + (claim == null ? "missing" : claim.getClass().getSimpleName()) + ")", () -> {
                    try {
                        authenticateJwt(claim);
                        Integer expected = claim instanceof Integer || claim instanceof Long || claim instanceof String
                                ? 7 : null;
                        assertThat(new UserUtils(null, null).getUserId()).isEqualTo(expected);
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                }));
    }

    @Test
    void malformedStringClaimStillRaisesNumberFormatException() {
        authenticateJwt("invalid-user-id");
        assertThatThrownBy(() -> new UserUtils(null, null).getUserId())
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void existingMembershipStillRepairsBothParentReferencesWithoutDuplicates() {
        Section section = new Section();
        Team team = new Team();
        Student student = new Student();
        section.addTeam(null);
        section.addStudent(null);
        assertThat(section.getTeams()).isEmpty();
        assertThat(section.getStudents()).isEmpty();

        section.addTeam(team);
        section.addStudent(student);
        assertThat(team.getSection()).isSameAs(section);
        assertThat(student.getSection()).isSameAs(section);

        team.setSection(null);
        student.setSection(null);
        section.addTeam(team);
        section.addStudent(student);
        assertThat(section.getTeams()).containsExactly(team);
        assertThat(section.getStudents()).containsExactly(student);
        assertThat(team.getSection()).isSameAs(section);
        assertThat(student.getSection()).isSameAs(section);
    }

    private void authenticateJwt(Object claim) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token").header("alg", "none").subject("test-user");
        if (claim != null) {
            builder.claim("userId", claim);
        }
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(builder.build(), List.of());
        assertThat(authentication.isAuthenticated()).isTrue();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
