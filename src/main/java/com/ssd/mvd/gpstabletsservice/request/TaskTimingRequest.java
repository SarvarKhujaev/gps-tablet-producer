package com.ssd.mvd.gpstabletsservice.request;

import java.util.Date;

import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskTimingRequest {
    private TaskTypes taskType;
    private Date startDate;
    private Date endDate;
}
