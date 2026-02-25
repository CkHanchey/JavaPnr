package com.pnrgov.api.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkGenerationRequest {
    private int fileCount = 10;
    private int minPassengers = 1;
    private int maxPassengers = 5;
    private int minFlights = 1;
    private int maxFlights = 3;
    private String receiver = "USCBP";
}
