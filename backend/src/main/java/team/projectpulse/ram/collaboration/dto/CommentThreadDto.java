package team.projectpulse.ram.collaboration.dto;

import team.projectpulse.ram.collaboration.CommentThreadStatus;
import team.projectpulse.user.dto.PeerEvaluationUserDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record CommentThreadDto(Long id,
                               CommentThreadStatus status,
                               Instant createdAt,
                               PeerEvaluationUserDto createdBy,
                               List<CommentDto> comments) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public CommentThreadDto {
        comments = comments == null ? null : new ArrayList<>(comments);
    }

    @Override
    public List<CommentDto> comments() {
        List<CommentDto> result = comments;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }
}
