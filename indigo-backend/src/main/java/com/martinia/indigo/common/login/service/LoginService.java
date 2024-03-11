package com.martinia.indigo.common.login.service;

import com.martinia.indigo.user.domain.ports.usecases.FindUserByUsernameUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LoginService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private FindUserByUsernameUseCase findUserByUsernameUseCase;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<com.martinia.indigo.user.domain.model.User> user = findUserByUsernameUseCase.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(user.get().getRole()));
        return new org.springframework.security.core.userdetails.User(user.get().getUsername(), user.get().getPassword(),
                authorities);
    }

}
