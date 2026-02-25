package com.pnrgov.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedFile {
    private String fileName;
    private String recordLocator;
    private String content;
    private int passengerCount;
    private int flightCount;
    private FileOptions options;
}
