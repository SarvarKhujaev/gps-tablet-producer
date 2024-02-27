package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;

/*
хранит данные о времени которое патрульный потратил на достижение
локации задания
*/
public final class PatrulTimeConsumedToArriveToTaskLocation extends TimeInspector {
    public Patrul getPatrul() {
        return this.patrul;
    }

    public void setPatrul( final Patrul patrul ) {
        this.patrul = patrul;
    }

    public Boolean getInTime() {
        return this.inTime;
    }

    public void setInTime( final Boolean inTime ) {
        this.inTime = inTime;
    }

    public Long getTotalTimeConsumption() {
        return this.totalTimeConsumption;
    }

    public void setTotalTimeConsumption( final Long totalTimeConsumption ) {
        this.totalTimeConsumption = totalTimeConsumption;
    }

    private Patrul patrul;
    /*
    показывает пришел ли Патрульный вовремя
     */
    private Boolean inTime;
    /*
    показывает сколько времени Патрульный потратил на всю задачу от начала до конца
     */
    private Long totalTimeConsumption;

    public static PatrulTimeConsumedToArriveToTaskLocation generate (
            final Patrul patrul
    ) {
        return new PatrulTimeConsumedToArriveToTaskLocation( patrul );
    }

    private PatrulTimeConsumedToArriveToTaskLocation(
            final Patrul patrul
    ) {
        this.setPatrul( patrul );
        this.setInTime( patrul.check() );
        this.setTotalTimeConsumption(
                super.getTimeDifference(
                        patrul
                        .getPatrulDateData()
                        .getTaskDate()
                        .toInstant(), 0 ) );
    }
}
