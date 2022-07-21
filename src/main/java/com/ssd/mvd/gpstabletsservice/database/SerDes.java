package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.payload.ReqLocationExchange;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Data;

import java.util.Date;

@Data
public class SerDes {
    private final Gson gson = new Gson();
    private static SerDes serDes = new SerDes();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static SerDes getSerDes () { return serDes != null ? serDes : ( serDes = new SerDes() ); }

    public String serialize ( Card object ) { return this.gson.toJson( object ); }

    public String serialize ( com.ssd.mvd.gpstabletsservice.entity.Data object ) { return this.gson.toJson( object ); }

    public String serialize ( Patrul object ) { return this.gson.toJson( object ); }

    public String serialize ( ReqCar object ) { return this.gson.toJson( object ); }

    public String serialize ( Polygon object ) { return this.gson.toJson( object ); }

    public String serialize ( AtlasLustra object ) { return this.gson.toJson( object ); }

    public String serialize ( PolygonType object ) { return this.gson.toJson( object ); }

    public String serialize ( Notification object ) { return this.gson.toJson( object ); }

    public String serialize( PoliceType policeType ) { return this.gson.toJson( policeType ); }

    public Patrul deserialize ( String object ) { return this.gson.fromJson( object, Patrul.class ); }

    public ReqCar deserializeCar ( String object ) { return this.gson.fromJson( object, ReqCar.class ); }

    public Polygon deserializePolygon ( String object ) { return this.gson.fromJson( object, Polygon.class ); }

    public AtlasLustra deserializeLustra ( String object ) { return this.gson.fromJson( object, AtlasLustra.class ); }

    public PoliceType deserializePoliceType ( String object ) { return this.gson.fromJson( object, PoliceType.class ); }

    public PolygonType deserializePolygonType( String value ) { return this.getGson().fromJson( value, PolygonType.class ); }

    public Patrul deserialize ( Object object ) { return this.objectMapper.convertValue( object, new TypeReference<>() {} ); }

    public Date convertDate ( String value ) { return this.objectMapper.convertValue( value, new TypeReference<>() {} ); }

    public ReqLocationExchange deserializeReqLocation( String position ) { return this.getGson().fromJson( position, ReqLocationExchange.class ); }

    public String serialize ( SelfEmploymentTask selfEmploymentTask ) { try { return this.objectMapper.writeValueAsString( selfEmploymentTask ); } catch ( JsonProcessingException e ) { throw new RuntimeException(e); } }

    public SelfEmploymentTask deserializeSelfEmployment ( String position ) { try { return this.objectMapper.reader().forType( SelfEmploymentTask.class ).readValue( position ); } catch ( JsonProcessingException e ) { throw new RuntimeException(e); } }
}
