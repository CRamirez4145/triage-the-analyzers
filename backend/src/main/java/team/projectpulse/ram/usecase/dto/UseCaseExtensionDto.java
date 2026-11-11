package team.projectpulse.ram.usecase.dto;

import team.projectpulse.ram.usecase.ExtensionExit;
import team.projectpulse.ram.usecase.ExtensionKind;

import java.util.ArrayList;
import java.util.List;

public record UseCaseExtensionDto(
        Long id,
        String conditionText,
        ExtensionKind kind,
        ExtensionExit exit,
        List<UseCaseExtensionStepDto> steps) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public UseCaseExtensionDto {
        steps = steps == null ? null : new ArrayList<>(steps);
    }

    @Override
    public List<UseCaseExtensionStepDto> steps() {
        List<UseCaseExtensionStepDto> result = steps;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }
}
