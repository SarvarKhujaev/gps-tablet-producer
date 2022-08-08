package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.util.Date;
import lombok.Data;

@Data
public class SerDes {
    private final Gson gson = new Gson();
    private static SerDes serDes = new SerDes();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static SerDes getSerDes () { return serDes != null ? serDes : ( serDes = new SerDes() ); }

    public String serialize ( Card object ) { return this.gson.toJson( object ); }

    public String serialize ( EventCar face ) { return this.gson.toJson( face ); }

    public String serialize ( EventFace face ) { return this.gson.toJson( face ); }

    public String serialize ( EventBody face ) { return this.gson.toJson( face ); }

    public String serialize ( Patrul object ) { return this.gson.toJson( object ); }

    public String serialize ( ReqCar object ) { return this.gson.toJson( object ); }

    public String serialize ( Polygon object ) { return this.gson.toJson( object ); }

    public String serialize ( AtlasLustra object ) { return this.gson.toJson( object ); }

    public String serialize ( PolygonType object ) { return this.gson.toJson( object ); }

    public String serialize ( Notification object ) { return this.gson.toJson( object ); }

    public String serialize( PoliceType policeType ) { return this.gson.toJson( policeType ); }

    public String serialize ( ActiveTask activeTask ) { return this.gson.toJson( activeTask ); }

    public String serialize ( TupleOfPatrul tupleOfPatrul ) { return this.gson.toJson( tupleOfPatrul ); }
    public String serialize ( SelfEmploymentTask selfEmploymentTask ) { try { return this.objectMapper.writeValueAsString( selfEmploymentTask ); }
    catch ( JsonProcessingException e ) { throw new RuntimeException(e); } }

    public Card deserializeCard ( String object ) { return this.gson.fromJson( object, Card.class ); }

    public Patrul deserialize ( String object ) { return this.gson.fromJson( object, Patrul.class ); }

    public ReqCar deserializeCar ( String object ) { return this.gson.fromJson( object, ReqCar.class ); }

    public Polygon deserializePolygon ( String object ) { return this.gson.fromJson( object, Polygon.class ); }

    public AtlasLustra deserializeLustra ( String object ) { return this.gson.fromJson( object, AtlasLustra.class ); }

    public PoliceType deserializePoliceType ( String object ) { return this.gson.fromJson( object, PoliceType.class ); }

    public PolygonType deserializePolygonType( String value ) { return this.getGson().fromJson( value, PolygonType.class ); }

    public Patrul deserialize ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public Date convertDate ( String value ) { return this.objectMapper.convertValue( value, new TypeReference<>() {} ); }

    public SelfEmploymentTask deserializeSelfEmployment ( String position ) { try { return this.objectMapper.reader()
            .forType( SelfEmploymentTask.class ).readValue( position ); } catch ( JsonProcessingException e ) { throw new RuntimeException(e); } }

    public ActiveTask deserializeActiveTask ( String value ) { return this.gson.fromJson( value, ActiveTask.class ); }

    public Card deserializeCard ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public EventCar deserializeEventCar ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public EventFace deserializeEventFace ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public EventBody deserializeEventBody ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }
}
