package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests;

import java.util.Date;

@lombok.Data
@lombok.Builder
public class PatrulActivityRequest {
    private String patrulUUID;
    private Date startDate;
    private Date endDate;
}
