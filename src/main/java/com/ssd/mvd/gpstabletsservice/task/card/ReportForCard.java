package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.datastax.driver.core.UDTValue;

import java.util.List;
import java.util.Date;
import java.util.UUID;

@JsonIgnoreProperties ( ignoreUnknown = true )
/*
Рапорт от патрульного после завершения задачи
*/
public final class ReportForCard {
    public Double getLan() {
        return this.lan;
    }

    public void setLan ( final Double lan ) {
        this.lan = lan;
    }

    public Double getLat() {
        return this.lat;
    }

    public void setLat ( final Double lat ) {
        this.lat = lat;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle ( final String title ) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription ( final String description ) {
        this.description = description;
    }

    public String getPassportSeries() {
        return this.passportSeries;
    }

    public void setPassportSeries ( final String passportSeries ) {
        this.passportSeries = passportSeries;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate ( final Date date ) {
        this.date = date;
    }

    public UUID getUuidOfPatrul() {
        return this.uuidOfPatrul;
    }

    public List< String > getImagesIds() {
        return this.imagesIds;
    }

    public void setImagesIds ( final List< String> imagesIds) {
        this.imagesIds = imagesIds;
    }

    // локация патрульного откуда он отправил рапорт
    private Double lan;
    private Double lat;

    // оглавление рапорта
    private String title;
    // полное описание выполненной работы
    private String description;
    // номер паспорта патрульного
    private String passportSeries;

    // дата когда рапорт был создан
    private Date date;
    private UUID uuidOfPatrul;

    @JsonDeserialize
    // хранит список из фото которые сделал патрульный
    private List< String > imagesIds;

    public ReportForCard ( final UDTValue udtValue ) {
        this.setLan( udtValue.getDouble( "lan" ) );
        this.setLat( udtValue.getDouble( "lat" ) );

        this.setTitle( udtValue.getString( "title" ) );
        this.setDescription( udtValue.getString( "description" ) );
        this.setPassportSeries( udtValue.getString( "passportSeries" ) );

        this.setDate( udtValue.getTimestamp( "date" ) );
        this.setImagesIds( udtValue.getList( "imagesIds", String.class ) );
    }
}
