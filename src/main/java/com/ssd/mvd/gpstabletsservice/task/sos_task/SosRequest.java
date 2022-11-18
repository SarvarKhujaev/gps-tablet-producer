package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
// используется патрульным для того чтобы подтвердить или отказаться от соса
public class SosRequest {
    private UUID patrulUUID;
    private UUID sosUUID;
    private Status status; // might be Accepted or Cancel either
}