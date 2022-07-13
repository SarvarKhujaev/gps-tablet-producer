package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAddress {
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