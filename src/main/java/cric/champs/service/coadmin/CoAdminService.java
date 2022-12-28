package cric.champs.service.coadmin;

import cric.champs.entity.Matches;
import cric.champs.entity.Users;
import cric.champs.model.CoAdmin;
import cric.champs.requestmodel.CoAdminRequestModel;
import cric.champs.resultmodels.CoAdminResult;
import cric.champs.resultmodels.SuccessResultModel;
import cric.champs.service.AccountStatus;
import cric.champs.service.MatchStatus;
import cric.champs.service.system.SystemInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CoAdminService implements CoAdminInterface {

    @Autowired
    private SystemInterface systemInterface;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<CoAdminResult> viewCoAdmin(long tournamentId) {
        List<CoAdminResult> coAdminResults = new ArrayList<>();
        if (systemInterface.verifyTournamentsIdWithOutUserVerification(tournamentId).isEmpty())
            throw new NullPointerException("Invalid tournament");
        List<CoAdmin> coAdmins = jdbcTemplate.query("select * from coAdmin where tournamentId = ? group by userId",
                new BeanPropertyRowMapper<>(CoAdmin.class), tournamentId);
        for (CoAdmin coAdmin : coAdmins) {
            List<CoAdmin> coAdminList = jdbcTemplate.query("select * from coAdmin where tournamentId = ? and userId = ?",
                    new BeanPropertyRowMapper<>(CoAdmin.class), tournamentId, coAdmin.getUserId());
            List<Integer> matchNumber = new ArrayList<>();
            for (CoAdmin admin : coAdminList)
                matchNumber.add(admin.getMatchNumber());
            coAdminResults.add(new CoAdminResult(getUser(coAdmin.getUserId()).getUsername(),
                    getUser(coAdmin.getUserId()).getProfilePicture(), matchNumber));
        }
        return coAdminResults;
    }

    @Override
    public SuccessResultModel assignCoAdminToMatch(CoAdminRequestModel coAdminRequestModel) {
        List<Users> user = systemInterface.getUserDetails(coAdminRequestModel.getCoAdminEmail(),
                AccountStatus.VERIFIED.toString());
        if (systemInterface.verifyUserID().isEmpty() || user.isEmpty())
            throw new NullPointerException("tournament not created or Invalid co-admin email");
        List<Matches> matches = jdbcTemplate.query("select * from matches where matchId = ? and matchStatus = ?",
                new BeanPropertyRowMapper<>(Matches.class), coAdminRequestModel.getMatchId(), MatchStatus.UPCOMING.toString());
        if (matches.isEmpty())
            throw new NullPointerException("no match found to assign co admin");
        jdbcTemplate.update("insert into coAdmin values(?,?,?,?,?,?)", user.get(0).getUserId(),
                matches.get(0).getTournamentId(), matches.get(0).getMatchId(), matches.get(0).getMatchNumber(),
                matches.get(0).getMatchStatus(), "false");
        return new SuccessResultModel("Co-admin added successfully");
    }

    private Users getUser(Long userId) {
        return jdbcTemplate.query("select * from users where userId = ? and isDeleted = 'false'",
                new BeanPropertyRowMapper<>(Users.class), userId).get(0);
    }


}
