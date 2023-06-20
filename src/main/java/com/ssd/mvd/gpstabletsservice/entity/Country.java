package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import java.util.Optional;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class Country {
    private UUID uuid;
	private String flag; // флаг страны
	private String symbol;
	private String countryNameEn;
	private String countryNameUz;
	private String countryNameRu;

    public Country ( final Row row ) { Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "uuid"   ) );
            this.setFlag( row.getString( "flag" ) );
            this.setSymbol( row.getString( "symbol" ) );
            this.setCountryNameEn( row.getString( "countryNameEN" ) );
            this.setCountryNameUz( row.getString( "countryNameUz" ) );
            this.setCountryNameRu( row.getString( "countryNameRu" ) ); } ); }
}
