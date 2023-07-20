package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;

import java.util.ArrayList;
import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
// используется когда нужно найти патрульных рядом с камерой
// максимум 5 не занятых патрульных
public final class PatrulInRadiusList {
    private Double maxDistance;
    private final List< Patrul > freePatrulList = new ArrayList<>(); // максимум 5 не занятых патрульных
    private final List < Patrul > busyPatrulListInRadius = new ArrayList<>();
    private final List < Patrul > busyPatrulListOutOfRadius = new ArrayList<>();
    private final List < Patrul > freePatrulListOutOfRadius = new ArrayList<>(); // список патрульных которые не входят в радиус

    public PatrulInRadiusList ( final List< Patrul > patruls, final Boolean check ) {
        if ( check ) {
            for ( int i = 0; i < patruls.size() && freePatrulList.size() < 5; i++ )
                if ( patruls.get( i ).getTaskTypes().compareTo( TaskTypes.FREE ) == 0 ) freePatrulList.add( patruls.get( i ) );

            // чтобы не было дупликатов, убираем патрульных из топ 5 списка
            patruls.removeAll( freePatrulList );
            this.setMaxDistance( freePatrulList.get( 4 ).getDistance() );

            for ( final Patrul patrul : patruls ) {
                if ( patrul.getTaskTypes().compareTo( TaskTypes.FREE ) == 0 ) freePatrulListOutOfRadius.add( patrul );

                else { if ( DataValidateInspector
                        .getInstance()
                        .checkDistance
                        .test( maxDistance, patrul.getDistance() ) ) busyPatrulListInRadius.add( patrul );
                else busyPatrulListOutOfRadius.add( patrul ); } } }
        else patruls.forEach( patrul -> {
            // сохраняем патрульных которые никогда не авторизовавались в системе
            if ( patrul.getTokenForLogin().equals( "null" ) ) this.getFreePatrulListOutOfRadius().add( patrul );

            else { if ( TimeInspector
                        .getInspector()
                        .getGetTimeDifference()
                        .apply( patrul.getLastActiveDate().toInstant(), 1 ) <= 24 )
                    this.getBusyPatrulListInRadius().add( patrul ); // сохраняем патрульных которые были активны последние 24 часа

                // сохраняем патрульных которые были не активны больше 24 часов
                else this.getFreePatrulList().add( patrul ); } } ); }
}
