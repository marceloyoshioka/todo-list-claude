package com.pet.dto;

import java.time.LocalDate;
import java.util.List;

import com.pet.model.StatusTarefa;

public record TarefaResponseDto(
		Long id,
		String titulo,
		String descricao,
		StatusTarefa status,
		LocalDate dataInicio,
		LocalDate dataConclusao,
		Long categoriaId,
		String categoriaNome,
		List<UsuarioResumoDto> usuarios
) {
}
