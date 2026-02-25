package com.pnrgov.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SampleDataResponse {
    private Long reservationId;
    private String recordLocator;
    private int passengerCount;
    private int flightCount;
    private String message;
}
