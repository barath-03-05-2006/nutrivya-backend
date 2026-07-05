package com.nutritrack.service;
import com.nutritrack.entity.User; import com.nutritrack.repository.UserRepository;
import com.nutritrack.exception.AccountLockedException;
import com.nutritrack.exception.InvalidCredentialsException;
import com.nutritrack.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*; import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
@Service
public class AuthService {
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder encoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authManager;
    @Autowired private CustomUserDetailsService uds;

    // After this many wrong passwords in a row, the account gets locked out.
    private static final int MAX_ATTEMPTS = 3;
    // First lockout is 1 minute; each subsequent lockout for the same account
    // doubles (1, 2, 4, 8... minutes) up to the cap below, to slow down repeated
    // brute-force attempts instead of letting them retry every minute forever.
    private static final long BASE_LOCKOUT_SECONDS = 60;
    private static final long MAX_LOCKOUT_SECONDS = 30 * 60;

    public Map<String,Object> login(String email, String password){
        User u = userRepo.findByEmail(email).orElse(null);

        if (u != null && u.getLockedUntil() != null) {
            if (u.getLockedUntil().isAfter(LocalDateTime.now())) {
                long secondsLeft = Duration.between(LocalDateTime.now(), u.getLockedUntil()).getSeconds();
                throw new AccountLockedException(Math.max(secondsLeft, 1));
            }
            // Lock has expired naturally — clear it and give them a fresh set of attempts.
            u.setLockedUntil(null);
            u.setFailedLoginAttempts(0);
            userRepo.save(u);
        }

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (AuthenticationException ex) {
            if (u != null) {
                int attempts = u.getFailedLoginAttempts() + 1;
                if (attempts >= MAX_ATTEMPTS) {
                    int streak = u.getLockoutStreak() + 1;
                    long lockoutSeconds = Math.min(BASE_LOCKOUT_SECONDS * (1L << Math.min(streak - 1, 10)), MAX_LOCKOUT_SECONDS);
                    u.setLockoutStreak(streak);
                    u.setLockedUntil(LocalDateTime.now().plusSeconds(lockoutSeconds));
                    u.setFailedLoginAttempts(0);
                    userRepo.save(u);
                    throw new AccountLockedException(lockoutSeconds);
                }
                u.setFailedLoginAttempts(attempts);
                userRepo.save(u);
                throw new InvalidCredentialsException(MAX_ATTEMPTS - attempts);
            }
            // Unknown email — don't reveal that, just a generic invalid-credentials error.
            throw new InvalidCredentialsException(-1);
        }

        // Successful login — clear any prior failed-attempt / lockout history.
        if (u.getFailedLoginAttempts() != 0 || u.getLockoutStreak() != 0 || u.getLockedUntil() != null) {
            u.setFailedLoginAttempts(0);
            u.setLockoutStreak(0);
            u.setLockedUntil(null);
            userRepo.save(u);
        }

        String token=jwtUtil.generateToken(uds.loadUserByUsername(email));
        return Map.of("token",token,"email",u.getEmail(),"role",u.getRole().name(),
            "fullName",u.getFullName()!=null?u.getFullName():"","userId",u.getId());
    }

    public Map<String,Object> register(String username,String email,String password,String fullName,String phone){
        if(userRepo.existsByEmail(email)) throw new RuntimeException("Email already registered");
        // Server-side password strength validation — cannot be bypassed from the client
        if(password == null || password.length() < 6)
            throw new RuntimeException("Password must be at least 6 characters");
        if(!password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*"))
            throw new RuntimeException("Password must contain at least one letter and one number");
        if(userRepo.existsByUsername(username)) throw new RuntimeException("Username already taken");
        User u=new User(); u.setUsername(username); u.setEmail(email);
        u.setPassword(encoder.encode(password)); u.setFullName(fullName);
        u.setPhoneNumber(phone); u.setRole(User.Role.CLIENT);
        userRepo.save(u);
        String token=jwtUtil.generateToken(uds.loadUserByUsername(email));
        return Map.of("token",token,"email",u.getEmail(),"role",u.getRole().name(),
            "fullName",u.getFullName()!=null?u.getFullName():"","userId",u.getId());
    }
}
