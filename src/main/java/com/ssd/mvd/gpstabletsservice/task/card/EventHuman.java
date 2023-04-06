package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.jackson.Jacksonized;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class EventHuman {
	private Integer humanId;
	private Integer hospital;
	private Integer hospitaldept;
	private Integer treatmentkind;
	private Integer professionidcaller;

	private String phone;
	private String checkin;
	private String lastName;
	private String firstName;
	private String middleName;
	private String dateOfBirth;

	@JsonDeserialize
	private HumanAddress humanAddress;
}