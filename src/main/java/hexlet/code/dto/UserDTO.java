package hexlet.code.dto;

import lombok.Setter;
import lombok.Getter;

import java.time.LocalDateTime;


@Setter
@Getter
public class UserDTO {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
