package cric.champs.entity;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Grounds {

    private long groundId;

    private long tournamentId;

    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Ground name should only contain alphabets")
    private String groundName;

    private String city;

    private String groundLocation;


    private double latitude;


    private double longitude;

    private String isDeleted;
}
