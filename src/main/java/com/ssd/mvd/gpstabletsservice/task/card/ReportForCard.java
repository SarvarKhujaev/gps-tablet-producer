package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@JsonIgnoreProperties ( ignoreUnknown = true )
public class ReportForCard { // creates when some of Patrul from current Card has finished the work and has written the report about everything he has done
    private Double lan;
    private Double lat;

    private String title; // the name of Report
    private String description;

    private Date date; // the date when report was created
    private String passportSeries;
    @JsonDeserialize
    private List< String > imagesIds; // contains all images Ids which was downloaded in advance
}
