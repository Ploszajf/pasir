package pk.fp.pasir_ploszaj_filip.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LoginDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
