package hexlet.code.dto;

import lombok.Setter;
import lombok.Getter;

import java.time.LocalDate;


@Setter
@Getter
public class TaskStatusDTO {
    private Long id;

    private String name;

    private String slug;

    private LocalDate createdAt;
}
