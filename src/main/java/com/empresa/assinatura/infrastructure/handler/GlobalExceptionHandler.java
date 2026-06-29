package com.empresa.assinatura.infrastructure.handler;

import com.empresa.assinatura.infrastructure.exception.AssinaturaJaAtivaException;
import com.empresa.assinatura.infrastructure.exception.AssinaturaNaoCancelaveException;
import com.empresa.assinatura.infrastructure.exception.AssinaturaNaoEncontradaException;
import com.empresa.assinatura.infrastructure.exception.PlanoInvalidoException;
import com.empresa.assinatura.infrastructure.exception.UsuarioNaoEncontradoException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

/**
 * Classe responsável por tratar exceções globais na aplicação.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Trata a exceção AssinaturaNaoEncontradaException.
   *
   * @param ex AssinaturaNaoEncontradaException lançada
   * @return um ProblemDetail, no formato RFC 7807, com informações sobre o erro
   */
  @ExceptionHandler(AssinaturaNaoEncontradaException.class)
  public ProblemDetail handleNaoEncontrada(AssinaturaNaoEncontradaException ex) {
    log.warn("Assinatura não encontrada: {}", ex.getMessage());
    return construirProblem(HttpStatus.NOT_FOUND, "Assinatura não encontrada", ex.getMessage(),
        null);
  }

  /**
   * Trata a exceção UsuarioNaoEncontradoException.
   *
   * @param ex UsuarioNaoEncontradoException lançada
   * @return um ProblemDetail, no formato RFC 7807, com informações sobre o erro
   */
  @ExceptionHandler(UsuarioNaoEncontradoException.class)
  public ProblemDetail handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
    log.warn("Usuário não encontrado: {}", ex.getMessage());
    return construirProblem(HttpStatus.NOT_FOUND, "Usuário não encontrado", ex.getMessage(), null);
  }

  /**
   * Trata a exceção AssinaturaJaAtivaException.
   *
   * @param ex AssinaturaJaAtivaException lançada
   * @return um ProblemDetail, no formato RFC 7807, com informações sobre o erro
   */
  @ExceptionHandler(AssinaturaJaAtivaException.class)
  public ProblemDetail handleAssinaturaJaAtiva(AssinaturaJaAtivaException ex) {
    log.warn("Usuário já possui assinatura ativa: {}", ex.getMessage());
    return construirProblem(HttpStatus.CONFLICT, "Assinatura já ativa", ex.getMessage(), null);
  }

  /**
   * Trata a exceção AssinaturaNaoCancelaveException.
   *
   * @param ex AssinaturaNaoCancelaveException lançada
   * @return um ProblemDetail, no formato RFC 7807, com informações sobre o erro
   */
  @ExceptionHandler(AssinaturaNaoCancelaveException.class)
  public ProblemDetail handleAssinaturaNaoCancelavel(AssinaturaNaoCancelaveException ex) {
    log.warn("Tentativa de cancelar assinatura já cancelada: {}", ex.getMessage());
    return construirProblem(HttpStatus.CONFLICT, "Assinatura já cancelada", ex.getMessage(), null);
  }

  /**
   * Trata a exceção PlanoInvalidoException.
   *
   * @param ex PlanoInvalidoException lançada
   * @return um ProblemDetail, no formato RFC 7807, com informações sobre o erro
   */
  @ExceptionHandler(PlanoInvalidoException.class)
  public ProblemDetail handlePlanoInvalido(PlanoInvalidoException ex) {
    log.warn("Plano inválido: {}", ex.getMessage());
    return construirProblem(HttpStatus.UNPROCESSABLE_ENTITY, "Plano inválido", ex.getMessage(),
        null);
  }

  /**
   * Trata a exceção WebExchangeBindException, que ocorre quando há erros de validação nos dados de
   * entrada.
   *
   * @param ex WebExchangeBindException lançada
   * @return um ProblemDetail, no formato RFC 7807, com informações sobre o erro
   */
  @ExceptionHandler(WebExchangeBindException.class)
  public ProblemDetail handleValidacao(WebExchangeBindException ex) {
    Map<String, String> campos = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      campos.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    log.warn("Erro de validação: {}", campos);
    ProblemDetail problem = construirProblem(HttpStatus.UNPROCESSABLE_ENTITY, "Erro de validação",
        "Dados inválidos", campos);
    problem.setProperty("campos", campos);
    return problem;
  }

  /**
   * Trata a exceção genérica, que ocorre quando há erros inesperados no sistema.
   *
   * @param ex Exception lançada
   * @return um ProblemDetail, no formato RFC 7807, com informações sobre o erro
   */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGenerico(Exception ex) {
    log.error("Erro interno não esperado", ex);
    return construirProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
        "Ocorreu um erro inesperado", null);
  }

  private ProblemDetail construirProblem(HttpStatus status, String titulo, String detalhe,
      Map<String, String> campos) {
    ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setTitle(titulo);
    problem.setDetail(detalhe);
    problem.setType(URI.create("ms-assinatura/erros"));
    if (campos != null) {
      problem.setProperty("campos", campos);
    }
    return problem;
  }
}
