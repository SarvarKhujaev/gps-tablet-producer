package com.ssd.mvd.gpstabletsservice.request;

import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class TaskDetailsRequest {
    private TaskTypes taskTypes;
    private UUID patrulUUID;
    private String id;
}
