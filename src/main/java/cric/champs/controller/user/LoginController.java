package cric.champs.controller.user;

import com.cloudinary.utils.ObjectUtils;
import cric.champs.customexceptions.*;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.entity.Users;
import cric.champs.service.AccountStatus;
import cric.champs.service.cloud.UploadImageTOCloud;
import cric.champs.service.system.SystemInterface;
import cric.champs.service.user.LoginInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
public class LoginController {

    @Autowired
    private LoginInterface loginInterface;

    @Autowired
    private UploadImageTOCloud uploadImageTOCloud;

    @Autowired
    private SystemInterface systemInterface;

    @SuppressWarnings("rawtypes")
    @PostMapping("/sign-up")
    public ResponseEntity<SuccessResultModel> register(@ModelAttribute @Valid Users user, @RequestPart @Nullable MultipartFile profilePhoto)
            throws IOException, SignupException {

        Map result = null;
        if (profilePhoto == null)
            user.setProfilePicture(null);
        else if (profilePhoto.isEmpty())
            user.setProfilePicture(null);
        else
            result = uploadImageTOCloud.uploadImage(profilePhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        if (result != null)
            user.setProfilePicture(result.get("url").toString());

        return ResponseEntity.of(Optional.of(loginInterface.signUp(user)));

    }

    @PostMapping("/login")
    public HttpEntity<?> login(@RequestHeader @Email(message = "enter valid email") String email,
                               @RequestHeader @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,250}$",
                                       message = "Please provide password at least one uppercase letter,one lowercase letter,one number and " +
                                               "one special character with minimum length 8 maximum length 250")
                               String password) throws LoginFailedException, NotVerifiedException {
        String result = loginInterface.signIn(email, password);
        if (result == null)
            return new HttpEntity<>(Collections.singletonMap("Message", "Invalid credentials"));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", result);
        return new HttpEntity<>(systemInterface.getUserDetails(email, AccountStatus.VERIFIED.toString()).get(0), headers);
    }

    @GetMapping("/refresh-token")
    public HttpEntity<?> generateRefreshToken(HttpServletRequest httpServletRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", loginInterface.refreshToken(httpServletRequest));
        return new HttpEntity<>(Collections.singletonMap("message", "Token generated successfully"), headers);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<SuccessResultModel> resetPassword(@RequestHeader @Email(message = "enter valid email") String email,
                                                            @RequestHeader @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,250}$",
                                                                    message = "Please provide password at least one uppercase letter,one lowercase letter,one number and "
                                                                            + "one special character with minimum length 8 maximum length 250") String newPassword,
                                                            @RequestHeader @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,250}$",
                                                                    message = "Please provide password at least one uppercase letter,one lowercase letter,one number and " +
                                                                            "one special character with minimum length 8 maximum length 250") String confirmPassword) throws UpdateFailedException {
        return ResponseEntity.of(Optional.of(loginInterface.resetPassword(newPassword, confirmPassword, email)));
    }

    @PatchMapping("/forgot-password")
    public ResponseEntity<SuccessResultModel> forgotPassword(@RequestHeader @Email(message = "enter valid email") String email)
            throws UsernameNotFoundException, OTPGenerateException {
        return ResponseEntity.of(Optional.of(loginInterface.forgotPassword(email)));
    }

    @PatchMapping("/reset")
    public ResponseEntity<String> verifyOTP(@RequestParam @Min(value = 100000, message = "enter valid otp")
                                            @Max(value = 999999, message = "enter valid otp") int otp,
                                            @RequestHeader @Email(message = "enter valid email") String email) {
        HttpHeaders responseHeaders = new HttpHeaders();
        if (loginInterface.resetPassword(otp, email)) {
            responseHeaders.set("isVerified", "true");
            return ResponseEntity.ok().headers(responseHeaders).body("Verified successfully");
        }
        responseHeaders.set("isVerified", "false");
        return ResponseEntity.ok().headers(responseHeaders).body("Invalid OTP");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<SuccessResultModel> sendOtp(@RequestHeader @Email(message = "enter valid email") String email) throws Exception {
        return ResponseEntity.of(Optional.of(systemInterface.sendOTP(email)));
    }

    @PatchMapping("/verify")
    public ResponseEntity<SuccessResultModel> verifyEmail(@RequestParam int otp, @RequestHeader String email) throws Exception {
        return ResponseEntity.of(Optional.of(systemInterface.verifyUserAccount(otp, email)));
    }

    @SuppressWarnings("rawtypes")
    @PatchMapping("/user/change/profile-photo")
    public ResponseEntity<SuccessResultModel> changeProfilePhoto(@RequestPart @Nullable MultipartFile profilePhoto) throws IOException, UpdateFailedException {
        Map result;
        if (profilePhoto == null)
            return ResponseEntity.of(Optional.of(loginInterface.changeProfilePhoto(null)));
        else if (profilePhoto.isEmpty())
            return ResponseEntity.of(Optional.of(loginInterface.changeProfilePhoto(null)));
        else
            result = uploadImageTOCloud.uploadImage(profilePhoto.getBytes(), ObjectUtils.asMap("resource type", "auto"));

        return ResponseEntity.of(Optional.of(loginInterface.changeProfilePhoto(result.get("url").toString())));
    }

    @PatchMapping("/user/change/password")
    public ResponseEntity<SuccessResultModel> changePassword(@RequestHeader @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,250}$",
            message = "Please provide password at least one uppercase letter,one lowercase letter,one number and " +
                    "one special character with minimum length 8 maximum length 250") String newPassword,
                                                             @RequestHeader @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,250}$",
                                                                     message = "Please provide password at least one uppercase letter,one lowercase letter,one number and " +
                                                                             "one special character with minimum length 8 maximum length 250") String confirmPassword) throws UpdateFailedException {
        return ResponseEntity.of(Optional.of(loginInterface.changePassword(newPassword, confirmPassword)));
    }

    @DeleteMapping("/user/remove/profile-photo")
    public ResponseEntity<SuccessResultModel> removeProfilePhoto() {
        return ResponseEntity.of(Optional.of(loginInterface.deleteOldProfilePhoto()));
    }

    @GetMapping("/user/details")
    public ResponseEntity<?> getUserDetails() {
        return ResponseEntity.of(Optional.of(loginInterface.getUserDetails()));
    }

}
