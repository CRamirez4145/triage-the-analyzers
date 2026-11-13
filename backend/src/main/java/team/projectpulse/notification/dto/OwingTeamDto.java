package team.projectpulse.notification.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * One team's students who owe an item, as the dialog and the report pages group them.
 */
public record OwingTeamDto(Integer teamId,
                           String teamName,
                           List<OwingStudentDto> students) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public OwingTeamDto {
        students = students == null ? null : new ArrayList<>(students);
    }

    @Override
    public List<OwingStudentDto> students() {
        List<OwingStudentDto> result = students;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }
}
