package team.projectpulse.ram.usecase.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.requirement.Priority;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record UseCaseDto(
        Long id,
        String artifactKey,
        @NotEmpty(message = "Use case title must not be empty.")
        String title,
        @NotEmpty(message = "Use case description must not be empty.")
        String description,
        @NotNull(message = "Team ID is must not be null.")
        Integer teamId,
        @NotNull(message = "Primary actor ID must not be null.")
        Long primaryActorId,
        Set<Long> secondaryActorIds,
        @NotEmpty(message = "Use case trigger must not be empty.")
        String trigger,
        Set<ConditionDto> preconditions,
        Set<ConditionDto> postconditions,
        List<UseCaseMainStepDto> mainSteps,
        Priority priority,
        String notes,
        Instant createdAt,
        Instant updatedAt,
        PeerEvaluationUserDto createdBy,
        PeerEvaluationUserDto updatedBy,
        Integer version,
        UseCaseLockDto lock) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public UseCaseDto {
        mainSteps = mainSteps == null ? null : new ArrayList<>(mainSteps);
        postconditions = postconditions == null ? null : new LinkedHashSet<>(postconditions);
        preconditions = preconditions == null ? null : new LinkedHashSet<>(preconditions);
        secondaryActorIds = secondaryActorIds == null ? null : new LinkedHashSet<>(secondaryActorIds);
    }

    @Override
    public List<UseCaseMainStepDto> mainSteps() {
        List<UseCaseMainStepDto> result = mainSteps;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }

    @Override
    public Set<ConditionDto> postconditions() {
        Set<ConditionDto> result = postconditions;
        if (result != null) {
            result = new LinkedHashSet<>(result);
        }
        return result;
    }

    @Override
    public Set<ConditionDto> preconditions() {
        Set<ConditionDto> result = preconditions;
        if (result != null) {
            result = new LinkedHashSet<>(result);
        }
        return result;
    }

    @Override
    public Set<Long> secondaryActorIds() {
        Set<Long> result = secondaryActorIds;
        if (result != null) {
            result = new LinkedHashSet<>(result);
        }
        return result;
    }
}
