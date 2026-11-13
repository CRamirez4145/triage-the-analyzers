package team.projectpulse;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import team.projectpulse.evaluation.PeerEvaluationAverage;
import team.projectpulse.evaluation.RatingAverage;
import team.projectpulse.evaluation.WeeklyPeerEvaluationReport;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.student.Student;
import team.projectpulse.system.Result;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ConstructorCompatibilityTest {

    @TestFactory
    Stream<DynamicTest> allReviewedClassesRetainPublicReflectiveNoArgumentConstruction() {
        return Stream.of(
                "activity.Activity", "course.Course", "evaluation.PeerEvaluation",
                "evaluation.PeerEvaluationAverage", "evaluation.Rating", "evaluation.RatingAverage",
                "evaluation.WeeklyPeerEvaluationReport", "instructor.Instructor",
                "ram.collaboration.Comment", "ram.collaboration.CommentThread",
                "ram.document.DocumentSection", "ram.document.DocumentSectionLock",
                "ram.document.RequirementDocument", "ram.requirement.ArtifactKeySequence",
                "ram.requirement.ArtifactLink", "ram.requirement.RequirementArtifact",
                "ram.usecase.UseCase", "ram.usecase.UseCaseExtension", "ram.usecase.UseCaseExtensionStep",
                "ram.usecase.UseCaseLock", "ram.usecase.UseCaseMainStep", "rubric.Criterion", "rubric.Rubric",
                "section.Section", "student.Student", "system.Result", "team.Team", "user.PeerEvaluationUser",
                "user.resetpassword.ResetPasswordToken", "user.userinvitation.UserInvitation"
        ).map(name -> DynamicTest.dynamicTest(name, () -> {
            Class<?> type = Class.forName("team.projectpulse." + name);
            // getConstructor finds only public constructors, including compiler-provided defaults.
            assertThat(Modifier.isPublic(type.getConstructor().getModifiers())).isTrue();
            if (!Modifier.isAbstract(type.getModifiers())) {
                assertThat(type.getConstructor().newInstance()).isExactlyInstanceOf(type);
            }
        }));
    }

    @Test
    void abstractUserConstructionStillWorksThroughBothConcreteSubclasses() {
        Student student = new Student();
        Instructor instructor = new Instructor();
        student.setUsername("student@example.test");
        instructor.setUsername("instructor@example.test");
        assertThat(student.getUsername()).isEqualTo("student@example.test");
        assertThat(instructor.getUsername()).isEqualTo("instructor@example.test");
    }

    @Test
    void reportAssemblyStillHasAnInitializedMutableListAndSetterPopulatedAverages() {
        WeeklyPeerEvaluationReport report = new WeeklyPeerEvaluationReport();
        assertThat(report.getPeerEvaluationAverages()).isEmpty();
        PeerEvaluationAverage average = new PeerEvaluationAverage();
        average.setStudentId(7);
        average.setAverageTotalScore(9.0);
        report.getPeerEvaluationAverages().add(average);
        assertThat(report.getPeerEvaluationAverages()).containsExactly(average);
        assertThat(report.getPeerEvaluationAverages().getFirst()).isSameAs(average);
        assertThat(average.getStudentId()).isEqualTo(7);
        assertThat(average.getAverageTotalScore()).isEqualTo(9.0);
    }

    @Test
    void resultRetainsEmptyBeanAndParameterizedConstruction() {
        Result result = new Result();
        assertThat(result.getCode()).isNull();
        result.setFlag(true);
        result.setCode(200);
        result.setMessage("Success");
        result.setData("payload");
        assertThat(result.isFlag()).isTrue();
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("Success");
        assertThat(result.getData()).isEqualTo("payload");
        assertThat(new Result(true, 200, "Success").getMessage()).isEqualTo("Success");
        assertThat(new Result(true, 200, "Success", "payload").getData()).isEqualTo("payload");
    }

    @Test
    void ratingAverageRetainsEmptyBeanAndParameterizedConstruction() {
        RatingAverage average = new RatingAverage();
        assertThat(average.getCriterionId()).isNull();
        assertThat(average.getAverageScore()).isNull();
        average.setCriterionId(7);
        average.setAverageScore(8.5);
        assertThat(average.getCriterionId()).isEqualTo(7);
        assertThat(average.getAverageScore()).isEqualTo(8.5);
        RatingAverage populated = new RatingAverage(7, 8.5);
        assertThat(populated.getCriterionId()).isEqualTo(7);
        assertThat(populated.getAverageScore()).isEqualTo(8.5);
    }
}
