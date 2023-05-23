package com.ssd.mvd.gpstabletsservice.entity.notifications;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.task.sos_task.PatrulSos;
import com.ssd.mvd.gpstabletsservice.constants.Status;

@lombok.Data
public class SosNotificationForAndroid {
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

    public SosNotificationForAndroid ( final PatrulSos patrulSos,
                                      final Patrul patrul,
                                      final Status status,
                                      final String patrulPassportSeries ) {
        this.setStatus( status );
        this.setAddress( patrulSos.getAddress() );
        this.setLatitude( patrulSos.getLatitude() );
        this.setLongitude( patrulSos.getLongitude() );

        this.setRank( patrul.getRank() );
        this.setRegionId( patrul.getRegionId() );
        this.setMahallaId( patrul.getMahallaId() );
        this.setDistrictId( patrul.getDistrictId() );
        this.setPhoneNumber( patrul.getPhoneNumber() );
        this.setDateOfBirth( patrul.getDateOfBirth() );
        this.setPatrulPassportSeries( patrulPassportSeries );
        this.setPatrulImageLink( patrul.getPatrulImageLink() );
        this.setSurnameNameFatherName( patrul.getSurnameNameFatherName() ); }
}