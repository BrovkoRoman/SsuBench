package com.brovko.SsuBench.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.brovko.SsuBench.entity.User;
import com.brovko.SsuBench.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.brovko.SsuBench.entity.User.Role.ADMIN;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String SECRET;
    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(SECRET);
        this.verifier = JWT.require(algorithm).build();
    }

    @Autowired
    private UserService userService;

    public String create(User user) {
        Date expiresAt = new Date(System.currentTimeMillis() + 3600000);
        try {
            return JWT.create()
                    .withClaim("userId", user.getId())
                    .withExpiresAt(expiresAt)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new BusinessException("Can't create JWT.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public User find(String jwt) {
        try {
            DecodedJWT decodedJwt = verifier.verify(jwt);
            return userService.findById(decodedJwt.getClaim("userId").asLong());
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    public void checkAdminRights(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            User user = find(jwt);

            if (user == null || user.getRole() != ADMIN) {
                throw new BusinessException("You should have admin rights to do this operation",
                        HttpServletResponse.SC_FORBIDDEN);
            }

            return;
        }

        throw new BusinessException("You should have admin rights to do this operation",
                HttpServletResponse.SC_FORBIDDEN);
    }
}
