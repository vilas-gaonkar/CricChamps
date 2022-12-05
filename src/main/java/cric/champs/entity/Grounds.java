package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Grounds {

    private long groundId;

    private long tournamentId;

    private String groundName;

    private String city;

    private String groundLocation;

    private String groundPhoto;

    private double latitude;

    private double longitude;

    private String isDeleted;
}
