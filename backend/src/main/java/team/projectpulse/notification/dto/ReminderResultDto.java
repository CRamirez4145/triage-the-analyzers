package team.projectpulse.notification.dto;

import team.projectpulse.notification.SubmittableItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @param sent   how many reminders the mail server accepted; 0 with no failures means nobody owed the item
 * @param failed the students who could not be reached
 */
public record ReminderResultDto(SubmittableItem item,
                                String week,
                                int sent,
                                List<OwingStudentDto> failed) {

    // Own collection membership while preserving nulls, encounter order, and element identity.
    public ReminderResultDto {
        failed = failed == null ? null : new ArrayList<>(failed);
    }

    @Override
    public List<OwingStudentDto> failed() {
        List<OwingStudentDto> result = failed;
        if (result != null) {
            result = new ArrayList<>(result);
        }
        return result;
    }
}
