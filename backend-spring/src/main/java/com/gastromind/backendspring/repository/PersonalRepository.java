package com.gastromind.backendspring.repository;

import com.gastromind.backendspring.entity.Empleado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * HU-03: consultas de personal filtradas por restaurante.
 * Se separa de EmpleadoRepository (HU-02) para no modificar ese archivo.
 */
@Repository
public interface PersonalRepository extends JpaRepository<Empleado, Long> {

    @EntityGraph(attributePaths = {"restaurante", "rol"})
    Optional<Empleado> findByCorreo(String correo);

    @EntityGraph(attributePaths = {"rol"})
    List<Empleado> findByRestauranteIdOrderByIdAsc(Long restauranteId);

    @EntityGraph(attributePaths = {"rol"})
    Optional<Empleado> findByIdAndRestauranteId(Long id, Long restauranteId);
}
