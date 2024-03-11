package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.ssd.mvd.gpstabletsservice.entity.notifications.SosNotificationForAndroid;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.constants.Status;

import java.util.UUID;
import java.util.Date;

public final class SosTotalData extends DataValidateInspector {
    public void setUuid( final UUID uuid ) {
        this.uuid = uuid;
    }

    public void setPatrulUUID( final UUID patrulUUID ) {
        this.patrulUUID = patrulUUID;
    }

    public void setAddress( final String address ) {
        this.address = address;
    }

    public void setSosWasSendDate( final Date sosWasSendDate ) {
        this.sosWasSendDate = sosWasSendDate;
    }

    public void setSosWasClosed( final Date sosWasClosed ) {
        this.sosWasClosed = sosWasClosed;
    }

    public void setLatitude( final Double latitude ) {
        this.latitude = latitude;
    }

    public void setLongitude( final Double longitude ) {
        this.longitude = longitude;
    }

    public void setPatrulStatus( final Status patrulStatus ) {
        this.patrulStatus = patrulStatus;
    }

    public void setStatus( final Status status ) {
        this.status = status;
    }

    public void setSosNotificationForAndroid( final SosNotificationForAndroid sosNotificationForAndroid ) {
        this.sosNotificationForAndroid = sosNotificationForAndroid;
    }

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

    public SosTotalData (
            final PatrulSos patrulSos,
            final String patrulStatus,
            final SosNotificationForAndroid sosNotificationForAndroid
    ) {
        this.setUuid( patrulSos.getUuid() );
        this.setPatrulUUID( patrulSos.getPatrulUUID() );

        this.setAddress( patrulSos.getAddress() );
        this.setSosWasClosed( patrulSos.getSosWasClosed() );
        this.setSosWasSendDate( patrulSos.getSosWasSendDate() );

        this.setLatitude( patrulSos.getLatitude() );
        this.setLongitude( patrulSos.getLongitude() );

        this.setSosNotificationForAndroid( sosNotificationForAndroid );
        this.setPatrulStatus(
                super.objectIsNotNull( patrulStatus )
                ? Status.valueOf( patrulStatus )
                : Status.ATTACHED
        );
    }
}
