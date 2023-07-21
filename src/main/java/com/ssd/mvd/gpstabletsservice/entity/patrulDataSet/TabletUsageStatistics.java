package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import java.time.Month;
import java.util.*;

@lombok.Data
public final class TabletUsageStatistics {
    // хранит данные о том, сколько часов патрульный использовал в каждом месяце
    private final SortedMap< Month, Long > tabletUsageStatisticsForYear = new TreeMap<>();
    // хранит данные о том, сколько часов патрульный использовал каждый день в течении месяца
    private final SortedMap< Date, Long > tabletUsageStatisticsForEachDay = new TreeMap<>();

    public void setMap () { Arrays.stream( Month.values() )
            .parallel()
            .forEach( month -> this.getTabletUsageStatisticsForYear().put( month, 0L ) ); }

    public TabletUsageStatistics update ( final Date date, final Long usage, final Boolean flag ) {
        if ( flag ) {
            final Month month = TimeInspector
                    .getInspector()
                    .getGetMonthName()
                    .apply( date );
            this.getTabletUsageStatisticsForYear().put( month, this.getTabletUsageStatisticsForYear().get( month ) + usage ); }
        else this.getTabletUsageStatisticsForEachDay().put( date, usage );
        return this; }
}
