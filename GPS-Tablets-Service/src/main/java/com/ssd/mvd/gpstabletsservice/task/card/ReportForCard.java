package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class ReportForCard { // creates when some of Patrul from current Card has finished the work and has written the report about everything he has done
    private Double lan;
    private Double lat;

    private String title; // the name of Report
    private String cardType; // from 102, SelfEmployment or Camera
    private String description;

    private Date date; // the date when report was created
    private UUID cardId;
    private String passportSeries;
    private List< String > imagesIds; // contains all images Ids which was downloaded in advance
}
