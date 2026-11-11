package team.projectpulse.ram.document.dto;

import team.projectpulse.ram.requirement.SectionType;
import team.projectpulse.ram.requirement.dto.RequirementArtifactDto;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record DocumentSectionDto(Long id,
                                 String sectionKey,
                                 SectionType type,
                                 String title,
                                 String content,
                                 List<RequirementArtifactDto> requirementArtifacts,
                                 String guidance,
                                 Instant createdAt,
                                 Instant updatedAt,
                                 PeerEvaluationUserDto createdBy,
                                 PeerEvaluationUserDto updatedBy,
                                 Integer version,
                                 DocumentSectionLockDto lock) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public DocumentSectionDto {
        requirementArtifacts = requirementArtifacts == null ? null : new ArrayList<>(requirementArtifacts);
    }

    @Override
    public List<RequirementArtifactDto> requirementArtifacts() {
        List<RequirementArtifactDto> result = requirementArtifacts;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }
}
