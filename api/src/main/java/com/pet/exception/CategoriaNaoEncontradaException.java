package com.pet.exception;

public class CategoriaNaoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CategoriaNaoEncontradaException(Long id) {
		super("Categoria com id " + id + " não encontrada.");
	}

}
