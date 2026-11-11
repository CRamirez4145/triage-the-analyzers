package team.projectpulse;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import team.projectpulse.course.Course;
import team.projectpulse.evaluation.PeerEvaluation;
import team.projectpulse.evaluation.Rating;
import team.projectpulse.ram.collaboration.Comment;
import team.projectpulse.ram.collaboration.CommentThread;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.requirement.RequirementArtifactType;
import team.projectpulse.ram.usecase.UseCase;
import team.projectpulse.ram.usecase.UseCaseMainStep;
import team.projectpulse.rubric.Criterion;
import team.projectpulse.section.Section;
import team.projectpulse.team.Team;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JpaCollectionOwnershipIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    EntityManager entityManager;

    @Test
    void courseHelperStillPersistsBothSidesBehindTheReadOnlyView() {
        Course course = entityManager.find(Course.class, 1);
        Criterion criterion = new Criterion("D1 criterion", "Relationship ownership", 10.0);
        List<Criterion> view = course.getCriteria();
        course.addCriterion(criterion);
        entityManager.persist(criterion);
        entityManager.flush();
        Integer criterionId = criterion.getCriterionId();
        assertThat(view).contains(criterion);
        entityManager.clear();

        Criterion reloaded = entityManager.find(Criterion.class, criterionId);
        assertThat(reloaded.getCourse().getCourseId()).isEqualTo(1);
        assertThat(reloaded.getCourse().getCriteria()).contains(reloaded);
    }

    @Test
    void ratingCopyPreservesCascadeParentsScoresAndPersistentIdentity() {
        PeerEvaluation existing = entityManager.createQuery("select e from PeerEvaluation e", PeerEvaluation.class)
                .setMaxResults(1).getSingleResult();
        Criterion criterion = existing.getRatings().getFirst().getCriterion();
        Rating rating = new Rating(criterion, criterion.getMaxScore() / 2);
        List<Rating> input = new ArrayList<>(List.of(rating));
        PeerEvaluation evaluation = new PeerEvaluation("2099-W01", existing.getEvaluator(), existing.getEvaluatee(),
                input, "D1 public", "D1 private");
        input.clear();
        entityManager.persist(evaluation);
        entityManager.flush();
        Integer evaluationId = evaluation.getPeerEvaluationId();
        Integer ratingId = rating.getRatingId();
        entityManager.clear();

        PeerEvaluation reloaded = entityManager.find(PeerEvaluation.class, evaluationId);
        assertThat(reloaded.getRatings()).hasSize(1);
        Rating persisted = reloaded.getRatings().getFirst();
        assertThat(persisted.getRatingId()).isEqualTo(ratingId);
        assertThat(persisted.getPeerEvaluation()).isSameAs(reloaded);
        persisted.setActualScore(persisted.getCriterion().getMaxScore());
        reloaded.calculateTotalScore();
        Double expectedScore = persisted.getActualScore();
        entityManager.flush();
        entityManager.clear();

        PeerEvaluation rescored = entityManager.find(PeerEvaluation.class, evaluationId);
        assertThat(rescored.getRatings().getFirst().getRatingId()).isEqualTo(ratingId);
        assertThat(rescored.getTotalScore()).isEqualTo(expectedScore);
    }

    @Test
    void activeWeekCopiesStillPersistMembershipAndSupportLaterUpdates() {
        Section section = entityManager.find(Section.class, 2);
        List<String> input = new ArrayList<>(List.of("2026-W37", "2026-W38"));
        section.setActiveWeeks(input);
        input.clear();
        section.getActiveWeeks().clear();
        entityManager.flush();
        entityManager.clear();

        Section reloaded = entityManager.find(Section.class, 2);
        assertThat(reloaded.getActiveWeeks()).containsExactlyInAnyOrder("2026-W37", "2026-W38");
        reloaded.setActiveWeeks(List.of("2026-W39"));
        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(Section.class, 2).getActiveWeeks()).containsExactly("2026-W39");
    }

    @Test
    void documentSectionCopyPreservesOrderedCascadeAndParentReferences() {
        RequirementDocument document = new RequirementDocument();
        document.setType(DocumentType.SRS);
        document.setDocumentKey("D1_OWNERSHIP");
        document.setTeam(entityManager.find(Team.class, 1));
        DocumentSection first = new DocumentSection();
        first.setSectionKey("first");
        first.setDocument(document);
        DocumentSection second = new DocumentSection();
        second.setSectionKey("second");
        second.setDocument(document);
        List<DocumentSection> input = new ArrayList<>(List.of(first, second));
        document.setSections(input);
        input.clear();
        entityManager.persist(document);
        entityManager.flush();
        Long documentId = document.getId();
        entityManager.clear();

        RequirementDocument reloaded = entityManager.find(RequirementDocument.class, documentId);
        assertThat(reloaded.getSections()).extracting(DocumentSection::getSectionKey).containsExactly("first", "second");
        assertThat(reloaded.getSections()).allSatisfy(section -> assertThat(section.getDocument()).isSameAs(reloaded));
    }

    @Test
    void copiedCommentsRemainOrderedAndSupportManagedOrphanRemoval() {
        CommentThread thread = new CommentThread();
        thread.setTeam(entityManager.find(Team.class, 1));
        Comment first = new Comment();
        first.setContent("first");
        first.setCommentThread(thread);
        Comment second = new Comment();
        second.setContent("second");
        second.setCommentThread(thread);
        List<Comment> input = new ArrayList<>(List.of(first, second));
        thread.setComments(input);
        input.clear();
        entityManager.persist(thread);
        entityManager.flush();
        Long threadId = thread.getId();
        Long firstId = first.getId();
        entityManager.clear();

        CommentThread reloaded = entityManager.find(CommentThread.class, threadId);
        assertThat(reloaded.getComments()).extracting(Comment::getContent).containsExactly("first", "second");
        reloaded.removeComment(reloaded.getComments().getFirst());
        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(Comment.class, firstId)).isNull();
        assertThat(entityManager.find(CommentThread.class, threadId).getComments())
                .extracting(Comment::getContent).containsExactly("second");
    }

    @Test
    void copiedUseCaseStepsRemainOrderedAndReplacementDeletesOldSteps() {
        UseCase useCase = new UseCase();
        useCase.setArtifact(new RequirementArtifact(entityManager.find(Team.class, 1),
                RequirementArtifactType.USE_CASE, "D1 use case", "Ownership test", null));
        UseCaseMainStep first = new UseCaseMainStep("student", "first");
        first.setUseCase(useCase);
        UseCaseMainStep second = new UseCaseMainStep("student", "second");
        second.setUseCase(useCase);
        List<UseCaseMainStep> input = new ArrayList<>(List.of(first, second));
        useCase.setMainSteps(input);
        input.clear();
        entityManager.persist(useCase);
        entityManager.flush();
        Long useCaseId = useCase.getId();
        Long firstId = first.getId();
        Long secondId = second.getId();
        entityManager.clear();

        UseCase reloaded = entityManager.find(UseCase.class, useCaseId);
        assertThat(reloaded.getMainSteps()).extracting(UseCaseMainStep::getActionText).containsExactly("first", "second");
        reloaded.replaceMainSteps(List.of(new UseCaseMainStep("instructor", "replacement")));
        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(UseCaseMainStep.class, firstId)).isNull();
        assertThat(entityManager.find(UseCaseMainStep.class, secondId)).isNull();
        UseCase replaced = entityManager.find(UseCase.class, useCaseId);
        assertThat(replaced.getMainSteps()).extracting(UseCaseMainStep::getActionText).containsExactly("replacement");
        assertThat(replaced.getMainSteps().getFirst().getUseCase()).isSameAs(replaced);
    }
}
