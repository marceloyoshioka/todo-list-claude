package com.pet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UsuarioRequestDto(
		@NotBlank(message = "O nome do usuário é obrigatório") String nome,
		@NotBlank(message = "O email do usuário é obrigatório")
		@Email(message = "O email informado é inválido") String email
) {
}
