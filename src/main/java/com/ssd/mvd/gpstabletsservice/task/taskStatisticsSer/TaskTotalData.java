package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.datastax.driver.core.Row;
import java.util.List;

@lombok.Data
public class TaskTotalData {
    private Long timeWastedToArrive;
    private Long totalTimeConsumption;
    private List< PositionInfo > positionInfoList;

    public TaskTotalData ( final Row row ) {
        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( row ) ) {
            this.setTimeWastedToArrive( row.getLong( "timewastedtoarrive" ) );
            this.setTotalTimeConsumption( row.getLong( "totaltimeconsumption" ) );
            this.setPositionInfoList( row.getList( "positionInfoList", PositionInfo.class ) ); } }
}