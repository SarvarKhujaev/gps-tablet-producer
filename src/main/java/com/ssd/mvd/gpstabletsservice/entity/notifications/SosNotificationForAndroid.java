package com.ssd.mvd.gpstabletsservice.entity.notifications;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.task.sos_task.PatrulSos;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.datastax.driver.core.Row;

/*
когда патрульный отправляет сигнал СОС
то используем этот объект для отправки уведомлений
всем ближайшим патрульным
*/
public final class SosNotificationForAndroid {
    public Status getStatus() {
        return this.status;
    }

    public void setStatus ( final Status status ) {
        this.status = status;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude ( final Double latitude ) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude ( final Double longitude ) {
        this.longitude = longitude;
    }

    public void setRegionId ( final Long regionId ) {
        this.regionId = regionId;
    }

    public void setMahallaId ( final Long mahallaId ) {
        this.mahallaId = mahallaId;
    }

    public void setDistrictId ( final Long districtId ) {
        this.districtId = districtId;
    }

    public void setRank ( final String rank ) {
        this.rank = rank;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress ( final String address ) {
        this.address = address;
    }

    public void setDateOfBirth ( final String dateOfBirth ) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setPhoneNumber ( final String phoneNumber ) {
        this.phoneNumber = phoneNumber;
    }

    public void setPatrulImageLink ( final String patrulImageLink ) {
        this.patrulImageLink = patrulImageLink;
    }

    public void setPatrulPassportSeries ( final String patrulPassportSeries ) {
        this.patrulPassportSeries = patrulPassportSeries;
    }

    public void setSurnameNameFatherName ( final String surnameNameFatherName ) {
        this.surnameNameFatherName = surnameNameFatherName;
    }

    private Status status;
    private Double latitude;
    private Double longitude;

    private Long regionId;
    private Long mahallaId;
    private Long districtId; // choosing from dictionary

    private String rank;
    private String address;
    private String dateOfBirth;
    private String phoneNumber;
    private String patrulImageLink;
    private String patrulPassportSeries;
    private String surnameNameFatherName; // Ф.И.О

    private void save ( final Row row ) {
        this.setRank( row.getString( "rank" ) );
        this.setDateOfBirth( row.getString( "dateOfBirth" ) );
        this.setPatrulImageLink( row.getString( "patrulImageLink" ) );
        this.setPatrulPassportSeries( row.getString( "passportNumber" ) );

        this.setRegionId( row.getUDTValue( "patrulRegionData" ).getLong( "regionId" ) );
        this.setMahallaId( row.getUDTValue( "patrulRegionData" ).getLong( "regionId" ) );
        this.setDistrictId( row.getUDTValue( "patrulRegionData" ).getLong( "regionId" ) );

        this.setPhoneNumber( row.getUDTValue( "patrulMobileAppInfo" ).getString( "phoneNumber" ) );
        this.setSurnameNameFatherName( row.getUDTValue( "patrulFIOData" ).getString( "surnameNameFatherName" ) );
    }

    private void save ( final Patrul patrul ) {
        this.setRank( patrul.getRank() );
        this.setDateOfBirth( patrul.getDateOfBirth() );
        this.setPatrulImageLink( patrul.getPatrulImageLink() );
        this.setPatrulPassportSeries( patrul.getPassportNumber() );
        this.setRegionId( patrul.getPatrulRegionData().getRegionId() );
        this.setMahallaId( patrul.getPatrulRegionData().getMahallaId() );
        this.setDistrictId( patrul.getPatrulRegionData().getDistrictId() );
        this.setPhoneNumber( patrul.getPatrulMobileAppInfo().getPhoneNumber() );
        this.setSurnameNameFatherName( patrul.getPatrulFIOData().getSurnameNameFatherName() );
    }

    private void save ( final PatrulSos patrulSos ) {
        this.setAddress( patrulSos.getAddress() );
        this.setLatitude( patrulSos.getLatitude() );
        this.setLongitude( patrulSos.getLongitude() );
    }

    public SosNotificationForAndroid (
            final PatrulSos patrulSos,
            final Patrul patrul,
            final Status status ) {
        this.setStatus( status );
        this.save( patrulSos );
        this.save( patrul );
    }

    public SosNotificationForAndroid (
            final PatrulSos patrulSos,
            final Row row ) {
        this.setStatus( Status.CREATED );
        this.save( patrulSos );
        this.save( row );
    }
}
