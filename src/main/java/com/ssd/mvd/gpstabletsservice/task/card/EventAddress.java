package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties ( ignoreUnknown = true )
public class EventAddress {
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