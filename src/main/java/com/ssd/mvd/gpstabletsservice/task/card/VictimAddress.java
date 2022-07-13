package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VictimAddress {
	private Integer sRegionId;
	private Integer sCountriesId;
	private Integer sOblastiId;
	private Integer sSettlementId;
	private Integer sMahallyaId;
	private String street;
	private String flat;
	private String sNote;
	private String house;
}