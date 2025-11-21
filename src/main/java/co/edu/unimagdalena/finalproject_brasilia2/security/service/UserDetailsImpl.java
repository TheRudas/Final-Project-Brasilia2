package co.edu.unimagdalena.finalproject_brasilia2.security.service;

import co.edu.unimagdalena.finalproject_brasilia2.domain.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter; // 1. Importar Getter
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Getter // 2. Agregar la anotación aquí
public class UserDetailsImpl implements UserDetails {

    // Lombok generará automáticamente: public User getUser() { return user; }
    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
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
        return user.isStatus();
    }

    // 3. ¡Borramos el método getUser() manual! Ya no es necesario.
}