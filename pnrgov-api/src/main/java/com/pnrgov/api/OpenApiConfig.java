package com.pnrgov.api;

import io.swagger.v3.oas.models.Paths;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    /**
     * Desired path order matching the .NET API on port 5000.
     * Extra Java-only endpoints are appended to the relevant group.
     */
    private static final List<String> PATH_ORDER = Arrays.asList(
        "/api/BulkEdifact/generate",
        "/api/Edifact/generate/{reservationId}",
        "/api/Edifact/download/{reservationId}",
        "/api/Edifact/manifest/generate",
        "/api/Edifact/manifest/download",
        "/api/Reservations",
        "/api/Reservations/{id}",
        "/api/Reservations/by-locator/{recordLocator}",
        "/api/Reservations/all",
        "/api/SampleData/generate",
        "/api/SampleData/generate-multiple",
        "/api/Edifact/generate"
    );

    @Bean
    public OpenApiCustomizer pathOrderCustomizer() {
        return openApi -> {
            Paths original = openApi.getPaths();
            if (original == null) return;

            Paths ordered = new Paths();

            // Add paths in the defined order first
            for (String path : PATH_ORDER) {
                if (original.containsKey(path)) {
                    ordered.addPathItem(path, original.get(path));
                }
            }

            // Append any remaining paths not explicitly ordered
            original.forEach((path, item) -> {
                if (!ordered.containsKey(path)) {
                    ordered.addPathItem(path, item);
                }
            });

            openApi.setPaths(ordered);
        };
    }
}
