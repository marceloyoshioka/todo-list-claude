package com.pet.dto;

public record UsuarioResponseDto(
		Long id,
		String nome,
		String email,
		int quantidadeTarefas
) {
}
