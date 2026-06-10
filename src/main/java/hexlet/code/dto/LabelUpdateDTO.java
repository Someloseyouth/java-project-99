package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class LabelUpdateDTO {
    @Size(min = 3, max = 1000)
    private JsonNullable<String> name;
}
