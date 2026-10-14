package com.pet.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pet.dto.CategoriaRequestDto;
import com.pet.dto.CategoriaResponseDto;
import com.pet.exception.CategoriaComTarefasVinculadasException;
import com.pet.exception.CategoriaNaoEncontradaException;
import com.pet.model.Categoria;
import com.pet.repository.CategoriaRepository;
import com.pet.repository.TarefaRepository;

@Service
public class CategoriaService {

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private TarefaRepository tarefaRepository;

	public CategoriaResponseDto criar(CategoriaRequestDto dto) {
		Categoria categoria = new Categoria(dto.nome());
		return toResponseDto(categoriaRepository.save(categoria));
	}

	public List<CategoriaResponseDto> listar() {
		return categoriaRepository.findAll().stream()
				.map(this::toResponseDto)
				.toList();
	}

	public CategoriaResponseDto buscarPorId(Long id) {
		return toResponseDto(buscarEntidadePorId(id));
	}

	public CategoriaResponseDto atualizar(Long id, CategoriaRequestDto dto) {
		Categoria categoria = buscarEntidadePorId(id);
		categoria.setNome(dto.nome());
		return toResponseDto(categoriaRepository.save(categoria));
	}

	public void deletar(Long id) {
		Categoria categoria = buscarEntidadePorId(id);
		if (tarefaRepository.existsByCategoriaId(id)) {
			throw new CategoriaComTarefasVinculadasException(id);
		}
		categoriaRepository.delete(categoria);
	}

	protected Categoria buscarEntidadePorId(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new CategoriaNaoEncontradaException(id));
	}

	private CategoriaResponseDto toResponseDto(Categoria categoria) {
		return new CategoriaResponseDto(
				categoria.getId(),
				categoria.getNome(),
				(int) tarefaRepository.countByCategoriaId(categoria.getId())
		);
	}

}
