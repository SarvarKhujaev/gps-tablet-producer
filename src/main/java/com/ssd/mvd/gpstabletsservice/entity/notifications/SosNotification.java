package com.ssd.mvd.gpstabletsservice.entity.notifications;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import java.util.UUID;

@lombok.Builder
// используется для уведомления фронта
public final class SosNotification {
    public UUID getPatrulUUID() {
        return patrulUUID;
    }

    private UUID patrulUUID;
    private Status status;
}
