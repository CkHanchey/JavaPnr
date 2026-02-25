package com.pnrgov.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdifactResponse {
    private Long reservationId;
    private String recordLocator;
    private String edifactContent;
    private LocalDateTime generatedAt;
}
