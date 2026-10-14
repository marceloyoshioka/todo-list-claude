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

import com.pet.dto.TarefaRequestDto;
import com.pet.dto.TarefaResponseDto;
import com.pet.service.TarefaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/tarefas")
@Tag(name = "Tarefas", description = "CRUD de tarefas")
public class TarefaController {

	@Autowired
	private TarefaService service;

	@PostMapping
	@Operation(summary = "Cria uma nova tarefa vinculada a uma categoria")
	public ResponseEntity<TarefaResponseDto> criar(@Valid @RequestBody TarefaRequestDto dto) {
		return ResponseEntity.ok(service.criar(dto));
	}

	@GetMapping
	@Operation(summary = "Lista todas as tarefas")
	public ResponseEntity<List<TarefaResponseDto>> listar() {
		return ResponseEntity.ok(service.listar());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca uma tarefa pelo id")
	public ResponseEntity<TarefaResponseDto> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(service.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualiza uma tarefa existente")
	public ResponseEntity<TarefaResponseDto> atualizar(@PathVariable Long id,
			@Valid @RequestBody TarefaRequestDto dto) {
		return ResponseEntity.ok(service.atualizar(id, dto));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remove uma tarefa")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		service.deletar(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/usuarios/{usuarioId}")
	@Operation(summary = "Vincula um usuário à tarefa (idempotente)")
	public ResponseEntity<TarefaResponseDto> adicionarUsuario(@PathVariable Long id,
			@PathVariable Long usuarioId) {
		return ResponseEntity.ok(service.adicionarUsuario(id, usuarioId));
	}

	@DeleteMapping("/{id}/usuarios/{usuarioId}")
	@Operation(summary = "Desvincula um usuário da tarefa (idempotente)")
	public ResponseEntity<Void> removerUsuario(@PathVariable Long id, @PathVariable Long usuarioId) {
		service.removerUsuario(id, usuarioId);
		return ResponseEntity.noContent().build();
	}

}
