package com.pet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pet.model.Tarefa;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

	boolean existsByCategoriaId(Long categoriaId);

	long countByCategoriaId(Long categoriaId);

	List<Tarefa> findByUsuariosId(Long usuarioId);

	boolean existsByUsuariosId(Long usuarioId);

	long countByUsuariosId(Long usuarioId);

}
