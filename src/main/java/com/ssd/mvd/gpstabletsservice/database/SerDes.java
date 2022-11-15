package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.task.card.SosMessageForTopic;
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

import java.util.Date;
import lombok.Data;

@Data
public class SerDes {
    private static String result;
    private final Gson gson = new Gson();
    private static SerDes serDes = new SerDes();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static SerDes getSerDes () { return serDes != null ? serDes : ( serDes = new SerDes() ); }

    public String serialize ( Card object ) { return this.gson.toJson( object ); }

    public String serialize ( EventCar eventCar ) { return this.gson.toJson( eventCar ); }

    public String serialize ( CarEvent carEvent ) { return this.gson.toJson( carEvent ); }

    public String serialize ( EventFace eventFace ) { return this.gson.toJson( eventFace ); }

    public String serialize ( EventBody eventBody ) { return this.gson.toJson( eventBody ); }

    public String serialize ( SosMessageForTopic sos ) { return this.getGson().toJson( sos ); }

    public String serialize( FaceEvent faceEvents ) { return this.gson.toJson( faceEvents ); }

    public String serialize ( ActiveTask activeTask ) { return this.gson.toJson( activeTask ); }

    public String serialize ( CarTotalData carTotalData ) { return this.gson.toJson( carTotalData ); }

    public String serialize ( Notification notification ) { return this.gson.toJson( notification ); }

    public String serialize ( SelfEmploymentTask selfEmploymentTask ) { return this.gson.toJson( selfEmploymentTask ); }

    public Card deserializeCard ( String object ) { return this.gson.fromJson( object, Card.class ); }

    public Patrul deserialize ( String object ) { return this.gson.fromJson( object, Patrul.class ); }

    public CarEvent deserializeCarEvents ( String card ) { return this.gson.fromJson( card, CarEvent.class ); }

    public ActiveTask deserializeActiveTask ( String value ) { return this.gson.fromJson( value, ActiveTask.class ); }

    public Date convertDate ( String value ) { return this.objectMapper.convertValue( value, new TypeReference<>() {} ); }

    public Patrul deserialize ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public Card deserializeCard ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public CarEvent deserializeCarEvents ( Object card ) { return this.objectMapper.convertValue( card, new TypeReference<>() {} ); }

    public EventCar deserializeEventCar ( String object ) { return this.gson.fromJson( object, EventCar.class ); }

    public EventFace deserializeEventFace ( String object ) { return this.gson.fromJson( object, EventFace.class ); }

    public EventBody deserializeEventBody ( String object ) { return this.gson.fromJson( object, EventBody.class ); }

    public FaceEvent deserializeFaceEvents ( String object ) { return this.gson.fromJson( object, FaceEvent.class ); }

    public CarTotalData deserializeCarTotalData ( String object ) { return this.gson.fromJson( object, CarTotalData.class ); }

    public EventCar deserializeEventCar ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public EventFace deserializeEventFace ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public EventBody deserializeEventBody ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public FaceEvent deserializeFaceEvents ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public SelfEmploymentTask deserializeSelfEmploymentTask ( String object ) { return this.gson.fromJson( object, SelfEmploymentTask.class ); }
}
