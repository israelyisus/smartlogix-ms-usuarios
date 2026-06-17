package com.smartlogix.usuarios;

import com.smartlogix.usuarios.dto.LoginDTO;
import com.smartlogix.usuarios.dto.RegistroDTO;
import com.smartlogix.usuarios.factory.UsuarioFactory;
import com.smartlogix.usuarios.model.Usuario;
import com.smartlogix.usuarios.repository.UsuarioRepository;
import com.smartlogix.usuarios.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AuthService
 * Patrón AAA: Arrange - Act - Assert
 * Usa Mockito para simular dependencias sin necesitar BD real
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - AuthService")
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioFactory usuarioFactory;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Inyectar valores de configuración JWT manualmente
        ReflectionTestUtils.setField(authService, "jwtSecret",
            "smartlogix-secret-key-2024-muy-segura-debe-ser-larga");
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);
    }

    // ─── PRUEBA 1: Registro exitoso ───────────────────────────────────────────

    @Test
    @DisplayName("Registro exitoso de usuario PYME con datos válidos")
    void testRegistroExitosoUsuarioPyme() {
        // ARRANGE: preparar datos de entrada y simular comportamiento
        RegistroDTO dto = new RegistroDTO();
        dto.setEmail("nueva@empresa.cl");
        dto.setPassword("123456");
        dto.setNombre("Jesus Guzman");
        dto.setTipo(Usuario.TipoUsuario.PYME);
        dto.setRut("76.123.456-7");
        dto.setEmpresa("Mi Empresa Ltda.");
        dto.setRegion("Metropolitana");

        Usuario usuarioCreado = new Usuario();
        usuarioCreado.setEmail("nueva@empresa.cl");
        usuarioCreado.setNombre("Jesus Guzman");
        usuarioCreado.setTipo(Usuario.TipoUsuario.PYME);
        usuarioCreado.setPassword("hash_seguro_123");

        when(usuarioRepository.existsByEmail("nueva@empresa.cl")).thenReturn(false);
        when(usuarioFactory.crearUsuario(dto)).thenReturn(usuarioCreado);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioCreado);

        // ACT: ejecutar el método a probar
        Usuario resultado = authService.registrar(dto);

        // ASSERT: verificar el resultado esperado
        assertNotNull(resultado);
        assertEquals("nueva@empresa.cl", resultado.getEmail());
        assertEquals(Usuario.TipoUsuario.PYME, resultado.getTipo());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    // ─── PRUEBA 2: Registro con email duplicado ───────────────────────────────

    @Test
    @DisplayName("Registro con email ya existente lanza IllegalArgumentException")
    void testRegistroEmailDuplicadoLanzaExcepcion() {
        // ARRANGE: simular que el email ya existe
        RegistroDTO dto = new RegistroDTO();
        dto.setEmail("empresa@pyme.cl");
        dto.setPassword("123456");
        dto.setNombre("Jesus Guzman");
        dto.setTipo(Usuario.TipoUsuario.PYME);

        when(usuarioRepository.existsByEmail("empresa@pyme.cl")).thenReturn(true);

        // ACT + ASSERT: verificar que lanza excepción
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> authService.registrar(dto)
        );

        assertTrue(ex.getMessage().contains("empresa@pyme.cl"));
        verify(usuarioRepository, never()).save(any());
    }

    // ─── PRUEBA 3: Login exitoso genera token ─────────────────────────────────

    @Test
    @DisplayName("Login con credenciales válidas retorna token JWT")
    void testLoginExitosoRetornaToken() {
        // ARRANGE: preparar usuario existente con password válida
        LoginDTO loginDto = new LoginDTO();
        loginDto.setEmail("empresa@pyme.cl");
        loginDto.setPassword("123456");

        Usuario usuario = new Usuario();
        usuario.setEmail("empresa@pyme.cl");
        usuario.setPassword("hash_seguro");
        usuario.setNombre("Jesus Guzman");
        usuario.setTipo(Usuario.TipoUsuario.PYME);
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail("empresa@pyme.cl"))
            .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", "hash_seguro")).thenReturn(true);

        // ACT: ejecutar login
        Map<String, String> resultado = authService.login(loginDto);

        // ASSERT: verificar que retorna token y datos del usuario
        assertNotNull(resultado);
        assertNotNull(resultado.get("token"));
        assertEquals("PYME", resultado.get("tipo"));
        assertEquals("Jesus Guzman", resultado.get("nombre"));
        assertTrue(resultado.get("token").startsWith("eyJ"));
    }

    // ─── PRUEBA 4: Login con credenciales inválidas ───────────────────────────

    @Test
    @DisplayName("Login con password incorrecta lanza RuntimeException")
    void testLoginPasswordIncorrectaLanzaExcepcion() {
        // ARRANGE: simular password incorrecta
        LoginDTO loginDto = new LoginDTO();
        loginDto.setEmail("empresa@pyme.cl");
        loginDto.setPassword("wrongpassword");

        Usuario usuario = new Usuario();
        usuario.setEmail("empresa@pyme.cl");
        usuario.setPassword("hash_seguro");
        usuario.setActivo(true);

        when(usuarioRepository.findByEmail("empresa@pyme.cl"))
            .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongpassword", "hash_seguro")).thenReturn(false);

        // ACT + ASSERT: verificar que lanza excepción
        assertThrows(RuntimeException.class, () -> authService.login(loginDto));
    }

    // ─── PRUEBA 5: Login con usuario no encontrado ────────────────────────────

    @Test
    @DisplayName("Login con email inexistente lanza RuntimeException")
    void testLoginEmailNoEncontradoLanzaExcepcion() {
        // ARRANGE: simular que el email no existe
        LoginDTO loginDto = new LoginDTO();
        loginDto.setEmail("noexiste@pyme.cl");
        loginDto.setPassword("123456");

        when(usuarioRepository.findByEmail("noexiste@pyme.cl"))
            .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThrows(RuntimeException.class, () -> authService.login(loginDto));
    }
}