package com.ssd.mvd.gpstabletsservice.entity.notifications;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import java.util.UUID;

@lombok.Data
@lombok.Builder
public final class SosNotification { // тспользуется для уведомления фронта
    private UUID patrulUUID;
    private Status status;
}
