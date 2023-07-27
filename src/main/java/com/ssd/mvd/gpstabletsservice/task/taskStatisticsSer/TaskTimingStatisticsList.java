package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import java.util.ArrayList;

@lombok.Data
public final class TaskTimingStatisticsList {
    private final ArrayList< TaskTimingStatistics > listLate = new ArrayList<>();
    private final ArrayList< TaskTimingStatistics > listInTime = new ArrayList<>();
    private final ArrayList< TaskTimingStatistics > listDidNotArrived = new ArrayList<>();
}
