package com.nutritrack.service;
import com.nutritrack.entity.User; import com.nutritrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Collections;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired private UserRepository userRepo;
    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u=userRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found: "+email));
        return new org.springframework.security.core.userdetails.User(
            u.getEmail(),u.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_"+u.getRole().name())));
    }
}
