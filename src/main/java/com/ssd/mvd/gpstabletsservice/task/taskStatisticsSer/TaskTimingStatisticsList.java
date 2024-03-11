package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.ssd.mvd.gpstabletsservice.inspectors.CollectionsInspector;
import java.util.ArrayList;

public final class TaskTimingStatisticsList extends CollectionsInspector {
    private final ArrayList< TaskTimingStatistics > listLate;
    private final ArrayList< TaskTimingStatistics > listInTime;
    private final ArrayList< TaskTimingStatistics > listDidNotArrived;

    public static TaskTimingStatisticsList empty() {
        return new TaskTimingStatisticsList();
    }

    private TaskTimingStatisticsList () {
        this.listDidNotArrived = super.newList();
        this.listInTime = super.newList();
        this.listLate = super.newList();

        super.close();
    }
}
