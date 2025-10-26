package com.hyuri.kanji_study.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class AuthenticationDto {
    private AuthenticationDto() {}
    @Builder
    public record AuthRequest(
            @NotBlank(message = "Please enter a login ID.")
            @Pattern(
                    regexp = "^[A-Za-z0-9]+$",
                    message = "The login ID may only contain alphanumeric characters (A–Z, a–z, 0–9)."
            )
            @Size(min = 3, max = 30,
                    message = "The login ID must be 3 to 30 characters long.")
            String loginId,

            @NotBlank(message = "Please enter a password.")
            @Pattern(
                    regexp = "^[A-Za-z\\d~!@#$%^&*?_=\\-+,./:;]+$",
                    message = """
                              The password may contain letters, numbers, and the following special characters:
                              ~ ! @ # $ % ^ & * ? _ = - + , . / : ;
                              """
            )
            @Size(min = 8, max = 100,
                    message = "The password must be 8 to 100 characters long.")
            String password){
        public AuthRequest {
        }
    }

    @Builder
    public record SignUpRequest(
            @NotBlank(message = "Please enter a user id.")
            @Pattern(
                    regexp = "[A-Za-z0-9]+",
                    message = "The user id may only contain alphanumeric characters, and is case-sensitive."
            )
            @Size(min = 3, message = "The password must be at least 3 characters long.")
            @Size(max = 30, message = "The password must be no more than 30 characters long.")
            String loginId,

            @NotBlank(message = "Please enter a password.")
            @Pattern(
                    regexp = "^[A-Za-z\\d~!@#$%^&*?_=\\-+,./:;]+$",
                    message = """
                            The password may only contain letters, numbers, and special characters, and is case-sensitive.
                            Allowed special characters are "~!@#$%^&*?_=\\-+,.:;".
                            """
            )
            @Size(min = 3, message = "The password must be at least 8 characters long.")
            @Size(max = 100, message = "The password must be no more than 100 characters long.")
            String password,


            @NotBlank
            String nickname

    ) {}

    @Builder
    public record AuthResponse(
            @JsonProperty("access_token")
            String token
    ) {
    }
}
