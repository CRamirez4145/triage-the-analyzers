package team.projectpulse.ram.document.dto;

import jakarta.validation.constraints.NotNull;
import team.projectpulse.ram.document.DocumentStatus;
import team.projectpulse.ram.document.DocumentType;

import java.util.ArrayList;
import java.util.List;

public record RequirementDocumentDto(Long id,
                                     @NotNull(message = "Document type must not be null")
                                     DocumentType type,
                                     @NotNull(message = "Team Id must not be null")
                                     Integer teamId,
                                     String documentKey,
                                     String title,
                                     List<DocumentSectionDto> sections,
                                     DocumentStatus status,
                                     Integer version) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public RequirementDocumentDto {
        sections = sections == null ? null : new ArrayList<>(sections);
    }

    @Override
    public List<DocumentSectionDto> sections() {
        List<DocumentSectionDto> result = sections;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }
}
