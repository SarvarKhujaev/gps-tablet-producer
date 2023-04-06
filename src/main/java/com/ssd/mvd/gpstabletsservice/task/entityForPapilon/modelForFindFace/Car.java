package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport.First;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport.Last;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Car {
	private Last last;
	private Best best;
	private First first;
}