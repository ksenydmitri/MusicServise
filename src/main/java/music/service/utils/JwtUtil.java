package music.service.utils;

import io.jsonwebtoken.*;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import music.service.config.CacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private static String secretKey;
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;
    private static final String SECRET_KEY = Optional.ofNullable(secretKey)
            .orElse("default-strong-secret-key-32-chars-min");

    @Autowired
    private UserDetailsService userDetailsService;

    public String generateToken(String username) {
        if (SECRET_KEY == null || SECRET_KEY.isEmpty()) {
            throw new IllegalStateException("Secret key for JWT signing is not configured");
        }

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public UserDetails loadUserByUsername(String username) {
        return userDetailsService.loadUserByUsername(username);
    }

    public boolean isTokenValid(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        try {
            logger.info("Extracting claims from token...");
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.error("Token has expired");
            throw new ExpiredJwtException(null, null, "Token expired");
        } catch (MalformedJwtException e) {
            logger.error("Malformed token");
            throw new RuntimeException("Malformed token", e);
        } catch (SignatureException e) {
            logger.error("Invalid signature");
            throw new RuntimeException("Invalid token signature", e);
        } catch (Exception e) {
            logger.error("Failed to parse token");
            throw new RuntimeException("Failed to parse token", e);
        }
    }



}
