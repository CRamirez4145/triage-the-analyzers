package team.projectpulse.ram.document;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Isolated("Changes the JVM default locale")
@ResourceLock(Resources.LOCALE)
class RequirementDocumentSpecsTest {

    @Mock
    Root<RequirementDocument> root;
    @Mock
    CriteriaQuery<?> query;
    @Mock
    CriteriaBuilder criteriaBuilder;
    @Mock
    Path<String> title;
    @Mock
    Expression<String> lowercaseTitle;

    @Test
    void titleSearchPatternIsIndependentOfTheDefaultLocale() {
        when(root.<String>get("title")).thenReturn(title);
        when(criteriaBuilder.lower(title)).thenReturn(lowercaseTitle);
        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);
        Locale original = Locale.getDefault();
        Locale originalDisplay = Locale.getDefault(Locale.Category.DISPLAY);
        Locale originalFormat = Locale.getDefault(Locale.Category.FORMAT);

        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            RequirementDocumentSpecs.hasTitleLike("TITLE").toPredicate(root, query, criteriaBuilder);

            verify(criteriaBuilder).like(eq(lowercaseTitle), pattern.capture());
            assertThat(pattern.getValue())
                    .as("Title-search LIKE pattern must not depend on the server locale")
                    .isEqualTo("%title%");
        } finally {
            Locale.setDefault(original);
            Locale.setDefault(Locale.Category.DISPLAY, originalDisplay);
            Locale.setDefault(Locale.Category.FORMAT, originalFormat);
        }
    }
}
