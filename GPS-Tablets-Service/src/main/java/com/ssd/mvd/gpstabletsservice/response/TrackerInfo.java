package com.ssd.mvd.gpstabletsservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackerInfo {
    private String topicName;
    private Boolean status;
}
