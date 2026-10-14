package com.pet.dto;

import java.time.LocalDate;

import com.pet.model.StatusTarefa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TarefaRequestDto(
		@NotBlank(message = "O título da tarefa é obrigatório") String titulo,
		String descricao,
		@NotNull(message = "O status da tarefa é obrigatório") StatusTarefa status,
		@NotNull(message = "A data de início é obrigatória") LocalDate dataInicio,
		LocalDate dataConclusao,
		@NotNull(message = "A categoria é obrigatória") Long categoriaId
) {
}
