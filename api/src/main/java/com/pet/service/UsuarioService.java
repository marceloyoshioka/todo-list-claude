package com.pet.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pet.dto.UsuarioRequestDto;
import com.pet.dto.UsuarioResponseDto;
import com.pet.exception.EmailJaCadastradoException;
import com.pet.exception.UsuarioComTarefasVinculadasException;
import com.pet.exception.UsuarioNaoEncontradoException;
import com.pet.model.Usuario;
import com.pet.repository.TarefaRepository;
import com.pet.repository.UsuarioRepository;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private TarefaRepository tarefaRepository;

	public UsuarioResponseDto criar(UsuarioRequestDto dto) {
		String email = normalizarEmail(dto.email());
		if (usuarioRepository.existsByEmail(email)) {
			throw new EmailJaCadastradoException(email);
		}
		Usuario usuario = new Usuario(dto.nome(), email);
		return toResponseDto(usuarioRepository.save(usuario));
	}

	public List<UsuarioResponseDto> listar() {
		return usuarioRepository.findAll().stream()
				.map(this::toResponseDto)
				.toList();
	}

	public UsuarioResponseDto buscarPorId(Long id) {
		return toResponseDto(buscarEntidadePorId(id));
	}

	public UsuarioResponseDto atualizar(Long id, UsuarioRequestDto dto) {
		Usuario usuario = buscarEntidadePorId(id);
		String email = normalizarEmail(dto.email());
		if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
			throw new EmailJaCadastradoException(email);
		}
		usuario.setNome(dto.nome());
		usuario.setEmail(email);
		return toResponseDto(usuarioRepository.save(usuario));
	}

	public void deletar(Long id) {
		Usuario usuario = buscarEntidadePorId(id);
		if (tarefaRepository.existsByUsuariosId(id)) {
			throw new UsuarioComTarefasVinculadasException(id);
		}
		usuarioRepository.delete(usuario);
	}

	private Usuario buscarEntidadePorId(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new UsuarioNaoEncontradoException(id));
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase();
	}

	private UsuarioResponseDto toResponseDto(Usuario usuario) {
		return new UsuarioResponseDto(
				usuario.getId(),
				usuario.getNome(),
				usuario.getEmail(),
				(int) tarefaRepository.countByUsuariosId(usuario.getId())
		);
	}

}
