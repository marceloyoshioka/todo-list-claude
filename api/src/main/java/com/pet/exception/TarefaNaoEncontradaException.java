package com.pet.exception;

public class TarefaNaoEncontradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TarefaNaoEncontradaException(Long id) {
		super("Tarefa com id " + id + " não encontrada.");
	}

}
