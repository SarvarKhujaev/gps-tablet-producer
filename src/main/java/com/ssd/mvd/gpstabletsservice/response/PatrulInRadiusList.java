package com.ssd.mvd.gpstabletsservice.response;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import java.util.ArrayList;
import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
// используется когда нужно найти патрульных рядом с камерой
// максимум 5 не занятых патрульных
public class PatrulInRadiusList {
    private Double maxDistance;
    private final List< Patrul > freePatrulList = new ArrayList<>(); // максимум 5 не занятых патрульных
    private final List < Patrul > busyPatrulListInRadius = new ArrayList<>();
    private final List < Patrul > busyPatrulListOutOfRadius = new ArrayList<>();
    private final List < Patrul > freePatrulListOutOfRadius = new ArrayList<>(); // список патрульных которые не входят в радиус

    public PatrulInRadiusList ( final List< Patrul > patruls ) {
        for ( int i = 0; i < patruls.size() && freePatrulList.size() < 5; i++ )
            if ( patruls.get( i ).getTaskTypes().compareTo( TaskTypes.FREE ) == 0 ) freePatrulList.add( patruls.get( i ) );

        // чтобы не было дупликатов, убираем патрульных из топ 5 списка
        patruls.removeAll( freePatrulList );
        this.setMaxDistance( freePatrulList.get( 4 ).getDistance() );

        for ( Patrul patrul : patruls ) {
            if ( patrul.getTaskTypes().compareTo( TaskTypes.FREE ) == 0 ) freePatrulListOutOfRadius.add( patrul );

            else {
                if ( DataValidateInspector
                        .getInstance()
                        .getCheckDistance()
                        .apply( maxDistance, patrul.getDistance() ) ) busyPatrulListInRadius.add( patrul );
                else busyPatrulListOutOfRadius.add( patrul ); } } }
}
