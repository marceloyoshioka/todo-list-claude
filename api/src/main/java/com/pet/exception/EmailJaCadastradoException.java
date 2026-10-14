package com.pet.exception;

public class EmailJaCadastradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EmailJaCadastradoException(String email) {
		super("Já existe um usuário cadastrado com o email " + email + ".");
	}

}
