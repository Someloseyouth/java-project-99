package hexlet.code.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Setter;
import lombok.Getter;

@Setter
@Getter
public class UserCreateDTO {
    @NotNull
    @Email
    private String email;

    private String firstName;

    private String lastName;

    @NotNull
    @Size(min = 3)
    private String password;

    private String encryptedPassword;
}
