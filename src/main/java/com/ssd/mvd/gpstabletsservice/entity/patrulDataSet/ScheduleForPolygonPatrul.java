package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import java.util.List;
import java.util.UUID;

@lombok.Data
public class ScheduleForPolygonPatrul {
    private List< UUID > patrulUUIDs;
    private String uuid;
    private String from; //  время с которого начинается дежурство
    private String to;
}