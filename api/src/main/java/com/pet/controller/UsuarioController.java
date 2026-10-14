package com.pet.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pet.dto.TarefaResponseDto;
import com.pet.dto.UsuarioRequestDto;
import com.pet.dto.UsuarioResponseDto;
import com.pet.service.TarefaService;
import com.pet.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "CRUD de usuários e consulta das tarefas de cada um")
public class UsuarioController {

	@Autowired
	private UsuarioService service;

	@Autowired
	private TarefaService tarefaService;

	@PostMapping
	@Operation(summary = "Cria um novo usuário (email deve ser único)")
	public ResponseEntity<UsuarioResponseDto> criar(@Valid @RequestBody UsuarioRequestDto dto) {
		return ResponseEntity.ok(service.criar(dto));
	}

	@GetMapping
	@Operation(summary = "Lista todos os usuários")
	public ResponseEntity<List<UsuarioResponseDto>> listar() {
		return ResponseEntity.ok(service.listar());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca um usuário pelo id")
	public ResponseEntity<UsuarioResponseDto> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(service.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualiza um usuário existente")
	public ResponseEntity<UsuarioResponseDto> atualizar(@PathVariable Long id,
			@Valid @RequestBody UsuarioRequestDto dto) {
		return ResponseEntity.ok(service.atualizar(id, dto));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remove um usuário (bloqueado se estiver vinculado a tarefas)")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		service.deletar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/tarefas")
	@Operation(summary = "Lista as tarefas vinculadas a um usuário")
	public ResponseEntity<List<TarefaResponseDto>> listarTarefas(@PathVariable Long id) {
		return ResponseEntity.ok(tarefaService.listarPorUsuario(id));
	}

}
