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
import com.pet.exception.EmailJaCadastradoException;
import com.pet.exception.UsuarioComTarefasVinculadasException;
import com.pet.model.StatusTarefa;

@SpringBootTest
class UsuarioServiceTest {

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private TarefaService tarefaService;

	@Autowired
	private CategoriaService categoriaService;

	@Test
	void naoDevePermitirEmailDuplicado() {
		usuarioService.criar(new UsuarioRequestDto("Ana", "ana.duplicado@teste.com"));

		assertThatThrownBy(() -> usuarioService.criar(new UsuarioRequestDto("Outra Ana", "ANA.duplicado@teste.com")))
				.isInstanceOf(EmailJaCadastradoException.class);
	}

	@Test
	void naoDeveAtualizarParaEmailDeOutroUsuario() {
		usuarioService.criar(new UsuarioRequestDto("Bia", "bia.update@teste.com"));
		UsuarioResponseDto carlos = usuarioService.criar(new UsuarioRequestDto("Carlos", "carlos.update@teste.com"));

		assertThatThrownBy(() -> usuarioService.atualizar(carlos.id(),
				new UsuarioRequestDto("Carlos", "bia.update@teste.com")))
				.isInstanceOf(EmailJaCadastradoException.class);
	}

	@Test
	void devePermitirAtualizarMantendoOProprioEmail() {
		UsuarioResponseDto usuario = usuarioService.criar(new UsuarioRequestDto("Dani", "dani.proprio@teste.com"));

		UsuarioResponseDto atualizado = usuarioService.atualizar(usuario.id(),
				new UsuarioRequestDto("Daniela", "dani.proprio@teste.com"));

		assertThat(atualizado.nome()).isEqualTo("Daniela");
	}

	@Test
	void naoDeveDeletarUsuarioVinculadoATarefas() {
		UsuarioResponseDto usuario = usuarioService.criar(new UsuarioRequestDto("Edu", "edu.vinculado@teste.com"));
		TarefaResponseDto tarefa = criarTarefa("Cat Usuario Bloqueio");
		tarefaService.adicionarUsuario(tarefa.id(), usuario.id());

		assertThatThrownBy(() -> usuarioService.deletar(usuario.id()))
				.isInstanceOf(UsuarioComTarefasVinculadasException.class);
		assertThat(usuarioService.buscarPorId(usuario.id()).quantidadeTarefas()).isEqualTo(1);
	}

	@Test
	void deveDeletarUsuarioAposDesvincularDasTarefas() {
		UsuarioResponseDto usuario = usuarioService.criar(new UsuarioRequestDto("Fabi", "fabi.desvincula@teste.com"));
		TarefaResponseDto tarefa = criarTarefa("Cat Usuario Desvincula");
		tarefaService.adicionarUsuario(tarefa.id(), usuario.id());
		tarefaService.removerUsuario(tarefa.id(), usuario.id());

		usuarioService.deletar(usuario.id());

		assertThat(usuarioService.listar()).noneMatch(u -> u.id().equals(usuario.id()));
	}

	private TarefaResponseDto criarTarefa(String nomeCategoria) {
		Long categoriaId = categoriaService.criar(new CategoriaRequestDto(nomeCategoria)).id();
		return tarefaService.criar(new TarefaRequestDto(
				"Tarefa de teste", null, StatusTarefa.INICIADO, LocalDate.now(), null, categoriaId));
	}

}
