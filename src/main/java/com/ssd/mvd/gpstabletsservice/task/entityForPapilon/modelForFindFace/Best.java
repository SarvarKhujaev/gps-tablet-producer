package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Best{
	private List< Integer > bbox;
	private String full_frame;
	private String normalized;
	private String timestamp;
	private Double quality;
}