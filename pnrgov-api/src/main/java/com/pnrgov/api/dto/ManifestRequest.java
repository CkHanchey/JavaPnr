package com.pnrgov.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManifestRequest {
    private int passengerCount = 5;
    private String airline;
    private String flightNumber;
    private String receiver = "USCBP";
}
