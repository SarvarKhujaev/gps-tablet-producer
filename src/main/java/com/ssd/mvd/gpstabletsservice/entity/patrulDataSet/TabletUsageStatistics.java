package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import java.time.Month;
import java.util.*;

public final class TabletUsageStatistics extends TimeInspector {
    public Long getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount( final Long totalCount ) {
        this.totalCount = totalCount;
    }

    public SortedMap< Month, Long > getTabletUsageStatisticsForYear() {
        return this.tabletUsageStatisticsForYear;
    }

    public SortedMap< Date, Long > getTabletUsageStatisticsForEachDay() {
        return this.tabletUsageStatisticsForEachDay;
    }

    private Long totalCount = 0L;
    // хранит данные о том, сколько часов патрульный использовал в каждом месяце
    private final SortedMap< Month, Long > tabletUsageStatisticsForYear = newTreeMap();
    // хранит данные о том, сколько часов патрульный использовал каждый день в течении месяца
    private final SortedMap< Date, Long > tabletUsageStatisticsForEachDay = newTreeMap();

    /*
    заполняем Map названиями всех месяцев года
    */
    public void setMap () {
        Arrays.stream( Month.values() )
            .forEach( month -> this.getTabletUsageStatisticsForYear().put( month, 0L ) );
    }

    public TabletUsageStatistics update (
            final Date date,
            final Long usage,
            final Boolean flag ) {
        if ( flag ) {
            final Month month = super.getMonthName( date );

            this.getTabletUsageStatisticsForYear().put(
                    month,
                    this.getTabletUsageStatisticsForYear().get( month ) + usage );
        }

        else {
            this.getTabletUsageStatisticsForEachDay().put( date, usage );
        }

        this.setTotalCount( this.getTotalCount() + usage );
        return this;
    }
}
