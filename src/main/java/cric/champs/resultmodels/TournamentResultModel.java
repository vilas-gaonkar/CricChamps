package cric.champs.resultmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TournamentResultModel {
    private String tournamentName;

    private String tournamentCode;

    private String message;
}
