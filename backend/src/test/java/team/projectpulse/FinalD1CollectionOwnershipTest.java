package team.projectpulse;

import org.junit.jupiter.api.Test;
import team.projectpulse.notification.ReminderService.ReminderResult;
import team.projectpulse.ram.document.DocumentType;
import team.projectpulse.ram.document.template.DocumentTemplate;
import team.projectpulse.ram.document.template.DocumentTemplate.SectionTemplate;
import team.projectpulse.ram.document.template.DocumentTemplateRegistry;
import team.projectpulse.ram.requirement.SectionType;
import team.projectpulse.student.Student;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FinalD1CollectionOwnershipTest {

    @Test
    void reminderResultCopiesMembershipOnInputAndOutput() {
        Student first = new Student();
        Student second = new Student();
        List<Student> input = new ArrayList<>(Arrays.asList(second, null, first, second));
        ReminderResult result = new ReminderResult(3, input);
        input.clear();
        List<Student> output = result.failed();
        assertThat(output).containsExactly(second, null, first, second);
        assertThat(output.getFirst()).isSameAs(second);
        output.clear();
        output.add(first);
        assertThat(result.failed()).containsExactly(second, null, first, second);
        assertThat(result.sent()).isEqualTo(3);
    }

    @Test
    void reminderResultPreservesNullAndEmptyLists() {
        assertThat(new ReminderResult(0, null).failed()).isNull();
        List<Student> input = new ArrayList<>();
        ReminderResult result = new ReminderResult(0, input);
        input.add(new Student());
        assertThat(result.failed()).isEmpty();
        result.failed().add(new Student());
        assertThat(result.failed()).isEmpty();
    }

    @Test
    void templateCopiesMembershipOnInputAndOutputWithoutCloningSections() {
        SectionTemplate first = new SectionTemplate();
        SectionTemplate second = new SectionTemplate();
        List<SectionTemplate> input = new ArrayList<>(Arrays.asList(second, null, first, second));
        DocumentTemplate template = new DocumentTemplate();
        template.setSections(input);
        input.clear();
        List<SectionTemplate> output = template.getSections();
        assertThat(output).containsExactly(second, null, first, second);
        assertThat(output.getFirst()).isSameAs(second);
        output.clear();
        output.add(first);
        assertThat(template.getSections()).containsExactly(second, null, first, second);
        second.setTitle("Updated guidance section");
        assertThat(template.getSections().getFirst().getTitle()).isEqualTo("Updated guidance section");
    }

    @Test
    void templatePreservesDefaultEmptyExplicitEmptyAndNullStates() {
        DocumentTemplate template = new DocumentTemplate();
        template.getSections().add(new SectionTemplate());
        assertThat(template.getSections()).isEmpty();
        template.setSections(null);
        assertThat(template.getSections()).isNull();
        List<SectionTemplate> input = new ArrayList<>();
        template.setSections(input);
        input.add(new SectionTemplate());
        assertThat(template.getSections()).isEmpty();
    }

    @Test
    void yamlUsesSetterAndPreservesSectionOrderAndProperties() {
        YAMLMapper mapper = YAMLMapper.builder().findAndAddModules().build();
        DocumentTemplate template = mapper.readValue("""
                title: Example
                sections:
                  - sectionKey: SECOND
                    title: Second
                    type: LIST
                    guidance: Keep this guidance
                  - sectionKey: FIRST
                    title: First
                    type: RICH_TEXT
                """, DocumentTemplate.class);
        assertThat(template.getTitle()).isEqualTo("Example");
        assertThat(template.getSections()).extracting(SectionTemplate::getSectionKey)
                .containsExactly("SECOND", "FIRST");
        assertThat(template.getSections().getFirst().getType()).isEqualTo(SectionType.LIST);
        assertThat(template.getSections().getFirst().getGuidance()).isEqualTo("Keep this guidance");
        template.getSections().clear();
        assertThat(template.getSections()).hasSize(2);
    }

    @Test
    void yamlPreservesMissingNullAndEmptySections() {
        YAMLMapper mapper = YAMLMapper.builder().findAndAddModules().build();
        assertThat(mapper.readValue("title: Missing\n", DocumentTemplate.class).getSections()).isEmpty();
        assertThat(mapper.readValue("sections: null\n", DocumentTemplate.class).getSections()).isNull();
        assertThat(mapper.readValue("sections: []\n", DocumentTemplate.class).getSections()).isEmpty();
    }

    @Test
    void productionYamlTemplatesRemainLoadedAndCannotLoseSectionsThroughGetters() {
        DocumentTemplateRegistry registry = new DocumentTemplateRegistry();
        registry.loadAllTemplates();
        for (DocumentType type : List.of(DocumentType.SRS, DocumentType.VISION_SCOPE)) {
            DocumentTemplate template = registry.find(type).orElseThrow();
            List<String> keys = template.getSections().stream().map(SectionTemplate::getSectionKey).toList();
            assertThat(keys).isNotEmpty();
            assertThat(keys.getFirst()).isEqualTo("INTRODUCTION");
            template.getSections().clear();
            assertThat(registry.find(type).orElseThrow().getSections())
                    .extracting(SectionTemplate::getSectionKey).containsExactlyElementsOf(keys);
        }
    }
}
