package com.ssd.mvd.gpstabletsservice.entity;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class ScheduleForPolygonPatrul {
    private List< UUID > patrulUUIDs;
    private String uuid;
    private String from; //  время с которого начинается дежурство
    private String to;
}