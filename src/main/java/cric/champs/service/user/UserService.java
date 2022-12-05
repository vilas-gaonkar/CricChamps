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
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements LoginInterface, TournamentInterface, GroundInterface, UmpiresInterface, TeamInterface, PlayerInterface {

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
                return new ResultModel("This Email is already registered with Cric Champs");
            jdbcTemplate.update("insert into users values(?,?,?,?,?,?,?,?,?,?,?)", null, user.getUsername(), user.getGender(),
                    user.getEmail(), user.getPhoneNumber(), user.getCity(), user.getProfilePicture(), user.getAge(),
                    passwordEncoder.encode(user.getPassword()), AccountStatus.NOTVERIFIED.toString(), "false");
            return new ResultModel("Your Cric Champs account has been created successfully");
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new SignupException("Failed to register. Please provide valid details");
        }
    }

    @Override
    public ResultModel forgotPassword(String email) throws UsernameNotFoundException, OTPGenerateException {
        if (!systemInterface.verifyEmail(email)) {
            systemInterface.forgetOtp(email);
            return new ResultModel("OTP has been sent to your email");
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
            return new ResultModel("Your password has been changed successfully");
        }
        throw new UpdateFailedException("Please re-check your password");
    }

    @Override
    public ResultModel resetPassword(String newPassword, String confirmPassword, String email) throws UpdateFailedException {
        if (newPassword.equals(confirmPassword)) {
            jdbcTemplate.update("update users set password = ? where email = ? and isDeleted = 'false'",
                    passwordEncoder.encode(newPassword), email);
            return new ResultModel("Your password has been reset successfully");
        }
        throw new UpdateFailedException("Please re-check your password");
    }

    @Override
    public ResultModel changeProfilePhoto(String profilePhoto) throws UpdateFailedException {
        if (profilePhoto != null) {
            jdbcTemplate.update("update users set profilePicture = ? where userId = ? and isDeleted = 'false'",
                    profilePhoto, systemInterface.getUserId());
            return new ResultModel("Your profile photo has been changed successfully");
        }
        throw new UpdateFailedException("Please select a photo");
    }

    @Override
    public ResultModel deleteOldProfilePhoto() {
        Users user = systemInterface.getUserDetailByUserId();
        if (user.getProfilePicture() != null) {
            jdbcTemplate.update("update users set profilePicture = ? where userId = ? and isDeleted = 'false'",
                    null, systemInterface.getUserId());
            return new ResultModel("Your Profile Photo has been deleted");
        }
        throw new NullPointerException("Please select a photo");
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
    public List<Tournaments> getTournamentDetails(int pageSize, int pageNumber) {
        int offSet = pageSize * (pageNumber - 1);
        return jdbcTemplate.query("select * from tournaments where userId = ? limit ? offset ?",
                new BeanPropertyRowMapper<>(Tournaments.class), systemInterface.getUserId(), pageSize, offSet);
    }

    @Override
    public Tournaments getTournament(long tournamentId) {
        List<Tournaments> tournament = systemInterface.verifyTournamentId(tournamentId);
        if (tournament.isEmpty())
            throw new NullPointerException("tournament not found");
        return tournament.get(0);
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

    @Override
    public ResultModel setTournamentDate(long tournamentId, LocalDate startDate, LocalDate endDate) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        jdbcTemplate.update("update tournaments set tournamentStartDate = ? , tournamentEndDate = ? where " +
                "tournamentId = ? and isDeleted = 'false'",startDate,endDate,tournamentId);
        return new ResultModel("date added successfully");
    }

    @Override
    public ResultModel setTournamentTime(long tournamentId, LocalTime startTime, LocalTime endTime) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        jdbcTemplate.update("update tournaments set tournamentStartTime = ? , tournamentEndTime = ? where " +
                "tournamentId = ? and isDeleted = 'false'",startTime,endTime,tournamentId);
        return new ResultModel("Time added successfully");
    }

    /**
     * ******Grounds Interface******
     */

    @Override
    public ResultModel registerGrounds(Grounds ground, List<String> groundPhoto) {
        if (!systemInterface.verifyLatitudeAndLongitude(ground.getLatitude(), ground.getLongitude(), ground.getTournamentId()).isEmpty())
            throw new NullPointerException("A ground already exists in the given co-ordinates. Please enter different co-ordinates");
        jdbcTemplate.update("insert into grounds values(?,?,?,?,?,?,?,?,?)", null, ground.getTournamentId(),
                ground.getGroundName(), ground.getCity(), ground.getGroundLocation(), ground.getGroundPhoto(),
                ground.getLatitude(), ground.getLongitude(), "false");
        jdbcTemplate.update("update tournaments set numberOfGrounds = numberOfGrounds + 1 where tournamentId = ?",
                ground.getTournamentId());
        return new ResultModel("Ground(s) added successfully");
    }

    @Override
    public ResultModel deleteGrounds(long groundId, long tournamentId) {
        if (systemInterface.verifyGroundId(groundId, tournamentId).isEmpty())
            throw new NullPointerException("Ground not found");
        jdbcTemplate.update("update grounds set isDeleted = 'true' where groundId = ?", groundId);
        jdbcTemplate.update("update tournaments set numberOfGrounds = numberOfGrounds - 1 where tournamentId in" +
                "(select tournamentId from grounds where groundId = ?)", groundId);
        return new ResultModel("Ground has been deleted");
    }

    @Override
    public ResultModel editGround(Grounds ground) {
        if (systemInterface.verifyGroundId(ground.getGroundId(), ground.getTournamentId()).isEmpty())
            return new ResultModel("Invalid ground");
        if (!systemInterface.verifyLatitudeAndLongitude(ground.getLatitude(), ground.getLongitude(), ground.getTournamentId()).isEmpty())
            return new ResultModel("latitude and longitude already added");
        jdbcTemplate.update("update grounds set groundName = ? , city = ? , groundLocation = ? , latitude = ? , longitude = ?," +
                        " GroundPhoto = ? where groundId = ?", ground.getGroundName(), ground.getCity(), ground.getGroundLocation(),
                ground.getLatitude(), ground.getLongitude(), ground.getGroundPhoto(), ground.getGroundId());
        return new ResultModel("Ground details updated successfully");
    }

    @Override
    public List<Grounds> getAllGrounds(long tournamentId, int pageSize, int pageNumber) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament details");
        return jdbcTemplate.query("select * from grounds where tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Grounds.class), tournamentId);
    }

    @Override
    public Grounds getGround(long groundId, long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament details");
        return jdbcTemplate.query("select * from grounds where tournamentId = ? and groundId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Grounds.class), tournamentId, groundId).get(0);
    }

    /**
     * ******Umpires Interface******
     */

    @Override
    public ResultModel registerUmpires(Umpires umpires) {
        if (systemInterface.verifyTournamentId(umpires.getTournamentId()).isEmpty())
            throw new NullPointerException("Tournament not found");
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
        return new ResultModel("Umpire details have been updated successfully");
    }

    @Override
    public List<Umpires> getUmpireDetails(long tournamentId, int pageSize, int pageNumber) {
        int offset = pageSize * (pageNumber - 1);
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        return jdbcTemplate.query("select * from umpires where tournamentId = ? and isDeleted = 'false' limit ? offset ?",
                new BeanPropertyRowMapper<>(Umpires.class), tournamentId, pageSize, offset);
    }

    @Override
    public Umpires getUmpire(long umpireId, long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        return jdbcTemplate.query("select * from umpires where tournamentId = ? and umpireId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Umpires.class), tournamentId, umpireId).get(0);
    }

    /**
     * ******Team Interface******
     */

    @Override
    public ResultModel registerTeam(Teams teams) {
        jdbcTemplate.update("insert into teams values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null, teams.getTournamentId(),
                teams.getTeamName(), null, teams.getCity(), 0, 0, 0, 0, 0, 0, 0, teams.getTeamLogo(), "false");
        return new ResultModel("Team registered successfully.");
    }

    @Override
    public ResultModel deleteTeam(long teamId, long tournamentId) {
        if (systemInterface.verifyTeamDetails(teamId, tournamentId).isEmpty())
            return new ResultModel("Team is not found");
        jdbcTemplate.update("update teams set isDeleted = 'true' where teamId = ? and tournamentId = ?", teamId, tournamentId);
        return new ResultModel("Team deleted successfully.");

    }

    @Override
    public ResultModel editTeam(Teams teams) {
        if (systemInterface.verifyTeamDetails(teams.getTeamId(), teams.getTournamentId()).isEmpty())
            return new ResultModel("edited failed");
        jdbcTemplate.update("update teams set teamName = ?, city = ?, teamLogo = ? where teamId = ? and tournamentId = ?",
                teams.getTeamName(), teams.getCity(), teams.getTeamLogo(), teams.getTeamId(), teams.getTournamentId());

        return new ResultModel("edited successfully");

    }

    @Override
    public List<Teams> getAllTeams(long tournamentId, int pageSize, int pageNumber) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        return jdbcTemplate.query("select * from teams where tournamentId = ? and isDeleted='false' limit ? offset ?",
                new BeanPropertyRowMapper<>(Teams.class), tournamentId, pageSize, pageNumber);
    }

    @Override
    public Teams getTeam(long teamId, long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        if (systemInterface.verifyTeamDetails(teamId, tournamentId).isEmpty())
            throw new NullPointerException("Team not found.");
        return jdbcTemplate.query("select * from teams where teamId = ? and tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Teams.class), teamId, tournamentId).get(0);
    }

    /**
     * ******Player Interface******
     */

    @Override
    public ResultModel registerPlayer(Players players) {
        jdbcTemplate.update("insert into players values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", null, players.getTournamentId(),
                players.getTeamId(), players.getPlayerName(), players.getCity(), players.getPhoneNumber(), players.getProfilePhoto(),
                players.getDesignation(), players.getExpertise(), players.getBattingStyle(), players.getBowlingStyle(),
                players.getBowlingType(), 0, 0, 0, null, players.getPersonalId(), players.getPersonalIdName(), "false");
        return new ResultModel("Player registered successfully.");
    }

    @Override
    public ResultModel deletePlayer(long playerId, long teamId, long tournamentId) {
        if (systemInterface.verifyPlayerDetails(playerId, teamId, tournamentId).isEmpty())
            return new ResultModel("Player not found");
        jdbcTemplate.update("update players set isDeleted = 'true' where playerId = ? and teamId = ? and tournamentId = ?",
                playerId, teamId, tournamentId);
        return new ResultModel("Player deleted successfully");

    }

    @Override
    public ResultModel editPlayer(Players players) {
        if (systemInterface.verifyPlayerDetails(players.getPlayerId(), players.getTeamId(), players.getTournamentId()).isEmpty())
            return new ResultModel("Player is not found.");
        jdbcTemplate.update("update players set playerName = ?, city = ?, phoneNumber = ?, profilePhoto = ?," +
                        "designation = ?, expertise = ?, battingStyle = ?, bowlingStyle = ?, bowlingType = ? where playerId = ? " +
                        "and teamId = ? and tournamentId = ?", players.getPlayerName(), players.getCity(), players.getPhoneNumber(),
                players.getProfilePhoto(), players.getDesignation(), players.getExpertise(), players.getBattingStyle(),
                players.getBowlingStyle(), players.getBowlingType(), players.getPlayerId(), players.getTeamId(), players.getTournamentId());

        return new ResultModel("Player edited successfully");

    }

    @Override
    public List<Players> getAllPlayers(long teamId, long tournamentId, int pageSize, int pageNumber) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        List<Players> players = systemInterface.verifyTeamAndTournamentId(teamId, tournamentId);
        if (players.isEmpty())
            throw new NullPointerException("Players not found");
        return jdbcTemplate.query("select * from players where teamId = ? and tournamentId = ? and isDeleted = 'false' limit ? offset ?",
                new BeanPropertyRowMapper<>(Players.class), teamId, tournamentId, pageSize, pageNumber);
    }

    @Override
    public Players getPlayer(long playerId, long teamId, long tournamentId) {
        if (systemInterface.verifyTournamentId(tournamentId).isEmpty())
            throw new NullPointerException("Tournament not found");
        if (systemInterface.verifyTeamDetails(teamId, tournamentId).isEmpty())
            throw new NullPointerException("Team not found");
        if (systemInterface.verifyPlayerDetails(playerId, teamId, tournamentId).isEmpty())
            throw new NullPointerException("Player not found");
        return jdbcTemplate.query("select * from players where playerId = ? and teamId = ? and tournamentId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Players.class), playerId, teamId, tournamentId).get(0);
    }

}
