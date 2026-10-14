package com.pet.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
		String mensagem,
		LocalDateTime dataHora,
		int status,
		List<String> erros
) {

	public ErrorResponse(String mensagem, LocalDateTime dataHora, int status) {
		this(mensagem, dataHora, status, List.of());
	}

}
