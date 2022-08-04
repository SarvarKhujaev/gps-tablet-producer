package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Last {
	private List< Integer > bbox;
	private String timestamp;
	private Double quality;
}