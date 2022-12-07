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

    private String groundName;

    @Pattern(regexp = "^[A-Za-z]+\\s+|\\s+[A-Za-z]+\\s+|[A-Za-z]+$", message = "City name should only contain alphabets")
    private String city;

    private String groundLocation;

    private String groundPhoto;

    @NotBlank(message = "Latitude cannot be empty")
    private double latitude;

    @NotBlank(message = "Longitude cannot be empty")
    private double longitude;

    private String isDeleted;
}
