package com.pet.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pet.dto.TarefaRequestDto;
import com.pet.dto.TarefaResponseDto;
import com.pet.dto.UsuarioResumoDto;
import com.pet.exception.CategoriaNaoEncontradaException;
import com.pet.exception.TarefaNaoEncontradaException;
import com.pet.exception.UsuarioNaoEncontradoException;
import com.pet.model.Categoria;
import com.pet.model.Tarefa;
import com.pet.model.Usuario;
import com.pet.repository.CategoriaRepository;
import com.pet.repository.TarefaRepository;
import com.pet.repository.UsuarioRepository;

// @Transactional: a coleção "usuarios" é LAZY e precisa de sessão aberta ao montar o DTO.
@Service
@Transactional
public class TarefaService {

	@Autowired
	private TarefaRepository tarefaRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	public TarefaResponseDto criar(TarefaRequestDto dto) {
		Tarefa tarefa = new Tarefa();
		aplicarDto(tarefa, dto);
		return toResponseDto(tarefaRepository.save(tarefa));
	}

	@Transactional(readOnly = true)
	public List<TarefaResponseDto> listar() {
		return tarefaRepository.findAll().stream()
				.map(this::toResponseDto)
				.toList();
	}

	@Transactional(readOnly = true)
	public TarefaResponseDto buscarPorId(Long id) {
		return toResponseDto(buscarEntidadePorId(id));
	}

	public TarefaResponseDto atualizar(Long id, TarefaRequestDto dto) {
		Tarefa tarefa = buscarEntidadePorId(id);
		aplicarDto(tarefa, dto);
		return toResponseDto(tarefaRepository.save(tarefa));
	}

	public void deletar(Long id) {
		Tarefa tarefa = buscarEntidadePorId(id);
		tarefaRepository.delete(tarefa);
	}

	public TarefaResponseDto adicionarUsuario(Long tarefaId, Long usuarioId) {
		Tarefa tarefa = buscarEntidadePorId(tarefaId);
		Usuario usuario = buscarUsuarioPorId(usuarioId);
		tarefa.getUsuarios().add(usuario);
		return toResponseDto(tarefaRepository.save(tarefa));
	}

	public void removerUsuario(Long tarefaId, Long usuarioId) {
		Tarefa tarefa = buscarEntidadePorId(tarefaId);
		Usuario usuario = buscarUsuarioPorId(usuarioId);
		tarefa.getUsuarios().remove(usuario);
		tarefaRepository.save(tarefa);
	}

	@Transactional(readOnly = true)
	public List<TarefaResponseDto> listarPorUsuario(Long usuarioId) {
		buscarUsuarioPorId(usuarioId);
		return tarefaRepository.findByUsuariosId(usuarioId).stream()
				.map(this::toResponseDto)
				.toList();
	}

	private Tarefa buscarEntidadePorId(Long id) {
		return tarefaRepository.findById(id)
				.orElseThrow(() -> new TarefaNaoEncontradaException(id));
	}

	private Usuario buscarUsuarioPorId(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new UsuarioNaoEncontradoException(id));
	}

	private void aplicarDto(Tarefa tarefa, TarefaRequestDto dto) {
		Categoria categoria = categoriaRepository.findById(dto.categoriaId())
				.orElseThrow(() -> new CategoriaNaoEncontradaException(dto.categoriaId()));

		tarefa.setTitulo(dto.titulo());
		tarefa.setDescricao(dto.descricao());
		tarefa.setStatus(dto.status());
		tarefa.setDataInicio(dto.dataInicio());
		tarefa.setDataConclusao(dto.dataConclusao());
		tarefa.setCategoria(categoria);
	}

	private TarefaResponseDto toResponseDto(Tarefa tarefa) {
		List<UsuarioResumoDto> usuarios = tarefa.getUsuarios().stream()
				.sorted(Comparator.comparing(Usuario::getId))
				.map(u -> new UsuarioResumoDto(u.getId(), u.getNome()))
				.toList();

		return new TarefaResponseDto(
				tarefa.getId(),
				tarefa.getTitulo(),
				tarefa.getDescricao(),
				tarefa.getStatus(),
				tarefa.getDataInicio(),
				tarefa.getDataConclusao(),
				tarefa.getCategoria().getId(),
				tarefa.getCategoria().getNome(),
				usuarios
		);
	}

}
