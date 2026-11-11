package team.projectpulse;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import team.projectpulse.evaluation.dto.PeerEvaluationDto;
import team.projectpulse.notification.dto.OwingTeamDto;
import team.projectpulse.notification.dto.ReminderResultDto;
import team.projectpulse.notification.dto.SubmissionStatusDto;
import team.projectpulse.ram.collaboration.dto.CommentThreadDto;
import team.projectpulse.ram.document.dto.DocumentSectionDto;
import team.projectpulse.ram.document.dto.RequirementDocumentDto;
import team.projectpulse.ram.requirement.dto.ArtifactTraceability;
import team.projectpulse.ram.usecase.dto.UseCaseDto;
import team.projectpulse.ram.usecase.dto.UseCaseExtensionDto;
import team.projectpulse.ram.usecase.dto.UseCaseMainStepDto;
import team.projectpulse.rubric.dto.RubricDto;
import team.projectpulse.section.dto.SectionDto;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DtoCollectionOwnershipTest {

    @TestFactory
    Stream<DynamicTest> collectionMembershipIsOwnedByEachRecord() {
        return Stream.of(
                PeerEvaluationDto.class,
                OwingTeamDto.class,
                ReminderResultDto.class,
                SubmissionStatusDto.class,
                CommentThreadDto.class,
                DocumentSectionDto.class,
                RequirementDocumentDto.class,
                ArtifactTraceability.class,
                UseCaseDto.class,
                UseCaseExtensionDto.class,
                UseCaseMainStepDto.class,
                RubricDto.class,
                SectionDto.class
        ).flatMap(type -> Arrays.stream(type.getRecordComponents())
                .filter(component -> Collection.class.isAssignableFrom(component.getType()))
                .map(component -> DynamicTest.dynamicTest(type.getSimpleName() + "." + component.getName(),
                        () -> verifyOwnership(type, component))));
    }

    private void verifyOwnership(Class<?> type, RecordComponent component) throws ReflectiveOperationException {
        Object first = new Object();
        Object second = new Object();
        Collection<Object> input = component.getType() == Set.class ? new LinkedHashSet<>() : new ArrayList<>();
        input.add(second);
        input.add(null);
        input.add(first);
        input.add(second);
        List<?> expected = new ArrayList<>(input);
        Object record = construct(type, component, input);
        input.clear();
        Collection<?> accessed = (Collection<?>) component.getAccessor().invoke(record);
        assertEquals(expected, new ArrayList<>(accessed));
        assertSame(second, accessed.iterator().next());
        accessed.clear();
        Collection<?> again = (Collection<?>) component.getAccessor().invoke(record);
        assertEquals(expected, new ArrayList<>(again));
        assertNotSame(accessed, again);

        Object empty = construct(type, component, input);
        input.add(first);
        assertTrue(((Collection<?>) component.getAccessor().invoke(empty)).isEmpty());
        Object absent = construct(type, component, null);
        assertNull(component.getAccessor().invoke(absent));
    }

    private Object construct(Class<?> type, RecordComponent selected, Object value) throws ReflectiveOperationException {
        RecordComponent[] components = type.getRecordComponents();
        Class<?>[] types = Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
        Object[] arguments = new Object[components.length];
        for (int index = 0; index < components.length; index++) {
            if (components[index].getName().equals(selected.getName())) {
                arguments[index] = value;
            } else if (types[index] == boolean.class) {
                arguments[index] = false;
            } else if (types[index] == int.class) {
                arguments[index] = 0;
            }
        }
        return type.getDeclaredConstructor(types).newInstance(arguments);
    }
}
