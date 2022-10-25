package com.ssd.mvd.gpstabletsservice.request;

import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaskTimingRequest {
    private List< TaskTypes > taskType;
    private Date startDate;
    private Date endDate;
}
