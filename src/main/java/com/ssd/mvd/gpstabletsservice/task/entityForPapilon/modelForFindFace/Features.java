package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport.Model;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport.Make;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Features{
	private Body body;
	private Make make;
	private Color color;
	private Model model;
	private LicensePlateRegion license_plate_region;
	private LicensePlateNumber license_plate_number;
	private LicensePlateCountry license_plate_country;
}