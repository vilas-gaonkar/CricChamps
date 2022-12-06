package cric.champs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UmpiresMatches {

    private long umpireId;

    private long matchId;

    private int matchNumber;

    private String umpireName;

    private String isDeleted;
}
