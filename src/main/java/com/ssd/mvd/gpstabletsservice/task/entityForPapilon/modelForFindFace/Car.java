package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

import com.ssd.mvd.entity.modelForPassport.First;
import com.ssd.mvd.entity.modelForPassport.Last;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car{
	private Last last;
	private Best best;
	private First first;
}