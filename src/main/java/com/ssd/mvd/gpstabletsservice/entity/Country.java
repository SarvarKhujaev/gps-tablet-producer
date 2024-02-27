package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import java.util.Optional;
import java.util.UUID;

public final class Country {
    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid ( final UUID uuid ) {
        this.uuid = uuid;
    }

    public String getFlag() {
        return this.flag;
    }

    public void setFlag ( final String flag ) {
        this.flag = flag;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol ( final String symbol ) {
        this.symbol = symbol;
    }

    public String getCountryNameEn() {
        return this.countryNameEn;
    }

    public void setCountryNameEn ( final String countryNameEn ) {
        this.countryNameEn = countryNameEn;
    }

    public String getCountryNameUz() {
        return this.countryNameUz;
    }

    public void setCountryNameUz ( final String countryNameUz ) {
        this.countryNameUz = countryNameUz;
    }

    public String getCountryNameRu() {
        return this.countryNameRu;
    }

    public void setCountryNameRu ( final String countryNameRu ) {
        this.countryNameRu = countryNameRu;
    }

    private UUID uuid;
	private String flag; // флаг страны
	private String symbol;
	private String countryNameEn;
	private String countryNameUz;
	private String countryNameRu;

    public Country ( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "uuid"   ) );
            this.setFlag( row.getString( "flag" ) );
            this.setSymbol( row.getString( "symbol" ) );
            this.setCountryNameEn( row.getString( "countryNameEN" ) );
            this.setCountryNameUz( row.getString( "countryNameUz" ) );
            this.setCountryNameRu( row.getString( "countryNameRu" ) );
        } );
    }
}
