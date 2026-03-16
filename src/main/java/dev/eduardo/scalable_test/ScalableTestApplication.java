package dev.eduardo.scalable_test;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(
        title = "Scalable Task Processing API",
        description = "Distributed async task processing system using AWS SQS FIFO queues and PostgreSQL",
        version = "0.0.1"
))
@SpringBootApplication
public class ScalableTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScalableTestApplication.class, args);
    }
}