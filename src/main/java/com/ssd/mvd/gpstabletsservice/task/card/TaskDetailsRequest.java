package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.UUID;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailsRequest {
    private TaskTypes taskTypes;
    private UUID patrulUUID;
    private String id;
}
