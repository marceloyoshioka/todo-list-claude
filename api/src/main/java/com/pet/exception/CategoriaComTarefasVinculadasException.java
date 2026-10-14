package com.pet.exception;

public class CategoriaComTarefasVinculadasException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CategoriaComTarefasVinculadasException(Long id) {
		super("A categoria " + id + " possui tarefas vinculadas e não pode ser removida. "
				+ "Remova ou realoque as tarefas antes de excluir a categoria.");
	}

}
