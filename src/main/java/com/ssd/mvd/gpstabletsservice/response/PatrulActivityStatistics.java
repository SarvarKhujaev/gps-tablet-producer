package com.ssd.mvd.gpstabletsservice.response;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import java.util.List;

@lombok.Data
@lombok.Builder
public class PatrulActivityStatistics {
    private final List< Long > dateList;
    private Patrul patrul;
}
