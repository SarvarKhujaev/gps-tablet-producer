package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.CardDetails;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FinishedTask {
    private String task;
    private String createdDate;
    private Long totalTimeConsumption; // показывает сколько времени Патрульный потратил на всю задачу от начала до конца

    private TaskTypes taskTypes;
    private CardDetails cardDetails;
    private ReportForCard reportForCard;
}
