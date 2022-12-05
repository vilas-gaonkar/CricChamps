package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Tokens {

    private String secureToken;

    @NotBlank(message = "userID cannot be null or empty.")
    private String userId;

    private LocalDateTime createAt;

    private LocalDateTime expireAt;

    private String tokenStatus;

}
