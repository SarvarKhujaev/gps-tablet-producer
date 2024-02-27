package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Best{
	private List< Integer > bbox;
	private String full_frame;
	private String normalized;
	private String timestamp;
	private Double quality;
}