package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Country {
	private String symbol;
	private String countryNameEn;
	private String countryNameUz;
	private String countryNameRu;

    public Country ( Row row ) {
        this.setSymbol( row.getString( "symbol" ) );
        this.setCountryNameEn( row.getString( "countryNameEN" ) );
        this.setCountryNameUz( row.getString( "countryNameRu" ) );
        this.setCountryNameRu( row.getString( "countryNameUz" ) ); }
}
