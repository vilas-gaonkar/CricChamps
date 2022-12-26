package cric.champs.setting.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HelpAndFAQs {

    private int questionId;

    private String question;

    private String answer;

}
