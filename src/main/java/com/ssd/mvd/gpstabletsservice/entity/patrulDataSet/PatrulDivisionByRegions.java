package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.entity.RegionData;
import com.datastax.driver.core.Row;

public final class PatrulDivisionByRegions extends DataValidateInspector {
    public Long getActivePatruls() {
        return activePatruls;
    }

    public void setActivePatruls( final Long activePatruls ) {
        this.activePatruls = activePatruls;
    }

    public Long getNonActivePatruls() {
        return nonActivePatruls;
    }

    public void setNonActivePatruls( final Long nonActivePatruls ) {
        this.nonActivePatruls = nonActivePatruls;
    }

    public Long getNeverAuthorizedPatruls() {
        return neverAuthorizedPatruls;
    }

    public void setNeverAuthorizedPatruls( final Long neverAuthorizedPatruls ) {
        this.neverAuthorizedPatruls = neverAuthorizedPatruls;
    }

    private Long activePatruls;
    private Long nonActivePatruls;
    private Long neverAuthorizedPatruls;

    private final Long regionId;
    private final String regionName;

    public PatrulDivisionByRegions ( final RegionData regionData ) {
        this.regionName = regionData.getName();
        this.regionId = regionData.getId();

        this.setNeverAuthorizedPatruls( 0L );
        this.setNonActivePatruls( 0L );
        this.setActivePatruls( 0L );
    }

    public PatrulDivisionByRegions save ( final Row row ) {
        // сохраняем патрульных которые никогда не авторизовавались в системе
        if ( super.checkPatrulActivity( row.getUUID( "uuid" ) ) ) {
            this.neverAuthorizedPatruls++;
        }

        else {
            // сохраняем патрульных которые были активны последние 24 часа
            if ( super.getTimeDifference( row.getTimestamp( "lastActiveDate" ).toInstant(), 1 ) <= 24 ) {
                this.activePatruls++;
            }

            // сохраняем патрульных которые были не активны больше 24 часов
            else {
                this.nonActivePatruls++;
            }
        }
        return this;
    }
}
