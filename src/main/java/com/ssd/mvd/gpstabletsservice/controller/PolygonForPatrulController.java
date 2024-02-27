package com.ssd.mvd.gpstabletsservice.controller;

import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.ssd.mvd.gpstabletsservice.entity.polygons.Polygon;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.subscribers.CustomSubscriber;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.ScheduleForPolygonPatrul;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;

@RestController
public final class PolygonForPatrulController extends LogInspector {
    @MessageMapping( value = "listOfPoligonsForPatrul" )
    public Flux< Polygon > listOfPoligonsForPatrul () {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.POLYGON_FOR_PATRUl )
            .map( Polygon::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "getPatrulsForPolygon" )
    public Mono< List< Patrul > > getPatrulsForPolygon ( final String uuid ) {
        final List< Patrul > patrulList = super.newList();

        CassandraDataControl
                .getInstance()
                .getPolygonForPatrul
                .apply( uuid )
                .map( Polygon::getPatrulList )
                .subscribe( new CustomSubscriber<>(
                        uuids -> super.analyze(
                                uuids,
                                uuid1 -> patrulList.add(
                                        new Patrul(
                                                CassandraDataControl
                                                        .getInstance()
                                                        .getRowFromTabletsKeyspace(
                                                                CassandraTables.PATRULS,
                                                                "uuid",
                                                                uuid1.toString()
                                                        )
                                        )
                                )
                        )
                ) );

        return super.convert( patrulList );
    }

    @MessageMapping ( value = "deletePolygonForPatrul" )
    public Mono< ApiResponseModel > deletePolygonForPatrul ( final String uuid ) {
        return CassandraDataControl
            .getInstance()
            .deletePolygonForPatrul
            .apply( uuid )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "addPolygonForPatrul" )
    public Mono< ApiResponseModel > addPolygonForPatrul ( final Polygon polygon ) {
        polygon.setName( super.concatNames( polygon.getName() ) );
        return CassandraDataControl
                .getInstance()
                .addPolygonForPatrul
                .apply( polygon )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "updatePolygonForPatrul" )
    public Mono< ApiResponseModel > updatePolygonForPatrul ( final Polygon polygon ) {
        polygon.setName( super.concatNames( polygon.getName() ) );
        return CassandraDataControl
                .getInstance()
                .updatePolygonForPatrul
                .apply( polygon )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "addPatrulToPolygon" )
    public Mono< ApiResponseModel > addPatrulToPolygon (
            final ScheduleForPolygonPatrul scheduleForPolygonPatrul
    ) {
        return super.isCollectionNotEmpty( scheduleForPolygonPatrul.getPatrulUUIDs() )
                ? CassandraDataControl
                    .getInstance()
                    .addPatrulToPolygon
                    .apply( scheduleForPolygonPatrul )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.errorResponse() )
                : super.errorResponse( "patrul list cannot be empty" );
    }
}
