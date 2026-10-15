package com.gastromind.backendspring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * HL-014: formato común de errores, igual en Spring y en Django.
 *
 * <pre>
 * { "error": "VALIDACION", "mensaje": "Hay campos con errores.", "campos": { "correo": "El correo es obligatorio" } }
 * </pre>
 *
 * @param error   código estable para el cliente (ver {@link com.gastromind.backendspring.exception.CodigoError})
 * @param mensaje texto para mostrar al usuario
 * @param campos  solo en errores de validación: campo → mensaje (no se envía si es null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String error, String mensaje, Map<String, String> campos) {

    public ErrorResponse(String error, String mensaje) {
        this(error, mensaje, null);
    }
}
