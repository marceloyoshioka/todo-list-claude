package com.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pet.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, Long id);

}
