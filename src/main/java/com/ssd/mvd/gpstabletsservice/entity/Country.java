package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.UUID;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Country {
    private UUID uuid;
	private String flag; // флаг страны
	private String symbol;
	private String countryNameEn;
	private String countryNameUz;
	private String countryNameRu;

    public Country ( Row row ) {
        this.setUuid( row.getUUID( "uuid"   ) );
        this.setFlag( row.getString( "flag" ) );
        this.setSymbol( row.getString( "symbol" ) );
        this.setCountryNameEn( row.getString( "countryNameEN" ) );
        this.setCountryNameUz( row.getString( "countryNameUz" ) );
        this.setCountryNameRu( row.getString( "countryNameRu" ) ); }
}
