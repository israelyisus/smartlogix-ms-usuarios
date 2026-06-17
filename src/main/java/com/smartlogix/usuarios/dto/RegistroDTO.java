package com.smartlogix.usuarios.dto;

import com.smartlogix.usuarios.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistroDTO {

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String nombre;

    @NotNull
    private Usuario.TipoUsuario tipo;

    // Campos PYME
    private String rut;
    private String empresa;
    private String region;

    // Campos Admin
    private String codigoAdmin;
}
