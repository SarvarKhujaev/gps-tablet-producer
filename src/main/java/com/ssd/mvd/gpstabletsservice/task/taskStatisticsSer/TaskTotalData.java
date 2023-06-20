package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.datastax.driver.core.Row;
import java.util.Optional;
import java.util.List;

@lombok.Data
public final class TaskTotalData {
    private Long timeWastedToArrive;
    private Long totalTimeConsumption;
    private List< PositionInfo > positionInfoList;

    public TaskTotalData ( final Row row ) { Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setTimeWastedToArrive( row.getLong( "timewastedtoarrive" ) );
            this.setTotalTimeConsumption( row.getLong( "totaltimeconsumption" ) );
            this.setPositionInfoList( row.getList( "positionInfoList", PositionInfo.class ) ); } ); }
}