package com.ssd.mvd.gpstracker.request;

import lombok.Data;

import java.util.Date;

@Data
public class RequestForStatistics {
    private Date startTime;
    private Date endTime;
}
