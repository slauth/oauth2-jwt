package de.slauth.auth.server;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import de.slauth.auth.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired UserRepo userRepo;

    @Override
    public User loadUserByUsername(String name) throws UsernameNotFoundException {
        UserEntity result = userRepo.findOne(name);
        if (result == null) {
            throw new UsernameNotFoundException(String.format("User '%s' does not exist", name));
        }
        return convert(result);
    }

    public boolean exists(String name) {
        return userRepo.exists(name);
    }

    public User save(User user) {
        UserEntity entity = new UserEntity()
                .setUsername(user.getUsername())
                .setPassword(user.getPassword());
        return convert(userRepo.save(entity));
    }

    public void delete(String name) {
        userRepo.delete(name);
    }

    private User convert(UserEntity entity) {
        return new User.Builder()
                .withUsername(entity.getUsername())
                .withPassword(entity.getPassword())
                .withAuthorities(toAuthorities(entity.getScopes()))
                .build();
    }

    private Set<GrantedAuthority> toAuthorities(String scopes) {
        if (scopes == null) {
            return ImmutableSet.of();
        }
        Splitter splitter = Splitter.on(',').omitEmptyStrings().trimResults();
        return splitter.splitToList(scopes).stream()
                .map(scope -> new SimpleGrantedAuthority("ROLE_" + scope.toUpperCase()))
                .collect(Collectors.toSet());
    }
}
