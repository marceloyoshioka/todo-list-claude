package com.pet.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pet.dto.ErrorResponse;
import com.pet.exception.CategoriaComTarefasVinculadasException;
import com.pet.exception.CategoriaNaoEncontradaException;
import com.pet.exception.EmailJaCadastradoException;
import com.pet.exception.TarefaNaoEncontradaException;
import com.pet.exception.UsuarioComTarefasVinculadasException;
import com.pet.exception.UsuarioNaoEncontradoException;

/**
 * Restrito a com.pet.controller: assim, requisições que não passam pelos
 * nossos controllers (ex.: 404 de rota inexistente, /h2-console) continuam
 * sendo tratadas pelo mecanismo padrão do Spring, em vez de caírem aqui como
 * erro 500 genérico.
 */
@RestControllerAdvice(basePackages = "com.pet.controller")
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler({ CategoriaNaoEncontradaException.class, TarefaNaoEncontradaException.class,
			UsuarioNaoEncontradoException.class })
	public ResponseEntity<ErrorResponse> handleNaoEncontrada(RuntimeException ex) {
		ErrorResponse error = new ErrorResponse(
				ex.getMessage(),
				LocalDateTime.now(),
				HttpStatus.NOT_FOUND.value()
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler({ CategoriaComTarefasVinculadasException.class, UsuarioComTarefasVinculadasException.class,
			EmailJaCadastradoException.class })
	public ResponseEntity<ErrorResponse> handleConflito(RuntimeException ex) {
		ErrorResponse error = new ErrorResponse(
				ex.getMessage(),
				LocalDateTime.now(),
				HttpStatus.CONFLICT.value()
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidacao(MethodArgumentNotValidException ex) {
		List<String> erros = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.toList();

		ErrorResponse error = new ErrorResponse(
				"Erro de validação nos dados enviados.",
				LocalDateTime.now(),
				HttpStatus.BAD_REQUEST.value(),
				erros
		);
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
		log.error("Erro inesperado ao processar requisição", ex);
		ErrorResponse error = new ErrorResponse(
				"Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.",
				LocalDateTime.now(),
				HttpStatus.INTERNAL_SERVER_ERROR.value()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

}
