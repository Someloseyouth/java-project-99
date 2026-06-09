package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class TaskUpdateDTO {
    private JsonNullable<Integer> index = JsonNullable.undefined();
    private JsonNullable<Long> assigneeId = JsonNullable.undefined();
    private JsonNullable<String> title = JsonNullable.undefined();
    private JsonNullable<String> content = JsonNullable.undefined();
    private JsonNullable<String> status = JsonNullable.undefined();
}
