package hexlet.code.dto;

import jakarta.validation.constraints.Size;
import lombok.Setter;
import lombok.Getter;


@Setter
@Getter
public class TaskCreateDTO {
    private Integer index;
    private Long assigneeId;
    @Size(min = 1)
    private String title;
    private String content;
    @Size(min = 1)
    private String status;
}
