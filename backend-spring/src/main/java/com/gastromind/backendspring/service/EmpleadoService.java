package com.gastromind.backendspring.service;

import com.gastromind.backendspring.dto.EmpleadoRegistroRequest;
import com.gastromind.backendspring.dto.EmpleadoResponse;

public interface EmpleadoService {
    EmpleadoResponse registrarEmpleado(EmpleadoRegistroRequest request, String adminIdStr);
}