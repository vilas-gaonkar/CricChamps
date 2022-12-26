package cric.champs.security.userdetails;

import cric.champs.entity.Users;
import cric.champs.service.AccountStatus;
import cric.champs.service.system.SystemInterface;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JWTUserDetailsService implements UserDetailsService {

    @Autowired
    private SystemInterface systemInterface;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        List<Users> user = systemInterface.getUserDetails(email, AccountStatus.VERIFIED.toString());
        if (user.isEmpty())
            throw new UsernameNotFoundException("Account not found");
        return new User(email, user.get(0).getPassword(), new ArrayList<>());
    }

    public void checkTokenExistInBlocklist(String token) {
        if(!systemInterface.verifyTokenValidity(token))
            throw new JwtException("JWT_TOKEN_EXPIRED");
    }
}
