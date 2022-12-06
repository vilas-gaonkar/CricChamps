package cric.champs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UmpiresMatches {

    private long umpireId;

    private long matchId;

    private int matchNumber;

    @Pattern(regexp = "^[A-Za-z]+\\s+|\\s+[A-Za-z]+\\s+$", message = "Umpire name should only contain alphabets")
    private String umpireName;

    private String isDeleted;
}
