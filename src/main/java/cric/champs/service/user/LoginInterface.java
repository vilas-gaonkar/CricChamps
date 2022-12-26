package cric.champs.service.user;

import cric.champs.customexceptions.*;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.entity.Users;

import javax.servlet.http.HttpServletRequest;

public interface LoginInterface {

    String signIn(String userName, String password) throws LoginFailedException, NotVerifiedException;

    String refreshToken(HttpServletRequest httpServletRequest);

    SuccessResultModel signUp(Users user) throws SignupException;

    SuccessResultModel forgotPassword(String username) throws UsernameNotFoundExceptions, OTPGenerateException;

    boolean resetPassword(int otp, String email);

    SuccessResultModel changePassword(String newPassword);

    SuccessResultModel resetPassword(String newPassword, String email);

    SuccessResultModel changeProfilePhoto(String photoLink) throws UpdateFailedException;

    SuccessResultModel deleteOldProfilePhoto();

    Users getUserDetails();

    SuccessResultModel logOut(HttpServletRequest httpServletRequest);

}
