package com.backhome.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.backhome.demo.model.EstadoPersona;
import com.backhome.demo.model.Persona;
import com.backhome.demo.repository.AdministradorRepository;
import com.backhome.demo.repository.ClienteRepository;
import com.backhome.demo.repository.PersonaRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AdministradorRepository administradorRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(
                personaRepository,
                clienteRepository,
                administradorRepository
        );
    }

    @Test
    void debeRechazarCorreoNulo() {

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername(null)
        );
    }

    @Test
    void debeRechazarCorreoVacio() {

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("   ")
        );
    }

    @Test
    void debeRechazarPersonaInexistente() {

        when(personaRepository.findByEmailIgnoreCase("correo@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("correo@gmail.com")
        );
    }

    @Test
    void debeRechazarEstadoNulo() {

        Persona persona = crearPersona();
        persona.setEstado(null);

        when(personaRepository.findByEmailIgnoreCase("correo@gmail.com"))
                .thenReturn(Optional.of(persona));

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("correo@gmail.com")
        );
    }

    @Test
    void debeRechazarCuentaBloqueada() {

        Persona persona = crearPersona();
        persona.setEstado(EstadoPersona.bloqueado);

        when(personaRepository.findByEmailIgnoreCase("correo@gmail.com"))
                .thenReturn(Optional.of(persona));

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("correo@gmail.com")
        );
    }

    @Test
    void debeRechazarCuentaSuspendida() {

        Persona persona = crearPersona();
        persona.setEstado(EstadoPersona.suspendido);

        when(personaRepository.findByEmailIgnoreCase("correo@gmail.com"))
                .thenReturn(Optional.of(persona));

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("correo@gmail.com")
        );
    }

    @Test
    void debeRechazarCuentaSinContrasena() {

        Persona persona = crearPersona();
        persona.setEstado(EstadoPersona.activo);
        persona.setPassword("   ");

        when(personaRepository.findByEmailIgnoreCase("correo@gmail.com"))
                .thenReturn(Optional.of(persona));

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("correo@gmail.com")
        );
    }

    @Test
    void debeRechazarPersonaSinPerfil() {

        Persona persona = crearPersona();
        persona.setEstado(EstadoPersona.activo);

        when(personaRepository.findByEmailIgnoreCase("correo@gmail.com"))
                .thenReturn(Optional.of(persona));

        when(administradorRepository.existsByPersona_IdPersona(1))
                .thenReturn(false);

        when(clienteRepository.existsByPersona_IdPersona(1))
                .thenReturn(false);

        assertThrows(
                UsernameNotFoundException.class,
                () -> service.loadUserByUsername("correo@gmail.com")
        );
    }

    @Test
    void debeCrearUsuarioCliente() {

        Persona persona = crearPersona();
        persona.setEstado(EstadoPersona.activo);

        when(personaRepository.findByEmailIgnoreCase("correo@gmail.com"))
                .thenReturn(Optional.of(persona));

        when(administradorRepository.existsByPersona_IdPersona(1))
                .thenReturn(false);

        when(clienteRepository.existsByPersona_IdPersona(1))
                .thenReturn(true);

        UserDetails resultado =
                service.loadUserByUsername("correo@gmail.com");

        assertEquals("correo@gmail.com", resultado.getUsername());
        assertEquals("password123", resultado.getPassword());
        assertTrue(
                resultado.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"))
        );
    }

    @Test
    void debeCrearUsuarioAdministrador() {

        Persona persona = crearPersona();
        persona.setEstado(EstadoPersona.activo);

        when(personaRepository.findByEmailIgnoreCase("correo@gmail.com"))
                .thenReturn(Optional.of(persona));

        when(administradorRepository.existsByPersona_IdPersona(1))
                .thenReturn(true);

        when(clienteRepository.existsByPersona_IdPersona(1))
                .thenReturn(false);

        UserDetails resultado =
                service.loadUserByUsername("correo@gmail.com");

        assertEquals("correo@gmail.com", resultado.getUsername());
        assertEquals("password123", resultado.getPassword());
        assertTrue(
                resultado.getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
        );
    }

    private Persona crearPersona() {

        Persona persona = new Persona();

        persona.setIdPersona(1);
        persona.setEmail("correo@gmail.com");
        persona.setPassword("password123");

        return persona;
    }
}