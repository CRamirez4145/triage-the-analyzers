package team.projectpulse.ram.requirement.dto;

import java.util.ArrayList;
import java.util.List;

public record ArtifactTraceability(Long artifactId,
                                   List<ArtifactLinkViewDto> incoming,
                                   List<ArtifactLinkViewDto> outgoing) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public ArtifactTraceability {
        incoming = incoming == null ? null : new ArrayList<>(incoming);
        outgoing = outgoing == null ? null : new ArrayList<>(outgoing);
    }

    @Override
    public List<ArtifactLinkViewDto> incoming() {
        List<ArtifactLinkViewDto> result = incoming;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }

    @Override
    public List<ArtifactLinkViewDto> outgoing() {
        List<ArtifactLinkViewDto> result = outgoing;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }
}
