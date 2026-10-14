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

import com.pet.dto.CategoriaRequestDto;
import com.pet.dto.CategoriaResponseDto;
import com.pet.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "CRUD de categorias de tarefas")
public class CategoriaController {

	@Autowired
	private CategoriaService service;

	@PostMapping
	@Operation(summary = "Cria uma nova categoria")
	public ResponseEntity<CategoriaResponseDto> criar(@Valid @RequestBody CategoriaRequestDto dto) {
		return ResponseEntity.ok(service.criar(dto));
	}

	@GetMapping
	@Operation(summary = "Lista todas as categorias")
	public ResponseEntity<List<CategoriaResponseDto>> listar() {
		return ResponseEntity.ok(service.listar());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca uma categoria pelo id")
	public ResponseEntity<CategoriaResponseDto> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(service.buscarPorId(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualiza uma categoria existente")
	public ResponseEntity<CategoriaResponseDto> atualizar(@PathVariable Long id,
			@Valid @RequestBody CategoriaRequestDto dto) {
		return ResponseEntity.ok(service.atualizar(id, dto));
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Remove uma categoria (bloqueado se houver tarefas vinculadas)")
	public ResponseEntity<Void> deletar(@PathVariable Long id) {
		service.deletar(id);
		return ResponseEntity.noContent().build();
	}

}
