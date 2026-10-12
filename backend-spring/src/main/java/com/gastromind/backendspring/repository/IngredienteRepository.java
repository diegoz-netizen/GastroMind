package com.gastromind.backendspring.repository;

import com.gastromind.backendspring.entity.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Todas las consultas reciben el restauranteId (obtenido con EmpleadoActual, nunca del cliente):
 * un insumo de otro restaurante se trata como inexistente.
 */
@Repository
public interface IngredienteRepository extends JpaRepository<Ingrediente, Long> {

    List<Ingrediente> findByRestauranteIdOrderByNombreAsc(Long restauranteId);

    Optional<Ingrediente> findByIdAndRestauranteId(Long id, Long restauranteId);

    boolean existsByRestauranteIdAndNombreIgnoreCase(Long restauranteId, String nombre);
}
