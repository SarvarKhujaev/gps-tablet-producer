package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.card.Card;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.BiFunction;
import java.util.function.Function;
import com.google.gson.Gson;

@lombok.Data
public class SerDes extends CassandraConverter {
    private final Gson gson = new Gson();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public <T> String serialize ( T object ) { return this.getGson().toJson( object ); }

    private final BiFunction< String, TaskTypes, ? > deserialize = ( s, taskTypes ) -> switch ( taskTypes ) {
        case CARD_102 -> this.getGson().fromJson( s, Card.class );

        case FIND_FACE_CAR -> this.getGson().fromJson( s, CarEvent.class );
        case FIND_FACE_PERSON -> this.getGson().fromJson( s, FaceEvent.class );

        case FIND_FACE_EVENT_CAR -> this.getGson().fromJson( s, EventCar.class );
        case FIND_FACE_EVENT_BODY -> this.getGson().fromJson( s, EventBody.class );
        case FIND_FACE_EVENT_FACE -> this.getGson().fromJson( s, EventFace.class );
        case SELF_EMPLOYMENT -> this.getGson().fromJson( s, SelfEmploymentTask.class );

        case ACTIVE_TASK -> this.getGson().fromJson( s, ActiveTask.class );
        default -> this.getGson().fromJson( s, CarTotalData.class ); };

    private final Function< Object, ? > deserializeWithJackson = s -> this.getObjectMapper().convertValue( s, new TypeReference<>() {} );
}
