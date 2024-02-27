package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

public final class PatrulRegionData {
    public Long getRegionId() {
        return this.regionId;
    }

    public void setRegionId( final Long regionId ) {
        this.regionId = regionId;
    }

    public Long getMahallaId() {
        return this.mahallaId;
    }

    public void setMahallaId( final Long mahallaId ) {
        this.mahallaId = mahallaId;
    }

    public Long getDistrictId() {
        return this.districtId;
    }

    public void setDistrictId( final Long districtId ) {
        this.districtId = districtId;
    }

    public String getRegionName() {
        return this.regionName;
    }

    public void setRegionName( final String regionName ) {
        this.regionName = regionName;
    }

    public String getDistrictName() {
        return this.districtName;
    }

    public void setDistrictName( final String districtName ) {
        this.districtName = districtName;
    }

    private long regionId;
    private long mahallaId;
    private long districtId; // choosing from dictionary

    private String regionName;
    private String districtName;

    public static <T> PatrulRegionData generate ( final T object ) {
        return object instanceof Row
                ? new PatrulRegionData( (Row) object )
                : new PatrulRegionData( (UDTValue) object );
    }

    private PatrulRegionData ( final Row row ) {
        this.setRegionId( row.getLong( "regionId" ) );
        this.setMahallaId( row.getLong( "mahallaId" ) );
        this.setDistrictId( row.getLong( "districtId" ) );

        this.setRegionName( row.getString( "regionName" ) );
        this.setDistrictName( row.getString( "districtName" ) );
    }

    private PatrulRegionData( final UDTValue udtValue ) {
        this.setRegionId( udtValue.getLong( "regionId" ) );
        this.setMahallaId( udtValue.getLong( "mahallaId" ) );
        this.setDistrictId( udtValue.getLong( "districtId" ) );

        this.setRegionName( udtValue.getString( "regionName" ) );
        this.setDistrictName( udtValue.getString( "districtName" ) );
    }
}
