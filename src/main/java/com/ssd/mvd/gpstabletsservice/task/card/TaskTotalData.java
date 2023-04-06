package com.ssd.mvd.gpstabletsservice.task.card;

import com.datastax.driver.core.Row;
import java.util.List;

@lombok.Data
public class TaskTotalData {
    private Long timeWastedToArrive;
    private Long totalTimeConsumption;
    private List< PositionInfo > positionInfoList;

    public TaskTotalData ( Row row ) {
        if ( row != null ) {
            this.setTimeWastedToArrive( row.getLong( "timewastedtoarrive" ) );
            this.setTotalTimeConsumption( row.getLong( "totaltimeconsumption" ) );
            this.setPositionInfoList( row.getList( "positionInfoList", PositionInfo.class ) ); } }
}