package cric.champs.resultmodels;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NameResult {

    private long id;

    private String name;

    private String city;

    private String photo;
}
