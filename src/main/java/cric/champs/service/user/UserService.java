package cric.champs.service.user;

import cric.champs.customexceptions.*;
import cric.champs.entity.*;
import cric.champs.security.userdetails.JWTUserDetailsService;
import cric.champs.security.utility.JWTUtility;
import cric.champs.service.AccountStatus;
import cric.champs.service.TournamentStatus;
import cric.champs.service.system.SystemInterface;
import cric.champs.service.system.TokenInterface;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements LoginInterface, TournamentInterface, GroundInterface, UmpiresInterface {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SystemInterface systemInterface;

    @Autowired
    private TokenInterface tokenInterface;

    @Autowired
    private JWTUtility jwtUtility;

    @Autowired
    private JWTUserDetailsService jwtUserDetailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;


    /**
     * ******Login Interface******
     */

    @Override
    public String signIn(String email, String password) throws LoginFailedException, NotVerifiedException {
        try {
            if (systemInterface.verifyEmail(email))
                return null;
            List<Users> user = systemInterface.getUserDetails(email, AccountStatus.VERIFIED.toString());
            if (user.isEmpty())
                throw new NotVerifiedException("Please verify your account before logging in");
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            final UserDetails userDetail = jwtUserDetailsService.loadUserByUsername(email);
            return "Bearer " + jwtUtility.generateToken(userDetail);
        } catch (BadCredentialsException exception) {
            throw new LoginFailedException("Invalid credentials");
        }
    }

    @Override
    public String refreshToken(HttpServletRequest httpServletRequest) {
        DefaultClaims claims = (DefaultClaims) httpServletRequest.getAttribute("claims");
        Map<String, Object> expectedMap = systemInterface.getMapFromDefaultClaim(claims);
        String refreshToken = jwtUtility.doGenerateRefreshToken(expectedMap, expectedMap.get("sub").toString());
        return "Bearer " + refreshToken;
    }

    @Override
    public ResultModel signUp(Users user) throws SignupException {
        try {
            if (!systemInterface.verifyEmail(user.getEmail()))
                return new ResultModel("This email is already registered with Cric Champs");
            jdbcTemplate.update("insert into users values(?,?,?,?,?,?,?,?,?,?,?)", null, user.getUsername(), user.getGender(),
                    user.getEmail(), user.getPhoneNumber(), user.getCity(), user.getProfilePicture(), user.getAge(),
                    passwordEncoder.encode(user.getPassword()), AccountStatus.NOTVERIFIED.toString(), "false");
            return new ResultModel("Cric Champs account created successfully");
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new SignupException("Failed to register. Please provide valid details");
        }
    }

    @Override
    public ResultModel forgotPassword(String email) throws UsernameNotFoundException, OTPGenerateException {
        if (!systemInterface.verifyEmail(email)) {
            systemInterface.forgetOtp(email);
            return new ResultModel("OTP sent successfully");
        }
        throw new UsernameNotFoundException("Invalid email");
    }

    @Override
    public boolean resetPassword(int otp, String email) {
        return systemInterface.verifyOtp(otp, email);
    }

    @Override
    public ResultModel changePassword(String newPassword, String confirmPassword) throws UpdateFailedException {
        if (newPassword.equals(confirmPassword)) {
            jdbcTemplate.update("update users set password = ? where userId = ? and isDeleted = 'false'",
                    passwordEncoder.encode(newPassword), systemInterface.getUserId());
            return new ResultModel("Password updated successfully");
        }
        throw new UpdateFailedException("Please re-check your password");
    }

    @Override
    public ResultModel resetPassword(String newPassword, String confirmPassword, String email) throws UpdateFailedException {
        if (newPassword.equals(confirmPassword)) {
            jdbcTemplate.update("update users set password = ? where email = ? and isDeleted = 'false'",
                    passwordEncoder.encode(newPassword), email);
            return new ResultModel("Password updated successfully");
        }
        throw new UpdateFailedException("Please re-check your password");
    }

    @Override
    public ResultModel changeProfilePhoto(String profilePhoto) throws UpdateFailedException {
        if (profilePhoto != null) {
            jdbcTemplate.update("update users set profilePicture = ? where userId = ? and isDeleted = 'false'",
                    profilePhoto, systemInterface.getUserId());
            return new ResultModel("Profile photo uploaded successfully");
        }
        throw new UpdateFailedException("Please select a Photo");
    }

    @Override
    public ResultModel deleteOldProfilePhoto() {
        Users user = systemInterface.getUserDetailByUserId();
        if (user.getProfilePicture() != null) {
            jdbcTemplate.update("update users set profilePicture = ? where userId = ? and isDeleted = 'false'",
                    null, systemInterface.getUserId());
            return new ResultModel("Photo has been deleted successfully");
        }
        throw new NullPointerException("Photo not uploaded");
    }

    /**
     * ******Tournament Interface******
     */

    @Override
    public Map<String, String> registerTournament(Tournaments tournaments) {
        Map<String, String> result = new HashMap<>();
        String tournamentCode = systemInterface.generateTournamentCode();
        if (!systemInterface.verifyTournamentCode(tournamentCode).isEmpty())
            registerTournament(tournaments);

        jdbcTemplate.update("insert into tournament values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null,
                systemInterface.getUserId(), tournaments.getTournamentName(), tournaments.getTournamentType(),
                tournamentCode, tournaments.getTournamentLogo(), null, null, null, null, 0, 0, 0, 0,
                TournamentStatus.UPCOMING.toString());

        result.put("tournamentName", tournaments.getTournamentName());
        result.put("tournamentCode", tournamentCode);
        result.put("message", "successfully created");
        return result;
    }

    @Override
    public ResultModel cancelTournament(long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");

        jdbcTemplate.update("update tournaments set tournamentStatus = ? where tournamentCode = ?",
                TournamentStatus.CANCELLED, tournamentId);
        systemInterface.deleteMatches(tournamentId);
        return new ResultModel("Tournament has been cancelled");
    }

    /**
     * ******Grounds Interface******
     */

    @Override
    public ResultModel registerGrounds(Grounds ground, List<String> groundPhoto) {
        if (!systemInterface.verifyLatitudeAndLongitude(ground.getLatitude(), ground.getLongitude(), ground.getTournamentId()).isEmpty())
            throw new NullPointerException("ground already added in given longitude and longitude");
        jdbcTemplate.update("insert into grounds values(?,?,?,?,?,?,?,?,?)", null, ground.getTournamentId(),
                ground.getGroundName(), ground.getCity(), ground.getGroundLocation(), ground.getGroundPhoto(),
                ground.getLatitude(), ground.getLongitude(), "false");
        jdbcTemplate.update("update tournaments set numberOfGrounds = numberOfGrounds + 1 where tournamentId = ?",
                ground.getTournamentId());
        return new ResultModel("Ground(s) added successfully");
    }

    @Override
    public ResultModel deleteGrounds(long groundId) {
        if (systemInterface.verifyGroundId(groundId).isEmpty())
            throw new NullPointerException("Ground not found");
        jdbcTemplate.update("update grounds set isDeleted = 'true' where groundId = ?", groundId);
        jdbcTemplate.update("update tournaments set numberOfGrounds = numberOfGrounds - 1 where tournamentId in" +
                "(select tournamentId from grounds where groundId = ?)", groundId);
        return new ResultModel("Ground has been deleted");
    }

    @Override
    public ResultModel editGround(Grounds ground) {
        if (systemInterface.verifyGroundId(ground.getGroundId()).isEmpty())
            return new ResultModel("Invalid ground");
        if (!systemInterface.verifyLatitudeAndLongitude(ground.getLatitude(), ground.getLongitude(), ground.getTournamentId()).isEmpty())
            return new ResultModel("latitude and longitude already added");
        jdbcTemplate.update("update grounds set groundName = ? , city = ? , groundLocation = ? , latitude = ? , longitude = ?," +
                        " GroundPhoto = ? where groundId = ?", ground.getGroundName(), ground.getCity(), ground.getGroundLocation(),
                ground.getLatitude(), ground.getLongitude(), ground.getGroundPhoto(), ground.getGroundId());
        return new ResultModel("Ground details updated successfully");
    }

    /**
     * ******Umpires Interface******
     */

    @Override
    public ResultModel registerUmpires(Umpires umpires) {
        if (systemInterface.verifyTournamentId(umpires.getTournamentId()).isEmpty())
            throw new NullPointerException("tournament not found");
        jdbcTemplate.update("insert into umpires values(?,?,?,?,?,?,?)", null, umpires.getTournamentId(),
                umpires.getUmpireName(), umpires.getCity(), umpires.getPhoneNumber(), umpires.getUmpirePhoto(), "false");
        jdbcTemplate.update("update tournaments set numberOfUmpires = numberOfUmpires + 1 where tournamentId = ?",
                umpires.getTournamentId());

        return new ResultModel("Umpire registered successfully");


    }

    @Override
    public ResultModel deleteUmpires(long umpireId, long tournamentId) {
        if (systemInterface.verifyUmpireDetails(tournamentId, umpireId).isEmpty())
            throw new NullPointerException("Umpire not found");
        jdbcTemplate.update("update umpires set isDeleted = 'true' where umpireId = ?", umpireId);
        jdbcTemplate.update("update tournaments set numberOfUmpires = numberOfUmpires - 1 where tournamentId = ?",
                tournamentId);
        return new ResultModel("Umpire has been removed");
    }

    @Override
    public ResultModel editUmpire(Umpires umpire) {
        if (systemInterface.verifyUmpireDetails(umpire.getTournamentId(), umpire.getUmpireId()).isEmpty())
            return new ResultModel("Invalid umpire details");
        jdbcTemplate.update("update umpires set umpireName = ? ,city = ? ,phoneNumber = ? , umpirePhoto = ? where umpireId = ? ",
                umpire.getUmpireName(), umpire.getCity(), umpire.getPhoneNumber(), umpire.getUmpirePhoto(), umpire.getUmpireId());
        return new ResultModel("changes have been updated");
    }

}
