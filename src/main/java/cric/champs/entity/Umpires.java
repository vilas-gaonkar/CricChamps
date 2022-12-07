package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Umpires {

    private long umpireId;

    private long tournamentId;

    @Pattern(regexp = "^[A-Za-z]+\\s+|\\s+[A-Za-z]+\\s+|[A-Za-z]+$", message = "Umpire name should only contain alphabets")
    private String umpireName;

    @Pattern(regexp = "^[A-Za-z]+\\s+|\\s+[A-Za-z]+\\s+|[A-Za-z]+$", message = "City name should only contain alphabets")
    private String city;

    private String phoneNumber;

    private String umpirePhoto;

    private String isDeleted;
}
