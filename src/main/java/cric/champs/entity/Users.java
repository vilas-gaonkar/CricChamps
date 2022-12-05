package cric.champs.entity;

import cric.champs.customannotations.Gender;
import cric.champs.customannotations.PhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Users {

    private long userId;

    @Pattern(regexp = "^[A-Za-z]+\\s[A-Za-z]+\\s[A-Za-z]+|[A-Za-z]+\\s[A-Za-z]+|[A-Za-z]+$",
            message = "name should contain only alphabet and white space followed by character")
    private String username;

    @Gender
    private String gender;

    @Email(message = "Invalid Email.Please provide valid Email")
    private String email;

    @PhoneNumber
    private String phoneNumber;

    private String city;

    private String profilePicture;

    @Min(value = 16, message = "minimum age should be 16")
    private int age;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,250}$",
            message = "Please provide password at least one uppercase letter,one lowercase letter,one number and " +
                    "one special character with minimum length 8 maximum length 250")
    private String password;

    private String accountStatus;

    private String isDeleted;
}
