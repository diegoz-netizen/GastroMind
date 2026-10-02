package com.gastromind.backendspring.repository;

import com.gastromind.backendspring.entity.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    Optional<Empleado> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}