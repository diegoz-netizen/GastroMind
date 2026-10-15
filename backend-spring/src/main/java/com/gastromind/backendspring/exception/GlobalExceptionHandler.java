package com.gastromind.backendspring.exception;

import com.gastromind.backendspring.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HL-014: todos los errores de la API salen como {@link ErrorResponse} ({error, mensaje, campos?}),
 * nunca como texto plano ni con stack trace.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Los servicios lanzan ResponseStatusException con el código y el motivo (404, 409, 403...). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> estado(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String mensaje = ex.getReason() != null ? ex.getReason() : mensajePorDefecto(status);
        return responder(status, new ErrorResponse(CodigoError.desde(status), mensaje));
    }

    /** @Valid en los DTO: un mensaje por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST,
                new ErrorResponse(CodigoError.VALIDACION, "Hay campos con errores.", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, new ErrorResponse(CodigoError.SOLICITUD_INVALIDA,
                "El cuerpo de la solicitud no es un JSON válido."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> metodo(HttpRequestMethodNotSupportedException ex) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, new ErrorResponse(CodigoError.METODO_NO_PERMITIDO,
                "El método " + ex.getMethod() + " no está permitido en esta ruta."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> rutaInexistente(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, new ErrorResponse(CodigoError.NO_ENCONTRADO, "La ruta no existe."));
    }

    /** Llega desde SecurityConfig: sin token, token inválido o vencido, o empleado INACTIVO. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> noAutenticado(AuthenticationException ex) {
        return responder(HttpStatus.UNAUTHORIZED, new ErrorResponse(CodigoError.NO_AUTENTICADO,
                "Tu sesión no es válida o expiró. Vuelve a iniciar sesión."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> sinPermiso(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, new ErrorResponse(CodigoError.SIN_PERMISO,
                "No tienes permiso para esta acción."));
    }

    /** Cualquier otro error: se registra en el log y el cliente solo ve un mensaje genérico. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> inesperado(Exception ex) {
        // Errores de Spring MVC que ya traen su código (parámetro faltante, Content-Type no soportado...)
        if (ex instanceof org.springframework.web.ErrorResponse conEstado
                && conEstado.getStatusCode().is4xxClientError()) {
            HttpStatusCode status = conEstado.getStatusCode();
            String detalle = conEstado.getBody().getDetail();
            return responder(status, new ErrorResponse(CodigoError.desde(status),
                    detalle != null ? detalle : mensajePorDefecto(status)));
        }
        log.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, new ErrorResponse(CodigoError.ERROR_INTERNO,
                "Ocurrió un error inesperado. Intenta de nuevo."));
    }

    private static ResponseEntity<ErrorResponse> responder(HttpStatusCode status, ErrorResponse cuerpo) {
        return ResponseEntity.status(status).body(cuerpo);
    }

    private static String mensajePorDefecto(HttpStatusCode status) {
        HttpStatus conocido = HttpStatus.resolve(status.value());
        return conocido != null ? conocido.getReasonPhrase() : "Error";
    }
}
