package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.datastax.driver.core.Row;
import java.util.Optional;
import java.util.List;

public final class TaskTotalData {
    public void setTimeWastedToArrive ( final long timeWastedToArrive ) {
        this.timeWastedToArrive = timeWastedToArrive;
    }

    public void setTotalTimeConsumption ( final long totalTimeConsumption ) {
        this.totalTimeConsumption = totalTimeConsumption;
    }

    public void setPositionInfoList ( final List< PositionInfo > positionInfoList ) {
        this.positionInfoList = positionInfoList;
    }

    public long getTimeWastedToArrive() {
        return this.timeWastedToArrive;
    }

    public long getTotalTimeConsumption() {
        return this.totalTimeConsumption;
    }

    public List< PositionInfo > getPositionInfoList() {
        return this.positionInfoList;
    }

    private long timeWastedToArrive;
    private long totalTimeConsumption;
    private List< PositionInfo > positionInfoList;

    public TaskTotalData ( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setTimeWastedToArrive( row.getLong( "timeWastedToArrive" ) );
            this.setTotalTimeConsumption( row.getLong( "totalTimeConsumption" ) );
            this.setPositionInfoList( row.getList( "positionInfoList", PositionInfo.class ) );
        } );
    }
}