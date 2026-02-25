package com.pnrgov.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultipleSampleDataRequest {
    private int passengerCount = 2;
    private int flightCount = 2;
    private boolean includeBags = true;
    private boolean includeSeats = true;
    private boolean includeDocuments = true;
    private boolean includePayment = true;
    private boolean includeCodeshare = false;
    private boolean includeThruFlight = false;
    private boolean includePhoneNumbers = true;
    private boolean includeAgencyInfo = true;
    private boolean includeCreditCard = true;
    private String receiver = "USCBP";
    private int count = 5;
}
