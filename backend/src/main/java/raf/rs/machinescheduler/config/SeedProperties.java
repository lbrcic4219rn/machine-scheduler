package raf.rs.machinescheduler.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(@NotBlank String adminPassword, @NotBlank String userPasswordPrefix) {
}
