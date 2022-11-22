package com.ssd.mvd.gpstabletsservice.task.sos_task;

import lombok.Data;

@Data
public class SosTotalData {
    private PatrulSos patrulSos;

    private SosNotificationForAndroid sosNotificationForAndroid;

    public SosTotalData ( PatrulSos patrulSos, SosNotificationForAndroid sosNotificationForAndroid ) {
        this.setSosNotificationForAndroid( sosNotificationForAndroid );
        this.setPatrulSos( patrulSos ); }
}
