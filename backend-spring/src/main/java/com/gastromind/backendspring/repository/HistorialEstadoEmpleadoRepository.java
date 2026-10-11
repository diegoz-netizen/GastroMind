package com.gastromind.backendspring.repository;

import com.gastromind.backendspring.entity.HistorialEstadoEmpleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialEstadoEmpleadoRepository extends JpaRepository<HistorialEstadoEmpleado, Long> {
}
