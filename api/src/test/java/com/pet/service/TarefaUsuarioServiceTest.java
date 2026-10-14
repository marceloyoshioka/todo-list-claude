package com.pet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.pet.dto.CategoriaRequestDto;
import com.pet.dto.TarefaRequestDto;
import com.pet.dto.TarefaResponseDto;
import com.pet.dto.UsuarioRequestDto;
import com.pet.dto.UsuarioResponseDto;
import com.pet.dto.UsuarioResumoDto;
import com.pet.exception.TarefaNaoEncontradaException;
import com.pet.exception.UsuarioNaoEncontradoException;
import com.pet.model.StatusTarefa;

@SpringBootTest
class TarefaUsuarioServiceTest {

	@Autowired
	private TarefaService tarefaService;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private CategoriaService categoriaService;

	@Test
	void deveVincularVariosUsuariosEVariasTarefasNosDoisSentidos() {
		Long categoriaId = categoriaService.criar(new CategoriaRequestDto("Cat NM")).id();
		TarefaResponseDto t1 = criarTarefa("T1", categoriaId);
		TarefaResponseDto t2 = criarTarefa("T2", categoriaId);
		UsuarioResponseDto u1 = usuarioService.criar(new UsuarioRequestDto("U1", "u1.nm@teste.com"));
		UsuarioResponseDto u2 = usuarioService.criar(new UsuarioRequestDto("U2", "u2.nm@teste.com"));

		tarefaService.adicionarUsuario(t1.id(), u1.id());
		tarefaService.adicionarUsuario(t1.id(), u2.id());
		tarefaService.adicionarUsuario(t2.id(), u1.id());

		assertThat(tarefaService.buscarPorId(t1.id()).usuarios())
				.extracting(UsuarioResumoDto::id).containsExactly(u1.id(), u2.id());
		assertThat(tarefaService.listarPorUsuario(u1.id()))
				.extracting(TarefaResponseDto::id).containsExactlyInAnyOrder(t1.id(), t2.id());
		assertThat(tarefaService.listarPorUsuario(u2.id()))
				.extracting(TarefaResponseDto::id).containsExactly(t1.id());
		assertThat(usuarioService.buscarPorId(u1.id()).quantidadeTarefas()).isEqualTo(2);
	}

	@Test
	void vincularEDesvincularDevemSerIdempotentes() {
		Long categoriaId = categoriaService.criar(new CategoriaRequestDto("Cat Idem")).id();
		TarefaResponseDto tarefa = criarTarefa("Idem", categoriaId);
		UsuarioResponseDto usuario = usuarioService.criar(new UsuarioRequestDto("Idem", "idem.nm@teste.com"));

		tarefaService.adicionarUsuario(tarefa.id(), usuario.id());
		TarefaResponseDto aposSegundoVinculo = tarefaService.adicionarUsuario(tarefa.id(), usuario.id());
		assertThat(aposSegundoVinculo.usuarios()).hasSize(1);

		tarefaService.removerUsuario(tarefa.id(), usuario.id());
		tarefaService.removerUsuario(tarefa.id(), usuario.id());
		assertThat(tarefaService.buscarPorId(tarefa.id()).usuarios()).isEmpty();
	}

	@Test
	void deletarTarefaNaoDeveApagarUsuario() {
		Long categoriaId = categoriaService.criar(new CategoriaRequestDto("Cat Del")).id();
		TarefaResponseDto tarefa = criarTarefa("Del", categoriaId);
		UsuarioResponseDto usuario = usuarioService.criar(new UsuarioRequestDto("Del", "del.nm@teste.com"));
		tarefaService.adicionarUsuario(tarefa.id(), usuario.id());

		tarefaService.deletar(tarefa.id());

		assertThat(usuarioService.buscarPorId(usuario.id()).quantidadeTarefas()).isZero();
	}

	@Test
	void deveFalharAoVincularComIdsInexistentes() {
		Long categoriaId = categoriaService.criar(new CategoriaRequestDto("Cat Inexistente")).id();
		TarefaResponseDto tarefa = criarTarefa("Inexistente", categoriaId);
		UsuarioResponseDto usuario = usuarioService.criar(new UsuarioRequestDto("Inex", "inex.nm@teste.com"));

		assertThatThrownBy(() -> tarefaService.adicionarUsuario(tarefa.id(), 999999L))
				.isInstanceOf(UsuarioNaoEncontradoException.class);
		assertThatThrownBy(() -> tarefaService.adicionarUsuario(999999L, usuario.id()))
				.isInstanceOf(TarefaNaoEncontradaException.class);
		assertThatThrownBy(() -> tarefaService.listarPorUsuario(999999L))
				.isInstanceOf(UsuarioNaoEncontradoException.class);
	}

	private TarefaResponseDto criarTarefa(String titulo, Long categoriaId) {
		return tarefaService.criar(new TarefaRequestDto(
				titulo, null, StatusTarefa.INICIADO, LocalDate.now(), null, categoriaId));
	}

}
