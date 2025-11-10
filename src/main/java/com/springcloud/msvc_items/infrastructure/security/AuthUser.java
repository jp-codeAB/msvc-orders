package com.springcloud.msvc_items.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class AuthUser implements UserDetails {

    @Getter
    private final Long id;
    @Getter
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthUser(String idHeader, String emailHeader, String rolesHeader) {
        this.id = Long.parseLong(idHeader);
        this.email = emailHeader;
        this.authorities = Arrays.stream(rolesHeader.split(","))
                .map(role -> new SimpleGrantedAuthority(
                        "ROLE_" + role.trim().toUpperCase()
                ))
                .filter(g -> g.getAuthority().equals("ROLE_ADMIN") || g.getAuthority().equals("ROLE_CLIENT")) // Asegura que solo se usan roles conocidos
                .collect(Collectors.toList());
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}