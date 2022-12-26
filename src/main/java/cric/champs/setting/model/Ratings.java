package cric.champs.setting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Ratings {

    private long userId;

    private Integer numberOfStarsRated;

    private String feedback;
}
