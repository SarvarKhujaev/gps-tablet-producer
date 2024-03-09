package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

public final class PatrulRegionData extends DataValidateInspector implements ObjectCommonMethods< PatrulRegionData > {
    public long getRegionId() {
        return this.regionId;
    }

    public void setRegionId( final long regionId ) {
        this.regionId = regionId;
    }

    public long getMahallaId() {
        return this.mahallaId;
    }

    public void setMahallaId( final long mahallaId ) {
        this.mahallaId = mahallaId;
    }

    public long getDistrictId() {
        return this.districtId;
    }

    public void setDistrictId( final long districtId ) {
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

    public static PatrulRegionData empty () {
        return new PatrulRegionData();
    }

    private PatrulRegionData () {}

    private PatrulRegionData ( final Row row ) {
        super.checkAndSetParams(
                row,
                row1 -> {
                    this.setRegionId( row.getLong( "regionId" ) );
                    this.setMahallaId( row.getLong( "mahallaId" ) );
                    this.setDistrictId( row.getLong( "districtId" ) );

                    this.setRegionName( row.getString( "regionName" ) );
                    this.setDistrictName( row.getString( "districtName" ) );
                }
        );
    }

    private PatrulRegionData( final UDTValue udtValue ) {
        super.checkAndSetParams(
                udtValue,
                udtValue1 -> {
                    this.setRegionId( udtValue.getLong( "regionId" ) );
                    this.setMahallaId( udtValue.getLong( "mahallaId" ) );
                    this.setDistrictId( udtValue.getLong( "districtId" ) );

                    this.setRegionName( udtValue.getString( "regionName" ) );
                    this.setDistrictName( udtValue.getString( "districtName" ) );
                }
        );
    }

    @Override
    public PatrulRegionData generate( final UDTValue udtValue ) {
        return new PatrulRegionData( udtValue );
    }

    @Override
    public PatrulRegionData generate( final Row row ) {
        return new PatrulRegionData( row );
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setLong( "regionId", this.getRegionId() )
                .setLong( "mahallaId", this.getMahallaId() )
                .setLong( "districtId", this.getDistrictId() )
                .setString( "regionName", this.getRegionName() )
                .setString( "districtName", this.getDistrictName() );
    }
}
