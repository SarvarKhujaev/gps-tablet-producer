package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Violation {
    private Long protocol_id;

    private String pinpp;
    private String decision;
    private String punishment;
    private String last_name_lat;
    private String violation_time;
    private String first_name_lat;
    private String protocol_series;
    private String second_name_lat;
    private String protocol_number;
    private String adm_case_number;
    private String adm_case_series;
    private String resolution_time;
    private String violation_article;
}
