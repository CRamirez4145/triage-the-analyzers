package team.projectpulse.ram.usecase.dto;

import java.util.ArrayList;
import java.util.List;

public record UseCaseMainStepDto(
        Long id,
        String actor,
        String actionText,
        List<UseCaseExtensionDto> extensions) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public UseCaseMainStepDto {
        extensions = extensions == null ? null : new ArrayList<>(extensions);
    }

    @Override
    public List<UseCaseExtensionDto> extensions() {
        List<UseCaseExtensionDto> result = extensions;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }
}
