package com.gastromind.backendspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.TimeZone;

@SpringBootApplication
@EnableConfigurationProperties
public class BackendSpringApplication {

	public static void main(String[] args) {
		// Hora global del sistema en UTC (misma BD para Spring y Django): no depende de la zona de cada máquina
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(BackendSpringApplication.class, args);
	}

}
