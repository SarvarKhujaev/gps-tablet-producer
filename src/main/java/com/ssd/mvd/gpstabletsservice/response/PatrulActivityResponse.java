package com.ssd.mvd.gpstabletsservice.response;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.PatrulDivisionByRegions;
import com.ssd.mvd.gpstabletsservice.inspectors.CollectionsInspector;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.List;

public final class PatrulActivityResponse extends CollectionsInspector {
    private Long count = 0L; // общее количество патрульных
    private final List< PatrulDivisionByRegions> regions;

    public PatrulActivityResponse (
            final SortedMap< Long, PatrulDivisionByRegions > regions
    ) {
        this.regions = new ArrayList<>( regions.values() );
        super.analyze(
                this.regions,
                patrulDivisionByRegions -> this.count += (
                        patrulDivisionByRegions.getActivePatruls()
                                + patrulDivisionByRegions.getNonActivePatruls()
                                + patrulDivisionByRegions.getNeverAuthorizedPatruls() )
        );
    }
}
