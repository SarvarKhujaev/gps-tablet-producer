package com.ssd.mvd.gpstabletsservice.request;

import lombok.Data;

import java.util.Date;

@Data
public class RequestForStatistics {
    private Date startTime;
    private Date endTime;
}
