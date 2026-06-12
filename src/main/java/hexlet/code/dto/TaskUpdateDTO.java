package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
public class TaskUpdateDTO {
    private Set<Long> taskLabelIds = new HashSet<>();

    private JsonNullable<Integer> index;

    private JsonNullable<Long> assigneeId;

    private JsonNullable<String> title;

    private JsonNullable<String> content;

    private JsonNullable<String> status;
}
