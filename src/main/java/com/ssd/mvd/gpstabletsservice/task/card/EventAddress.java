package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAddress {
    private Integer sSettlementId;
    private Integer sCountriesId;
    private Integer sMahallyaId;
    private Integer sOblastiId;
    private Integer sRegionId;
    private String street;
    private String sNote;
    private String house;
    private String flat;
}
