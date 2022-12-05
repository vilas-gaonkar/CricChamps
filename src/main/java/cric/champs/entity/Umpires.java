package cric.champs.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Umpires {

    private long umpireId;

    private long tournamentId;

    private String umpireName;

    private String city;

    private String phoneNumber;

    private String umpirePhoto;

    private String isDeleted;
}
