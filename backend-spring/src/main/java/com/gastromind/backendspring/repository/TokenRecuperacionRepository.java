package com.gastromind.backendspring.repository;

import com.gastromind.backendspring.entity.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    /** Enlaces del empleado que aún no se usaron (al pedir uno nuevo, se anulan). */
    List<TokenRecuperacion> findByEmpleadoIdAndUsadoEnIsNull(Long empleadoId);
}
