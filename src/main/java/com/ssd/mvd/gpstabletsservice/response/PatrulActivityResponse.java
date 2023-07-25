package com.ssd.mvd.gpstabletsservice.response;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.PatrulDivisionByRegions;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.List;

@lombok.Data
public final class PatrulActivityResponse {
    private Long count = 0L; // общее количество патрульных
    private final List< PatrulDivisionByRegions> regions;

    public PatrulActivityResponse ( final SortedMap< Long, PatrulDivisionByRegions > regions ) {
        this.regions = new ArrayList<>( regions.values() );
        this.regions.forEach( patrulDivisionByRegions -> this.count += (
                patrulDivisionByRegions.getActivePatruls()
                        + patrulDivisionByRegions.getNonActivePatruls()
                        + patrulDivisionByRegions.getNeverAuthorizedPatruls() ) ); }
}
