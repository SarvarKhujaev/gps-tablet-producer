package com.ssd.mvd.gpstracker.controller;

import com.ssd.mvd.gpstracker.database.Archive;
import com.ssd.mvd.gpstracker.database.CassandraDataControl;
import com.ssd.mvd.gpstracker.database.RedisDataControl;
import com.ssd.mvd.gpstracker.payload.ReqLocationExchange;
import com.ssd.mvd.gpstracker.request.Request;
import com.ssd.mvd.gpstracker.response.PatrulInfo;
import com.ssd.mvd.gpstracker.response.TrackerInfo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.lang.Math.*;

@RestController
public final class TabletsController {

    @MessageMapping( value = "trackers" )
    public Flux< TrackerInfo > trackers() { return Archive.getAchieve().getAllTrackers().map( trackers -> new TrackerInfo( trackers.getTopicName(), trackers.getKafkaConsumer().status ) ); }

    @MessageMapping ( value = "historyId" ) // returns the history of current tracker by its id
    public Mono< ReqLocationExchange > getHistory ( String id ) { return Archive.getAchieve().getPos( id ); }

    @MessageMapping ( value = "stream" ) // uses for monitoring of all trackers lan
    public Flux< ReqLocationExchange > getStream () { return Flux.fromStream( Archive.getAchieve().getPos() ); }

    @MessageMapping( value = "online" ) // returns only active trackers
    public Flux< TrackerInfo > online () { return Archive.getAchieve().getAllTrackers().filter( trackers -> trackers.getKafkaConsumer().status ).map( trackers -> new TrackerInfo( trackers.getTopicName(), trackers.getKafkaConsumer().status ) ); }

    @MessageMapping( value = "deleteTablet" ) // delete the current tracker by its id
    public Mono< Boolean > deleteTablet ( String trackerId ) { return Mono.just( Archive.getAchieve().switchOffInspector( trackerId ) ); }

    @MessageMapping ( value = "history" ) // uses for monitoring of all trackers lan
    public Flux< ReqLocationExchange > history ( Request request ) { return CassandraDataControl.getInstance().getHistory( request ).map( ReqLocationExchange::new ); }

    private Double calculate ( ReqLocationExchange first ) {
        if ( second == null ) {
            second = first;
            return ( distance = 0.0 );
        } else return ( distance += 12742 * asin( sqrt( 0.5 - cos( ( second.getLat() - first.getLat() ) * p ) / 2 + cos( first.getLat() * p ) * cos( second.getLat() * p ) * ( 1 - cos( ( second.getLan() - first.getLan() ) * p ) ) / 2 ) ) * 1000 );
//        return ( distance += ( 12742 * asin( sqrt( 0.5 - cos( ( lat2 - lat1 ) * p ) / 2 + cos( lat1 * p ) * cos( lat2 * p ) * ( 1 - cos( ( lon2 - lon1 ) * p ) ) / 2 ) ) * 1000 ) );
    }

    private static Double distance;
    private static final Double p = PI / 180;
    private static ReqLocationExchange second;

    @MessageMapping( value = "fuelConsumption" ) // it has to return the average fuel consumption with the full distance and the list of all lan and lats
    public Mono< PatrulInfo > fuelConsumption ( Request request ) { // computes the average fuel consumption of current user
        PatrulInfo patrulInfo = new PatrulInfo( this.history( request ) );
        return Mono.from( patrulInfo.getTransactions().map( this::calculate ) ).flatMap( value -> RedisDataControl.getRedis().getPatrul( (String) request.getSubject()).flatMap(patrul -> {
            RedisDataControl.getRedis().getCar( patrul.getCarNumber() ).subscribe( reqCar -> {
                reqCar.saveAverageFuelConsumption( patrulInfo.setDistance( distance ) );
                RedisDataControl.getRedis().update( reqCar ); } );
            patrulInfo.setNSF( patrul.getSurnameNameFatherName() );
            return Mono.just( patrulInfo ); } ) ); }

    @MessageMapping ( value = "averageFuelConsumption" ) // returns the average fuel consumption for current car without computing. this value is constant
    public Mono< Double > averageFuelConsumption ( String gosno ) { return RedisDataControl.getRedis().getCar( gosno ).flatMap( reqCar -> Mono.just( reqCar.getAverageFuelSize() ) ); }

    @MessageMapping ( value = "offline" ) // returns all inactive trackers
    public Flux<TrackerInfo> offline () { return Archive.getAchieve().getAllTrackers().filter( trackers -> trackers.getKafkaConsumer().status ).map( trackers -> new TrackerInfo( trackers.getTopicName(), trackers.getKafkaConsumer().status ) ); }
}
