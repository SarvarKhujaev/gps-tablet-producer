package com.ssd.mvd.gpstabletsservice.inspectors;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;

import java.util.stream.Stream;

/*
хранит все функции для более компактного и удобного хранения всех основных функции WebFlux
 */
public class WebFluxInspector extends DataValidateInspector {
    protected ParallelFlux< Row > convertValuesToParallelFlux (
            final ResultSet resultSet,
            final int parallelsCount
    ) {
        return Flux.fromStream(
                super.convertRowToStream( resultSet )
                ).parallel( super.checkDifference( parallelsCount ) )
                .runOn( Schedulers.parallel() );
    }

    protected <T> ParallelFlux< T > convertValuesToParallelFlux (
            final Stream<T> customStream,
            final int parallelsCount
    ) {
        return Flux.fromStream( customStream )
                .parallel( super.checkDifference( parallelsCount ) )
                .runOn( Schedulers.parallel() );
    }

    protected ParallelFlux< Row > convertValuesToParallelFlux (
            final ResultSet resultSet
    ) {
        return Flux.fromStream(
                        super.convertRowToStream( resultSet )
                ).parallel()
                .runOn( Schedulers.parallel() );
    }
}
