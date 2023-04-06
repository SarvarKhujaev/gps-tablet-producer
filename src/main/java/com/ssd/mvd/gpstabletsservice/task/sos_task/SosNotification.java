package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import java.util.UUID;

@lombok.Data
@lombok.Builder
public class SosNotification { // тспользуется для уведомления фронта
    private UUID patrulUUID;
    private Status status;
}
