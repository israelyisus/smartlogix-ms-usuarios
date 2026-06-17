package com.smartlogix.usuarios.factory;

import com.smartlogix.usuarios.dto.RegistroDTO;
import com.smartlogix.usuarios.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
public class UsuarioFactory {

    private final PasswordEncoder passwordEncoder;

    public UsuarioFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario crearUsuario(RegistroDTO dto) {
        return switch (dto.getTipo()) {
            case PYME -> crearUsuarioPyme(dto);
            case ADMIN -> crearUsuarioAdmin(dto);
        };
    }

    private Usuario crearUsuarioPyme(RegistroDTO dto) {
        validarCamposPyme(dto);

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombre(dto.getNombre());
        usuario.setTipo(Usuario.TipoUsuario.PYME);

        // Campos específicos de PYME
        usuario.setRut(dto.getRut());
        usuario.setEmpresa(dto.getEmpresa());
        usuario.setRegion(dto.getRegion());

        System.out.println("🏪 UsuarioPyme creado: " + dto.getEmail() + " | Empresa: " + dto.getEmpresa());
        return usuario;
    }

    private Usuario crearUsuarioAdmin(RegistroDTO dto) {
        validarCamposAdmin(dto);

        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setNombre(dto.getNombre());
        usuario.setTipo(Usuario.TipoUsuario.ADMIN);

        // Campos específicos de Admin
        usuario.setCodigoAdmin(dto.getCodigoAdmin());

        System.out.println("👤 UsuarioAdmin creado: " + dto.getEmail());
        return usuario;
    }

    private void validarCamposPyme(RegistroDTO dto) {
        if (dto.getRut() == null || dto.getRut().isBlank())
            throw new IllegalArgumentException("El RUT es obligatorio para usuarios PYME");
        if (dto.getEmpresa() == null || dto.getEmpresa().isBlank())
            throw new IllegalArgumentException("La empresa es obligatoria para usuarios PYME");
    }

    private void validarCamposAdmin(RegistroDTO dto) {
        if (dto.getCodigoAdmin() == null || dto.getCodigoAdmin().isBlank())
            throw new IllegalArgumentException("El código de admin es obligatorio");
    }
}
