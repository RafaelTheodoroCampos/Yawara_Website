package br.com.yamarasolution.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String UNPROCESSABLE_ENTITY_MESSAGE = "Unprocessable Entity";


  /**
   * If the exception is a DataIntegrityViolationException, MethodArgumentTypeMismatchException, or
   * AccountException, then return a response entity with a status of 422 and a message of "Unprocessable
   * Entity"
   * 
   * @param ex The exception that was thrown
   * @return A ResponseEntity object is being returned.
   */
  @ExceptionHandler({ DataIntegrityViolationException.class, MethodArgumentTypeMismatchException.class,
      AccountExcpetion.class, CategoryException.class })
  public ResponseEntity<ApiError> handleExceptions(RuntimeException ex) {
    log.error("Error: ", ex);
    return new ResponseEntity<>(
        new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, UNPROCESSABLE_ENTITY_MESSAGE, ex.getLocalizedMessage()),
        HttpStatus.UNPROCESSABLE_ENTITY);
  }


  /**
   * It takes the exception, the headers, the status, and the request, and returns a response entity with
   * the apiError, the headers, and the status
   * 
   * @param ex The exception that was thrown
   * @param headers The HTTP headers to be written to the response.
   * @param status The HTTP status code to return.
   * @param request The current request.
   * @return A ResponseEntity object.
   */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      org.springframework.http.HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    List<String> errors = ex.getBindingResult().getFieldErrors().stream()
        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
        .collect(Collectors.toList());
    ApiError apiError = new ApiError(status, "Validation Error", errors);
    return new ResponseEntity<>(apiError, headers, status);
  }

  /**
   * This function is called when the request body is not readable
   * 
   * @param ex The exception that was thrown.
   * @param headers The HTTP headers that were sent with the request.
   * @param status The HTTP status code to return.
   * @param request The current request.
   * @return A ResponseEntity object.
   */
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {
    ApiError apiError = new ApiError(status, "Malformed JSON request", ex.getLocalizedMessage());
    return new ResponseEntity<>(apiError, headers, status);
  }

}
