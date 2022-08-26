package com.ssd.mvd.gpstabletsservice.entity;

import java.util.UUID;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class PoliceType {
    private UUID uuid;
    private String policeType;

    public PoliceType( UDTValue value ) {
        this.setUuid( value.getUUID( "uuid" ) );
        this.setPoliceType( value.getString( "PoliceType" ) ); }

    public PoliceType( Row value ) {
        this.setUuid( value.getUUID( "uuid" ) );
        this.setPoliceType( value.getString( "PoliceType" ) ); }
}
