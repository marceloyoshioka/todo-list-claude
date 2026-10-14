package com.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pet.model.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
