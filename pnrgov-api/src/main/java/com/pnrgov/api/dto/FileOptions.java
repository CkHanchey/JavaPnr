package com.pnrgov.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileOptions {
    private boolean hasBags;
    private boolean hasSeats;
    private boolean hasDocuments;
    private boolean hasPayment;
    private boolean isCodeshare;
    private boolean isThruFlight;
    private boolean hasPhones;
    private boolean hasAgency;
    private boolean hasCreditCard;
}
