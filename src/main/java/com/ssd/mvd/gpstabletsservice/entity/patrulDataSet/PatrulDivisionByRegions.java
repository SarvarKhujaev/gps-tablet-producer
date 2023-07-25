package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.ssd.mvd.gpstabletsservice.entity.RegionData;
import com.datastax.driver.core.Row;

@lombok.Data
public final class PatrulDivisionByRegions {
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
        this.setActivePatruls( 0L ); }

    public PatrulDivisionByRegions save ( final Row row ) {
        // сохраняем патрульных которые никогда не авторизовавались в системе
        if ( row.getString( "tokenForLogin" ).equals( "null" ) ) this.neverAuthorizedPatruls++;
        else {
            // сохраняем патрульных которые были активны последние 24 часа
            if ( TimeInspector
                .getInspector()
                .getGetTimeDifference()
                .apply( row.getTimestamp( "lastActiveDate" ).toInstant(), 1 ) <= 24 ) this.activePatruls++;

            // сохраняем патрульных которые были не активны больше 24 часов
            else this.nonActivePatruls++; }
        return this; }
}
