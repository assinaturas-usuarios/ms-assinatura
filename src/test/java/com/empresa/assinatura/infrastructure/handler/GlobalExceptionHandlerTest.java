package com.empresa.assinatura.infrastructure.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.empresa.assinatura.infrastructure.exception.AssinaturaJaAtivaException;
import com.empresa.assinatura.infrastructure.exception.AssinaturaNaoCancelaveException;
import com.empresa.assinatura.infrastructure.exception.AssinaturaNaoEncontradaException;
import com.empresa.assinatura.infrastructure.exception.PlanoInvalidoException;
import com.empresa.assinatura.infrastructure.exception.UsuarioNaoEncontradoException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GlobalExceptionHandler - ms-assinatura")
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @Mock
  private WebExchangeBindException webExchangeEx;
  @Mock
  private BindingResult bindingResult;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  @DisplayName("404 para AssinaturaNaoEncontradaException")
  void deveRetornar404ParaAssinaturaNaoEncontrada() {
    ProblemDetail result = handler.handleNaoEncontrada(
        new AssinaturaNaoEncontradaException(UUID.randomUUID()));
    assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("404 para UsuarioNaoEncontradoException")
  void deveRetornar404ParaUsuarioNaoEncontrado() {
    ProblemDetail result = handler.handleUsuarioNaoEncontrado(
        new UsuarioNaoEncontradoException(UUID.randomUUID()));
    assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
  }

  @Test
  @DisplayName("409 para AssinaturaJaAtivaException")
  void deveRetornar409ParaJaAtiva() {
    ProblemDetail result = handler.handleAssinaturaJaAtiva(
        new AssinaturaJaAtivaException(UUID.randomUUID()));
    assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
  }

  @Test
  @DisplayName("409 para AssinaturaNaoCancelaveException")
  void deveRetornar409ParaNaoCancelavel() {
    ProblemDetail result = handler.handleAssinaturaNaoCancelavel(
        new AssinaturaNaoCancelaveException(UUID.randomUUID()));
    assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
  }

  @Test
  @DisplayName("422 para PlanoInvalidoException")
  void deveRetornar422ParaPlanoInvalido() {
    ProblemDetail result = handler.handlePlanoInvalido(new PlanoInvalidoException("INEXISTENTE"));
    assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
  }

  @Test
  @DisplayName("422 para WebExchangeBindException com campos")
  void deveRetornar422ParaValidacao() {
    FieldError fieldError = new FieldError("assinatura", "usuarioId", "obrigatorio");
    when(webExchangeEx.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
    ProblemDetail result = handler.handleValidacao(webExchangeEx);
    assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
  }

  @Test
  @DisplayName("503 para DataAccessException")
  void deveRetornar503ParaErroDatabase() {
    DataAccessException ex = new DataAccessException("erro de banco") {};
    ProblemDetail result = handler.handleDataAccessException(ex);
    assertThat(result.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    assertThat(result.getTitle()).contains("Banco de Dados");
  }

  @Test
  @DisplayName("500 para excecao generica")
  void deveRetornar500ParaExcecaoGenerica() {
    ProblemDetail result = handler.handleGenerico(new RuntimeException("erro"));
    assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
  }
}