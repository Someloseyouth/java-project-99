package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.List;

@Setter
@Getter
public class TaskUpdateDTO {
    private JsonNullable<List<Long>> labelIds;
    private JsonNullable<Integer> index;
    private JsonNullable<Long> assigneeId;
    private JsonNullable<String> title;
    private JsonNullable<String> content;
    private JsonNullable<String> status;
}
