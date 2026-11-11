package team.projectpulse;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import team.projectpulse.course.Course;
import team.projectpulse.evaluation.PeerEvaluation;
import team.projectpulse.evaluation.Rating;
import team.projectpulse.instructor.Instructor;
import team.projectpulse.ram.collaboration.Comment;
import team.projectpulse.ram.collaboration.CommentThread;
import team.projectpulse.ram.document.DocumentSection;
import team.projectpulse.ram.document.RequirementDocument;
import team.projectpulse.ram.requirement.ArtifactLink;
import team.projectpulse.ram.requirement.RequirementArtifact;
import team.projectpulse.ram.usecase.UseCase;
import team.projectpulse.ram.usecase.UseCaseMainStep;
import team.projectpulse.rubric.Criterion;
import team.projectpulse.section.Section;
import team.projectpulse.team.Team;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JpaCollectionOwnershipTest {

    @TestFactory
    Stream<DynamicTest> associationSettersOwnMembershipButShareEntities() {
        Instructor instructor = new Instructor();
        CommentThread thread = new CommentThread();
        DocumentSection section = new DocumentSection();
        RequirementDocument document = new RequirementDocument();
        RequirementArtifact artifact = new RequirementArtifact();
        UseCase useCase = new UseCase();

        return Stream.of(
                ownership("Instructor.teams", instructor::setTeams, instructor::getTeams,
                        ArrayList::new, new Team(), new Team()),
                ownership("Instructor.sections", instructor::setSections, instructor::getSections,
                        LinkedHashSet::new, new Section(), new Section()),
                ownership("Instructor.courses", instructor::setCourses, instructor::getCourses,
                        LinkedHashSet::new, new Course(), new Course()),
                ownership("CommentThread.comments", thread::setComments, thread::getComments,
                        ArrayList::new, new Comment(), new Comment()),
                ownership("DocumentSection.requirementArtifacts", section::setRequirementArtifacts,
                        section::getRequirementArtifacts, ArrayList::new,
                        new RequirementArtifact(), new RequirementArtifact()),
                ownership("DocumentSection.commentThreads", section::setCommentThreads,
                        section::getCommentThreads, LinkedHashSet::new, new CommentThread(), new CommentThread()),
                ownership("RequirementDocument.sections", document::setSections, document::getSections,
                        ArrayList::new, new DocumentSection(), new DocumentSection()),
                ownership("RequirementDocument.commentThreads", document::setCommentThreads,
                        document::getCommentThreads, LinkedHashSet::new, new CommentThread(), new CommentThread()),
                ownership("RequirementArtifact.outgoingArtifactLinks", artifact::setOutgoingArtifactLinks,
                        artifact::getOutgoingArtifactLinks, LinkedHashSet::new, new ArtifactLink(), new ArtifactLink()),
                ownership("RequirementArtifact.incomingArtifactLinks", artifact::setIncomingArtifactLinks,
                        artifact::getIncomingArtifactLinks, LinkedHashSet::new, new ArtifactLink(), new ArtifactLink()),
                ownership("RequirementArtifact.commentThreads", artifact::setCommentThreads,
                        artifact::getCommentThreads, LinkedHashSet::new, new CommentThread(), new CommentThread()),
                ownership("UseCase.mainSteps", useCase::setMainSteps, useCase::getMainSteps,
                        ArrayList::new, new UseCaseMainStep(), new UseCaseMainStep()),
                ownership("UseCase.secondaryActors", useCase::setSecondaryActors, useCase::getSecondaryActors,
                        LinkedHashSet::new, new RequirementArtifact(), new RequirementArtifact()),
                ownership("UseCase.preconditions", useCase::setPreconditions, useCase::getPreconditions,
                        LinkedHashSet::new, new RequirementArtifact(), new RequirementArtifact()),
                ownership("UseCase.postconditions", useCase::setPostconditions, useCase::getPostconditions,
                        LinkedHashSet::new, new RequirementArtifact(), new RequirementArtifact())
        );
    }

    private static <E, C extends Collection<E>> DynamicTest ownership(
            String name, Consumer<C> setter, Supplier<C> getter, Supplier<C> factory, E first, E second) {
        return DynamicTest.dynamicTest(name, () -> {
            C input = factory.get();
            input.add(first);
            input.add(second);
            input.add(null);
            setter.accept(input);
            input.clear();

            C owned = getter.get();
            assertThat(owned).containsExactly(first, second, null);
            assertThat(owned.iterator().next()).isSameAs(first);

            // Existing relationship helpers need a writable, live collection, even when
            // the caller originally supplied an immutable or independently owned container.
            owned.remove(null);
            owned.remove(first);
            owned.add(first);
            assertThat(getter.get()).containsExactly(second, first);
            setter.accept(owned);
            owned.clear();
            assertThat(getter.get()).containsExactly(second, first);

            setter.accept(null);
            assertThat(getter.get()).isNull();
        });
    }

    @Test
    void criteriaCanOnlyChangeThroughRelationshipHelpers() {
        Course course = new Course();
        Criterion criterion = new Criterion();
        List<Criterion> view = course.getCriteria();

        assertThatThrownBy(() -> view.add(criterion)).isInstanceOf(UnsupportedOperationException.class);
        course.addCriterion(criterion);
        assertThat(view).containsExactly(criterion);
        assertThat(criterion.getCourse()).isSameAs(course);
        assertThatThrownBy(view::clear).isInstanceOf(UnsupportedOperationException.class);

        course.removeCriterion(criterion);
        assertThat(view).isEmpty();
        assertThat(criterion.getCourse()).isNull();
    }

    @Test
    void ratingMembershipIsOwnedWhileScoresAndEntityIdentityRemainMutable() {
        Criterion criterion = new Criterion("Quality", "Quality of work", 10.0);
        Rating first = new Rating(criterion, 2.0);
        Rating second = new Rating(criterion, 3.0);
        List<Rating> input = new ArrayList<>(List.of(first, second));
        PeerEvaluation evaluation = new PeerEvaluation();
        evaluation.setRatings(input);
        input.clear();

        List<Rating> view = evaluation.getRatings();
        assertThat(view).containsExactly(first, second);
        assertThat(first.getPeerEvaluation()).isSameAs(evaluation);
        assertThat(second.getPeerEvaluation()).isSameAs(evaluation);
        assertThat(evaluation.getTotalScore()).isEqualTo(5.0);
        assertThatThrownBy(view::clear).isInstanceOf(UnsupportedOperationException.class);

        // EvaluationService.rescore updates these same persisted entities, then recalculates.
        assertThat(view.getFirst()).isSameAs(first);
        view.getFirst().setActualScore(4.0);
        evaluation.calculateTotalScore();
        assertThat(evaluation.getTotalScore()).isEqualTo(7.0);
        evaluation.setRatings(view);
        assertThat(evaluation.getRatings()).containsExactly(first, second);
    }

    @Test
    void nullRatingsAreRejectedWithoutChangingTheExistingState() {
        PeerEvaluation evaluation = new PeerEvaluation();
        Criterion criterion = new Criterion("Quality", "Quality of work", 10.0);
        Rating rating = new Rating(criterion, 3.0);
        List<Rating> input = new ArrayList<>(List.of(rating));
        evaluation.setRatings(input);
        assertThatThrownBy(() -> evaluation.setRatings(null)).isInstanceOf(NullPointerException.class);
        assertThat(evaluation.getRatings()).containsExactly(rating);
        assertThat(evaluation.getRatings().getFirst()).isSameAs(rating);
        assertThat(evaluation.getTotalScore()).isEqualTo(3.0);
        assertThat(rating.getPeerEvaluation()).isSameAs(evaluation);
        assertThat(input).containsExactly(rating);

        PeerEvaluation fresh = new PeerEvaluation();
        assertThatThrownBy(() -> fresh.setRatings(null)).isInstanceOf(NullPointerException.class);
        assertThat(fresh.getRatings()).isEmpty();
        assertThat(fresh.getTotalScore()).isNull();
        assertThatThrownBy(() -> new PeerEvaluation("2026-W37", null, null, null, null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void emptyRatingsAreCopiedAndProduceAZeroTotal() {
        Criterion criterion = new Criterion("Quality", "Quality of work", 10.0);
        PeerEvaluation evaluation = new PeerEvaluation();
        evaluation.setRatings(List.of(new Rating(criterion, 3.0)));
        List<Rating> input = new ArrayList<>();

        evaluation.setRatings(input);
        input.add(new Rating(criterion, 4.0));
        assertThat(evaluation.getRatings()).isEmpty();
        assertThat(evaluation.getTotalScore()).isEqualTo(0.0);
    }

    @Test
    void ratingsAreReplacedOnlyAfterTheInputHasBeenCopied() {
        Criterion criterion = new Criterion("Quality", "Quality of work", 10.0);
        PeerEvaluation evaluation = new PeerEvaluation();
        Rating original = new Rating(criterion, 3.0);
        evaluation.setRatings(List.of(original));
        Rating first = new Rating(criterion, 4.0);
        Rating second = new Rating(criterion, 5.0);
        AtomicBoolean observedCopy = new AtomicBoolean();
        List<Rating> input = new ArrayList<>(List.of(first, second)) {
            @Override
            public Object[] toArray() {
                observedCopy.set(true);
                assertThat(evaluation.getRatings()).containsExactly(original);
                assertThat(evaluation.getTotalScore()).isEqualTo(3.0);
                assertThat(original.getPeerEvaluation()).isSameAs(evaluation);
                assertThat(first.getPeerEvaluation()).isNull();
                assertThat(second.getPeerEvaluation()).isNull();
                return super.toArray();
            }
        };

        evaluation.setRatings(input);
        assertThat(observedCopy).isTrue();
        input.clear();
        assertThat(evaluation.getRatings()).containsExactly(first, second);
        assertThat(evaluation.getRatings().getFirst()).isSameAs(first);
        assertThat(evaluation.getRatings().getLast()).isSameAs(second);
        assertThat(first.getPeerEvaluation()).isSameAs(evaluation);
        assertThat(second.getPeerEvaluation()).isSameAs(evaluation);
        assertThat(evaluation.getTotalScore()).isEqualTo(9.0);
        // This raw setter has never detached the previous rating's parent reference.
        assertThat(original.getPeerEvaluation()).isSameAs(evaluation);
    }

    @Test
    void failedToArrayLeavesTheStoredRatingsAndScoreUntouched() {
        Criterion criterion = new Criterion("Quality", "Quality of work", 10.0);
        PeerEvaluation evaluation = new PeerEvaluation();
        Rating original = new Rating(criterion, 3.0);
        evaluation.setRatings(List.of(original));
        Rating candidate = new Rating(criterion, 4.0);
        IllegalStateException failure = new IllegalStateException("Unable to copy ratings");
        List<Rating> input = new ArrayList<>(List.of(candidate)) {
            @Override
            public Object[] toArray() {
                throw failure;
            }
        };

        assertThatThrownBy(() -> evaluation.setRatings(input)).isSameAs(failure);
        assertThat(evaluation.getRatings()).containsExactly(original);
        assertThat(evaluation.getRatings().getFirst()).isSameAs(original);
        assertThat(evaluation.getTotalScore()).isEqualTo(3.0);
        assertThat(original.getPeerEvaluation()).isSameAs(evaluation);
        assertThat(candidate.getPeerEvaluation()).isNull();
        assertThat(input.getFirst()).isSameAs(candidate);
    }

    @Test
    void failurePartwayThroughCopyingDoesNotReparentAnyRatings() {
        Criterion criterion = new Criterion("Quality", "Quality of work", 10.0);
        PeerEvaluation evaluation = new PeerEvaluation();
        Rating original = new Rating(criterion, 3.0);
        evaluation.setRatings(List.of(original));
        Rating candidate = new Rating(criterion, 4.0);
        IllegalStateException failure = new IllegalStateException("Unable to read the second rating");
        List<Rating> input = new AbstractList<>() {
            @Override
            public int size() {
                return 2;
            }

            @Override
            public Rating get(int index) {
                if (index == 0) {
                    return candidate;
                }
                throw failure;
            }
        };

        assertThatThrownBy(() -> evaluation.setRatings(input)).isSameAs(failure);
        assertThat(evaluation.getRatings()).containsExactly(original);
        assertThat(evaluation.getTotalScore()).isEqualTo(3.0);
        assertThat(original.getPeerEvaluation()).isSameAs(evaluation);
        assertThat(candidate.getPeerEvaluation()).isNull();
    }

    @Test
    void activeWeeksCannotBeChangedThroughTheRequestListOrGetter() {
        Section section = new Section();
        List<String> input = new ArrayList<>();
        input.add("2026-W37");
        input.add("2026-W37");
        input.add(null);
        section.setActiveWeeks(input);
        input.clear();
        List<String> returned = section.getActiveWeeks();
        assertThat(returned).containsExactly("2026-W37", "2026-W37", null);
        returned.clear();
        assertThat(section.getActiveWeeks()).containsExactly("2026-W37", "2026-W37", null);
        section.setActiveWeeks(null);
        assertThat(section.getActiveWeeks()).isNull();
    }

    @Test
    void relationshipHelpersStillUpdateOwnedCollectionsAndBackReferences() {
        Instructor instructor = new Instructor();
        instructor.setCourses(Set.of());
        instructor.setSections(Set.of());
        instructor.setTeams(List.of());
        Course course = new Course();
        Section section = new Section();
        Team team = new Team();
        course.addInstructor(instructor);
        section.addInstructor(instructor);
        team.addInstructor(instructor);
        assertThat(instructor.getCourses()).containsExactly(course);
        assertThat(instructor.getSections()).containsExactly(section);
        assertThat(instructor.getTeams()).containsExactly(team);

        CommentThread thread = new CommentThread();
        thread.setComments(List.of());
        Comment comment = new Comment();
        thread.addComment(comment);
        assertThat(comment.getCommentThread()).isSameAs(thread);
        thread.removeComment(comment);
        assertThat(thread.getComments()).isEmpty();
        assertThat(comment.getCommentThread()).isNull();

        DocumentSection documentSection = new DocumentSection();
        documentSection.setRequirementArtifacts(List.of());
        RequirementArtifact artifact = new RequirementArtifact();
        documentSection.addRequirementArtifact(artifact);
        assertThat(artifact.getSourceDocumentSection()).isSameAs(documentSection);
        documentSection.replaceAllRequirementArtifacts(List.of());
        assertThat(artifact.getSourceDocumentSection()).isNull();

        RequirementDocument document = new RequirementDocument();
        document.setSections(List.of());
        document.addSection(documentSection);
        assertThat(documentSection.getDocument()).isSameAs(document);
        document.removeSection(documentSection);
        assertThat(documentSection.getDocument()).isNull();

        artifact.setOutgoingArtifactLinks(Set.of());
        artifact.setIncomingArtifactLinks(Set.of());
        ArtifactLink link = new ArtifactLink();
        artifact.addOutgoingLink(link);
        artifact.addIncomingLink(link);
        assertThat(link.getSourceArtifact()).isSameAs(artifact);
        assertThat(link.getTargetArtifact()).isSameAs(artifact);
        artifact.removeOutgoingLink(link);
        artifact.removeIncomingLink(link);
        assertThat(link.getSourceArtifact()).isNull();
        assertThat(link.getTargetArtifact()).isNull();

        UseCase useCase = new UseCase();
        useCase.setMainSteps(List.of());
        useCase.setSecondaryActors(Set.of());
        useCase.setPreconditions(Set.of());
        useCase.setPostconditions(Set.of());
        UseCaseMainStep step = new UseCaseMainStep();
        useCase.addMainStep(step);
        useCase.addSecondaryActor(artifact);
        useCase.addPrecondition(artifact);
        useCase.addPostcondition(artifact);
        assertThat(step.getUseCase()).isSameAs(useCase);
        assertThat(useCase.getSecondaryActors()).containsExactly(artifact);
        assertThat(useCase.getPreconditions()).containsExactly(artifact);
        assertThat(useCase.getPostconditions()).containsExactly(artifact);
        useCase.removeMainStep(step);
        assertThat(step.getUseCase()).isNull();
    }
}
