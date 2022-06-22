package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VictimAddress {
    private Integer sSettlementId; // ??
    private Integer sCountriesId; // id of country
    private Integer sMahallyaId; // mahalla id
    private Integer sOblastiId; // oblast id
    private Integer sRegionId;
    private String street;
    private String sNote; // note about Victim
    private String house; // house data
    private String flat;
}
