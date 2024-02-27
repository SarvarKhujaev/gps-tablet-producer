package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.jackson.Jacksonized;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties ( ignoreUnknown = true )
public class EventAddress {
	private String flat;
	private String sNote;
	private String house;
	private String street;

	private int sRegionId;
	private int sOblastiId;
	private int sMahallyaId;
	private int sCountriesId;
	private int sSettlementId;
}