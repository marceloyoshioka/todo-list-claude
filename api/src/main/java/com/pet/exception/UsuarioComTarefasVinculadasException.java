package com.pet.exception;

public class UsuarioComTarefasVinculadasException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public UsuarioComTarefasVinculadasException(Long id) {
		super("O usuário " + id + " está vinculado a tarefas e não pode ser removido. "
				+ "Desvincule o usuário das tarefas antes de excluí-lo.");
	}

}
