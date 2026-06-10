package hexlet.code.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Setter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class TaskCreateDTO {
    private List<Long> labelIds = new ArrayList<>();

    private Integer index;

    private Long assigneeId;

    @NotNull
    @Size(min = 1)
    private String title;

    private String content;

    @NotNull
    @Size(min = 1)
    private String status;
}
