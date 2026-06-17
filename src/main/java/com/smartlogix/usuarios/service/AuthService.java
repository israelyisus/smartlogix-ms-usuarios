package com.smartlogix.usuarios.service;

import com.smartlogix.usuarios.dto.LoginDTO;
import com.smartlogix.usuarios.dto.RegistroDTO;
import com.smartlogix.usuarios.factory.UsuarioFactory;
import com.smartlogix.usuarios.model.Usuario;
import com.smartlogix.usuarios.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioFactory usuarioFactory;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthService(UsuarioRepository usuarioRepository,
                       UsuarioFactory usuarioFactory,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioFactory = usuarioFactory;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrar(RegistroDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con: " + dto.getEmail());
        }

        // Factory Method: crea PYME o ADMIN
        Usuario usuario = usuarioFactory.crearUsuario(dto);
        return usuarioRepository.save(usuario);
    }

 // ── Método de login: valida credenciales y genera JWT 
    public Map<String, String> login(LoginDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        if (!usuario.isActivo()) {
            throw new RuntimeException("Cuenta desactivada");
        }

        String token = generarToken(usuario);

        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("token", token);
        respuesta.put("tipo", usuario.getTipo().name());
        respuesta.put("nombre", usuario.getNombre());
        return respuesta;
    }

    private String generarToken(Usuario usuario) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
            .setSubject(usuario.getEmail())
            .claim("tipo", usuario.getTipo().name())
            .claim("nombre", usuario.getNombre())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key)
            .compact();
    }
}
