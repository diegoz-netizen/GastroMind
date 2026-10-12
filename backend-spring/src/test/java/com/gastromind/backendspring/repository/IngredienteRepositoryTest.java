package com.gastromind.backendspring.repository;

import com.gastromind.backendspring.entity.Ingrediente;
import com.gastromind.backendspring.entity.Restaurante;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** HU-05: el repositorio de insumos respeta el aislamiento por restaurante y la unicidad del nombre. */
@DataJpaTest
@ActiveProfiles("test")
class IngredienteRepositoryTest {

    @Autowired
    private IngredienteRepository ingredienteRepository;

    @Autowired
    private EntityManager entityManager;

    private Restaurante central;
    private Restaurante norte;

    @BeforeEach
    void setUp() {
        central = Restaurante.builder().nombre("Sede Central").build();
        norte = Restaurante.builder().nombre("Sede Norte").build();
        entityManager.persist(central);
        entityManager.persist(norte);
    }

    private Ingrediente insumo(Restaurante restaurante, String nombre) {
        Ingrediente ingrediente = Ingrediente.builder()
                .restaurante(restaurante).nombre(nombre).unidadMedida("kg")
                .costoUnitario(new BigDecimal("12.50")).build();
        entityManager.persist(ingrediente);
        return ingrediente;
    }

    @Test
    void listar_DeberiaTraerSoloLosInsumosDelRestauranteOrdenadosPorNombre() {
        insumo(central, "Tomate");
        insumo(central, "Arroz");
        insumo(norte, "Pollo");
        entityManager.flush();

        List<Ingrediente> deCentral = ingredienteRepository.findByRestauranteIdOrderByNombreAsc(central.getId());

        assertEquals(List.of("Arroz", "Tomate"), deCentral.stream().map(Ingrediente::getNombre).toList());
    }

    @Test
    void buscarPorIdDeOtroRestaurante_NoDeberiaEncontrarlo() {
        Ingrediente polloNorte = insumo(norte, "Pollo");
        entityManager.flush();

        assertTrue(ingredienteRepository.findByIdAndRestauranteId(polloNorte.getId(), central.getId()).isEmpty());
        assertTrue(ingredienteRepository.findByIdAndRestauranteId(polloNorte.getId(), norte.getId()).isPresent());
    }

    @Test
    void existeNombre_IgnoraMayusculasYEsPorRestaurante() {
        insumo(central, "Tomate");
        entityManager.flush();

        assertTrue(ingredienteRepository.existsByRestauranteIdAndNombreIgnoreCase(central.getId(), "TOMATE"));
        assertFalse(ingredienteRepository.existsByRestauranteIdAndNombreIgnoreCase(norte.getId(), "Tomate"));
    }

    @Test
    void mismoNombreEnElMismoRestaurante_DeberiaViolarLaRestriccionUnica() {
        insumo(central, "Tomate");
        entityManager.flush();

        // Con ids IDENTITY, Hibernate ejecuta el INSERT al hacer persist (no recién en el flush)
        assertThrows(PersistenceException.class, () -> {
            insumo(central, "Tomate");
            entityManager.flush();
        });
    }

    @Test
    void mismoNombreEnOtroRestaurante_DeberiaPermitirse() {
        insumo(central, "Tomate");
        insumo(norte, "Tomate");

        assertDoesNotThrow(() -> entityManager.flush());
    }

    @Test
    void valoresPorDefecto_DeberianCoincidirConElScriptSql() {
        Ingrediente tomate = insumo(central, "Tomate");
        entityManager.flush();
        entityManager.clear();

        Ingrediente guardado = ingredienteRepository.findById(tomate.getId()).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(guardado.getStockActual()));
        assertEquals(0, BigDecimal.ZERO.compareTo(guardado.getStockMinimo()));
        assertEquals("RECETA", guardado.getTipoControl());
    }
}
