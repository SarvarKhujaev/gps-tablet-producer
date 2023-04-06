package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import java.util.UUID;
import java.util.Date;

@lombok.Data
public class SosTotalData {
    private UUID uuid;
    private UUID patrulUUID;

    private String address;

    private Date sosWasSendDate; // созраняет время когда запрос был отправлен
    private Date sosWasClosed; // время когда сос был отменен

    private Double latitude;
    private Double longitude;

    private Status patrulStatus;
    private Status status = Status.CREATED;

    private SosNotificationForAndroid sosNotificationForAndroid;

    public SosTotalData ( PatrulSos patrulSos,
                          String patrulStatus,
                          SosNotificationForAndroid sosNotificationForAndroid ) {
        this.setUuid( patrulSos.getUuid() );
        this.setPatrulUUID( patrulSos.getPatrulUUID() );

        this.setAddress( patrulSos.getAddress() );
        this.setSosWasClosed( patrulSos.getSosWasClosed() );
        this.setSosWasSendDate( patrulSos.getSosWasSendDate() );

        this.setLatitude( patrulSos.getLatitude() );
        this.setLongitude( patrulSos.getLongitude() );

        this.setStatus( Status.CREATED );
        this.setSosNotificationForAndroid( sosNotificationForAndroid );
        this.setPatrulStatus( patrulStatus != null ? Status.valueOf( patrulStatus ) : Status.ATTACHED ); }
}
