package cric.champs.service.user;

import cric.champs.customexceptions.*;
import cric.champs.entity.ResultModel;
import cric.champs.entity.Users;

import javax.servlet.http.HttpServletRequest;

public interface LoginInterface {

    String signIn(String userName, String password) throws LoginFailedException, NotVerifiedException;

    String refreshToken(HttpServletRequest httpServletRequest);

    ResultModel signUp(Users user) throws SignupException;

    ResultModel forgotPassword(String username) throws UsernameNotFoundException, OTPGenerateException;

    boolean resetPassword(int otp, String email);

    ResultModel changePassword(String newPassword, String confirmPassword) throws UpdateFailedException;

    ResultModel resetPassword(String newPassword, String confirmPassword, String email) throws UpdateFailedException;

    ResultModel changeProfilePhoto(String photoLink) throws UpdateFailedException;

    ResultModel deleteOldProfilePhoto();

    Users getUserDetails();

}
