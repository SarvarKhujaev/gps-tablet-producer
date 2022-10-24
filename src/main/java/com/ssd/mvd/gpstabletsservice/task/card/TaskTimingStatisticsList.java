package com.ssd.mvd.gpstabletsservice.task.card;

import java.util.ArrayList;
import lombok.Data;

@Data
public class TaskTimingStatisticsList {
    private final ArrayList< TaskTimingStatistics > listLate = new ArrayList<>();
    private final ArrayList< TaskTimingStatistics > listInTime = new ArrayList<>();
    private final ArrayList< TaskTimingStatistics > listDidNotArrived = new ArrayList<>();
}
