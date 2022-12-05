package cric.champs.service.system;

import cric.champs.entity.Tokens;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class TokenService implements TokenInterface{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean verifyToken(String token) {
        return jdbcTemplate.query("Select * from tokens where secureToken = ?", new BeanPropertyRowMapper<>(Tokens.class), token).isEmpty();
    }

    @Override
    public String generate() {
        Random random = new Random();
        final String Characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final char[] symbols = Characters.toCharArray();
        char[] secureToken = new char[30];
        for (int index = 0; index < 30; index++) {
            secureToken[index] = symbols[random.nextInt(symbols.length)];
        }
        return new String(secureToken);
    }

    @Override
    public void remove(String secureToken, String username) {
        jdbcTemplate.update("Delete from tokens where secureToken = ? and userName = ?", secureToken, username);
    }

    @Override
    public void save(String token, String username, LocalDateTime createdAt, LocalDateTime expireAt, String tokenStatus) {
        jdbcTemplate.update("Insert into tokens values(?,?,?,?,?)", token, username, createdAt, expireAt, tokenStatus);
    }

    @Override
    public List<Tokens> getTokenDetail(String token) {
        return jdbcTemplate.query("select * from tokens where secureToken = ?",
                new BeanPropertyRowMapper<>(Tokens.class), token);
    }

}
