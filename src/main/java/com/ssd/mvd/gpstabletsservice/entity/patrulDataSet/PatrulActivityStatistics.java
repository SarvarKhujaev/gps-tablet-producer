package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import java.util.List;

@lombok.Builder
public class PatrulActivityStatistics {
    private final List< Long > dateList;
    private Patrul patrul;
}
