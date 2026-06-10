package hexlet.code.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LabelCreateDTO {
    @NotNull
    @Size(min = 3, max = 1000)
    private String name;
}
