package com.pnrgov.api.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultipleSampleDataResponse {
    private int totalGenerated;
    private List<SampleDataResponse> reservations = new ArrayList<>();
    private String message;
}
