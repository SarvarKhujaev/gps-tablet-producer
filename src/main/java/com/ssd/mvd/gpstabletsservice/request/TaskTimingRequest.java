package com.ssd.mvd.gpstabletsservice.request;

import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import java.util.Date;
import java.util.List;

@lombok.Data
@lombok.Builder
public class TaskTimingRequest {
    private List< TaskTypes > taskType;
    private Date startDate;
    private Date endDate;
}
