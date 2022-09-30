package com.ssd.mvd.gpstabletsservice.response;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatrulActivityStatistics {
    private final List< Long > dateList;
    private Patrul patrul;
}
