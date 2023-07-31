package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.datastax.driver.core.UDTValue;

import java.util.List;
import java.util.Date;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties ( ignoreUnknown = true )
public final class ReportForCard { // creates when some of Patrul from current Card has finished the work and has written the report about everything he has done
    private Double lan;
    private Double lat;

    private String title; // the name of Report
    private String description;
    private String passportSeries;

    private Date date; // the date when report was created
    private UUID uuidOfPatrul;
    @JsonDeserialize
    private List< String > imagesIds; // contains all images Ids which was downloaded in advance

    public ReportForCard ( final UDTValue value ) {
        this.setLan( value.getDouble( "lan" ) );
        this.setLat( value.getDouble( "lat" ) );

        this.setTitle( value.getString( "title" ) );
        this.setDescription( value.getString( "description" ) );
        this.setPassportSeries( value.getString( "passportSeries" ) );

        this.setDate( value.getTimestamp( "date" ) );
        this.setImagesIds( value.getList( "imagesIds", String.class ) ); }
}
