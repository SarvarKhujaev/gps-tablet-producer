package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SosNotification { // тспользуется для уведомления фронта
    private UUID patrulUUID;
    private Status status;
}
