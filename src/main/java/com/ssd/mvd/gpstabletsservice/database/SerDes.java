package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Data;

@Data
public class SerDes {
    private static String result;
    private final Gson gson = new Gson();
    private static SerDes serDes = new SerDes();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static SerDes getSerDes () { return serDes != null ? serDes : ( serDes = new SerDes() ); }

    public <T> String serialize ( T object ) { return this.getGson().toJson( object ); }

    public Card deserializeCard ( String object ) { return this.getGson().fromJson( object, Card.class ); }

    public CarEvent deserializeCarEvents ( String card ) { return this.getGson().fromJson( card, CarEvent.class ); }

    public ActiveTask deserializeActiveTask ( String value ) { return this.getGson().fromJson( value, ActiveTask.class ); }

    public Patrul deserialize ( Object object ) { return this.getObjectMapper().convertValue( object, new TypeReference<>() {} ); }

    public Card deserializeCard ( Object object ) { return this.getObjectMapper().convertValue( object, new TypeReference<>() {} ); }

    public CarEvent deserializeCarEvents ( Object card ) { return this.getObjectMapper().convertValue( card, new TypeReference<>() {} ); }

    public EventCar deserializeEventCar ( String object ) { return this.getGson().fromJson( object, EventCar.class ); }

    public EventFace deserializeEventFace ( String object ) { return this.getGson().fromJson( object, EventFace.class ); }

    public EventBody deserializeEventBody ( String object ) { return this.getGson().fromJson( object, EventBody.class ); }

    public FaceEvent deserializeFaceEvents ( String object ) { return this.getGson().fromJson( object, FaceEvent.class ); }

    public CarTotalData deserializeCarTotalData ( String object ) { return this.getGson().fromJson( object, CarTotalData.class ); }

    public EventCar deserializeEventCar ( Object object ) { return this.getObjectMapper().convertValue( object, new TypeReference<>() {} ); }

    public EventFace deserializeEventFace ( Object object ) { return this.getObjectMapper().convertValue( object, new TypeReference<>() {} ); }

    public EventBody deserializeEventBody ( Object object ) { return this.getObjectMapper().convertValue( object, new TypeReference<>() {} ); }

    public FaceEvent deserializeFaceEvents ( Object object ) { return this.getObjectMapper().convertValue( object, new TypeReference<>() {} ); }

    public SelfEmploymentTask deserializeSelfEmploymentTask ( String object ) { return this.getGson().fromJson( object, SelfEmploymentTask.class ); }
}
