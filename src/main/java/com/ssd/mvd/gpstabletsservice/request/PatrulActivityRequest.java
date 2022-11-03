package com.ssd.mvd.gpstabletsservice.request;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatrulActivityRequest {
    private String patrulUUID;
    private Date startDate;
    private Date endDate;
}
