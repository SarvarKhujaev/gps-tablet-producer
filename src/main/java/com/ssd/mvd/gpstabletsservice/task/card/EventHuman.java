package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

	private HumanAddress humanAddress;
}