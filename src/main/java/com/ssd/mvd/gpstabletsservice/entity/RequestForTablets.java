package com.ssd.mvd.gpstabletsservice.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Date;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestForTablets {
    private String patrulId;
    private Date startTime;
    private Date endTime;
}
