package com.example.tfg.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import com.example.tfg.model.User;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private String email;
    private String password;
    private boolean enabled;

    // Constructor que toma los datos del usuario
    public CustomUserDetails(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.enabled = user.isVerified(); // El estado de habilitación se asigna según isVerified
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Si tienes roles, puedes devolverlos aquí
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return password;
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
        return enabled; // Devuelve el estado habilitado según el campo 'isVerified'
    }
}
