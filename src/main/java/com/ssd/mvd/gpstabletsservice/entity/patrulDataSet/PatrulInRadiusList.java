package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.CollectionsInspector;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import java.util.List;

// используется когда нужно найти патрульных рядом с камерой
// максимум 5 не занятых патрульных
public final class PatrulInRadiusList extends CollectionsInspector {
    public void setMaxDistance(Double maxDistance) {
        this.maxDistance = maxDistance;
    }

    private Double maxDistance;
    private List< Patrul > freePatrulList; // максимум 5 не занятых патрульных
    private List< Patrul > busyPatrulListInRadius;
    private List< Patrul > busyPatrulListOutOfRadius;
    private List< Patrul > freePatrulListOutOfRadius; // список патрульных которые не входят в радиус

    public PatrulInRadiusList () {}

    public PatrulInRadiusList ( final List< Patrul > patruls ) {
        this.freePatrulList = super.newList();
        this.busyPatrulListInRadius = super.newList();
        this.busyPatrulListOutOfRadius = super.newList();
        this.freePatrulListOutOfRadius = super.newList();

        for ( int i = 0; i < patruls.size() && this.freePatrulList.size() < 5; i++ ) {
            if ( patruls.get( i ).getPatrulTaskInfo().getTaskTypes().compareTo( TaskTypes.FREE ) == 0 ) {
                this.freePatrulList.add( patruls.get( i ) );
            }
        }

        // чтобы не было дупликатов, убираем патрульных из топ 5 списка
        patruls.removeAll( this.freePatrulList );
        this.setMaxDistance( this.freePatrulList.get( 4 ).getPatrulLocationData().getDistance() );

        super.analyze(
                patruls,
                patrul -> {
                    if ( patrul.getPatrulTaskInfo().getTaskTypes().compareTo( TaskTypes.FREE ) == 0 ) {
                        this.freePatrulListOutOfRadius.add( patrul );
                    }

                    else {
                        if ( maxDistance <= patrul.getPatrulLocationData().getDistance() ) {
                            this.busyPatrulListInRadius.add( patrul );
                        }
                        else {
                            this.busyPatrulListOutOfRadius.add( patrul );
                        }
                    }
                }
        );
    }
}
