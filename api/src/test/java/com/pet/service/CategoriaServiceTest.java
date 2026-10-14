package com.pet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.pet.dto.CategoriaRequestDto;
import com.pet.dto.CategoriaResponseDto;
import com.pet.dto.TarefaRequestDto;
import com.pet.exception.CategoriaComTarefasVinculadasException;
import com.pet.model.StatusTarefa;

@SpringBootTest
class CategoriaServiceTest {

	@Autowired
	private CategoriaService categoriaService;

	@Autowired
	private TarefaService tarefaService;

	@Test
	void naoDeveDeletarCategoriaComTarefasVinculadas() {
		CategoriaResponseDto categoria = categoriaService.criar(new CategoriaRequestDto("Estudos"));

		tarefaService.criar(new TarefaRequestDto(
				"Estudar JPA",
				"Praticar relacionamentos",
				StatusTarefa.INICIADO,
				LocalDate.now(),
				null,
				categoria.id()
		));

		assertThatThrownBy(() -> categoriaService.deletar(categoria.id()))
				.isInstanceOf(CategoriaComTarefasVinculadasException.class);
	}

	@Test
	void deveDeletarCategoriaSemTarefasVinculadas() {
		CategoriaResponseDto categoria = categoriaService.criar(new CategoriaRequestDto("Sem tarefas"));

		categoriaService.deletar(categoria.id());

		assertThat(categoriaService.listar())
				.noneMatch(c -> c.id().equals(categoria.id()));
	}

}
