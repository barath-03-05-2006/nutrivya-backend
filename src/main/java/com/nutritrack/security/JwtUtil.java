package com.nutritrack.security;
import io.jsonwebtoken.*; import io.jsonwebtoken.io.Decoders; import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value; import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.*; import java.util.function.Function;
@Component
public class JwtUtil {
    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.expiration}") private long expiration;
    private SecretKey key(){return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));}
    public String generateToken(UserDetails u){
        return Jwts.builder().claims(new HashMap<>()).subject(u.getUsername())
            .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+expiration))
            .signWith(key()).compact();
    }
    public String extractUsername(String t){return extractClaim(t,Claims::getSubject);}
    public boolean validateToken(String t,UserDetails u){return extractUsername(t).equals(u.getUsername())&&!isExpired(t);}
    private boolean isExpired(String t){return extractClaim(t,Claims::getExpiration).before(new Date());}
    public <T> T extractClaim(String t,Function<Claims,T> f){
        return f.apply(Jwts.parser().verifyWith(key()).build().parseSignedClaims(t).getPayload());
    }
}
