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
public class BulkGenerationResponse {
    private List<GeneratedFile> files = new ArrayList<>();
    private int totalFiles;
    private LocalDateTime generatedAt;
}
