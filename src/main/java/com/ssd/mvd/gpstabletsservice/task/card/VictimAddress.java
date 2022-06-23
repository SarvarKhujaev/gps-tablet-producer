package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VictimAddress {
    private String flat;
    private String sNote;
    private String house;
    private String street;

    private Integer sRegionId;
    private Integer sOblastiId;
    private Integer sMahallyaId;
    private Integer sCountriesId;
    private Integer sSettlementId;
}
