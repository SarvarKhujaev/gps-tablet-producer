package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class HumanAddress {
	private Integer sRegionId;
	private Integer sOblastiId;
	private Integer sMahallyaId;
	private Integer sCountriesId;
	private Integer sSettlementId;

	private String flat;
	private String sNote;
	private String house;
	private String street;
}