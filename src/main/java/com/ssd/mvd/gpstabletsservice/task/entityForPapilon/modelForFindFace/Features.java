package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

import com.ssd.mvd.entity.modelForPassport.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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