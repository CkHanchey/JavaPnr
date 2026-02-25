package com.pnrgov.api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManifestResponse {
    private String edifactContent;
    private int passengerCount;
    private LocalDateTime generatedAt;
}
