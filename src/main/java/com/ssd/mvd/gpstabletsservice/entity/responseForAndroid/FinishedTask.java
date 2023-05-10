package com.ssd.mvd.gpstabletsservice.entity.responseForAndroid;

import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;

@lombok.Data
@lombok.Builder
public class FinishedTask {
    private String task;
    private Long createdDate;
    private Long totalTimeConsumption; // показывает сколько времени Патрульный потратил на всю задачу от начала до конца

    private TaskTypes taskTypes;
    private CardDetails cardDetails;
    private ReportForCard reportForCard;
}
