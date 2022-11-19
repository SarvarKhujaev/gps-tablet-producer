package com.ssd.mvd.gpstabletsservice.response;

import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.function.BiFunction;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
// используется когда нужно найти патрульных рядом с камерой
// максимум 5 не занятых патрульных
// SAM - 1011
public class PatrulInRadiusList {
    private Double maxDistance;
    private List< Patrul > freePatrulList = new ArrayList<>(); // максимум 5 не занятых патрульных

    private List < Patrul > busyPatrulListInRadius = new ArrayList<>();
    private List < Patrul > busyPatrulListOutOfRadius = new ArrayList<>();

    private List < Patrul > freePatrulListInRadius = new ArrayList<>(); // список патрульных которые не входят в радиус
    private List < Patrul > freePatrulListOutOfRadius = new ArrayList<>(); // список патрульных которые не входят в радиус

    private final BiFunction< Double, Double, Boolean > checkDistance = ( distance, patrulDistance ) -> patrulDistance <= distance;

    public PatrulInRadiusList( List< Patrul > patruls ) {
        for ( int i = 0; i < patruls.size() && freePatrulList.size() < 5; i++ )
            if ( patruls.get( i ).getTaskTypes().compareTo( TaskTypes.FREE ) == 0 ) freePatrulList.add( patruls.get( i ) );

        // чтобы не было дупликатов, убираем патрульных из топ 5 списка
        patruls.removeAll( freePatrulList );
        this.setMaxDistance( freePatrulList.get( 4 ).getDistance() );

        for ( Patrul patrul : patruls ) {
            if ( patrul.getTaskTypes().compareTo( TaskTypes.FREE ) == 0 ) {
                if ( this.checkDistance.apply( maxDistance, patrul.getDistance() ) ) freePatrulListInRadius.add( patrul );
                else freePatrulListOutOfRadius.add( patrul ); }

            else {
                if ( this.checkDistance.apply( maxDistance, patrul.getDistance() ) ) busyPatrulListInRadius.add( patrul );
                else busyPatrulListOutOfRadius.add( patrul ); } } }
}
