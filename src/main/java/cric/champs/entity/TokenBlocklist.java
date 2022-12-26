package cric.champs.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TokenBlocklist {

    private String token;

    private LocalDateTime expirationAt;

}
