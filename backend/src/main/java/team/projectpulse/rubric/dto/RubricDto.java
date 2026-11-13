package team.projectpulse.rubric.dto;

import java.util.LinkedHashSet;
import java.util.Set;

public record RubricDto(
        Integer rubricId,
        String rubricName,
        Set<CriterionDto> criteria,
        Integer courseId) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public RubricDto {
        criteria = criteria == null ? null : new LinkedHashSet<>(criteria);
    }

    @Override
    public Set<CriterionDto> criteria() {
        Set<CriterionDto> result = criteria;
        if (result != null) {
            result = new LinkedHashSet<>(result);
        }
        return result;
    }
}
