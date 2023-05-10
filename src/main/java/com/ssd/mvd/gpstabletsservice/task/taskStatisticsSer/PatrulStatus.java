package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;

@lombok.Data
public class PatrulStatus {
    private Patrul patrul;
    private Boolean inTime; // показывает пришел ли Патрульный вовремя
    private Long totalTimeConsumption; // показывает сколько времени Патрульный потратил на всю задачу от начала до конца

    public PatrulStatus ( final Patrul patrul ) {
        this.setPatrul( patrul );
        this.setInTime( patrul.check() );
        this.setTotalTimeConsumption( TimeInspector
                .getInspector()
                .getGetTimeDifferenceInSeconds()
                .apply( patrul.getTaskDate().toInstant() ) ); }
}
