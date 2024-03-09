package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

import java.util.UUID;

public final class PatrulUniqueValues extends DataValidateInspector implements ObjectCommonMethods< PatrulUniqueValues > {
    public UUID getOrgan() {
        return this.organ;
    }

    public void setOrgan( final UUID organ ) {
        this.organ = organ;
    }

    public UUID getSos_id() {
        return this.sos_id;
    }

    public void setSos_id( final UUID sos_id ) {
        this.sos_id = sos_id;
    }

    public UUID getUuidOfEscort() {
        return this.uuidOfEscort;
    }

    public void setUuidOfEscort( final UUID uuidOfEscort ) {
        this.uuidOfEscort = uuidOfEscort;
    }

    public UUID getUuidForPatrulCar() {
        return this.uuidForPatrulCar;
    }

    public void setUuidForPatrulCar( final UUID uuidForPatrulCar ) {
        this.uuidForPatrulCar = uuidForPatrulCar;
    }

    public UUID getUuidForEscortCar() {
        return this.uuidForEscortCar;
    }

    public void setUuidForEscortCar( final UUID uuidForEscortCar ) {
        this.uuidForEscortCar = uuidForEscortCar;
    }

    private UUID organ; // choosing from dictionary
    private UUID sos_id; // choosing from dictionary
    private UUID uuidOfEscort; // UUID of the Escort which this car is linked to
    private UUID uuidForPatrulCar; // choosing from dictionary
    private UUID uuidForEscortCar; // choosing from dictionary

    /*
        отвязываем патрульного от Эскорта
    */
    public void unlinkFromEscortCar () {
        this.setUuidForEscortCar( null );
        this.setUuidOfEscort( null );
    }

    public PatrulUniqueValues setInitialValues () {
        this.setOrgan( null );
        this.setSos_id( null );
        this.unlinkFromEscortCar();
        this.setUuidForPatrulCar( null );

        return this;
    }

    public static PatrulUniqueValues empty () {
        return new PatrulUniqueValues();
    }

    private PatrulUniqueValues () {}

    @Override
    public PatrulUniqueValues generate( final Row row ) {
        this.setUuidForEscortCar( row.getUUID( "uuidForEscortCar" ) );
        this.setUuidForPatrulCar( row.getUUID( "uuidForPatrulCar" ) );
        this.setUuidOfEscort( row.getUUID( "uuidOfEscort" ) );
        this.setSos_id( row.getUUID( "sos_id" ) );
        this.setOrgan( row.getUUID( "organ" ) );

        return this;
    }

    @Override
    public PatrulUniqueValues generate( final UDTValue udtValue ) {
        super.checkAndSetParams(
                udtValue,
                udtValue1 -> {
                    this.setOrgan( udtValue.getUUID( "organ" ) );
                    this.setSos_id( udtValue.getUUID( "sos_id" ) );
                    this.setUuidOfEscort( udtValue.getUUID( "uuidOfEscort" ) );
                    this.setUuidForPatrulCar( udtValue.getUUID( "uuidForPatrulCar" ) );
                    this.setUuidForEscortCar( udtValue.getUUID( "uuidForEscortCar" ) );
                }
        );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setUUID( "organ", this.getOrgan() )
                .setUUID( "sos_id", this.getSos_id() )
                .setUUID( "uuidOfEscort", this.getUuidOfEscort() )
                .setUUID( "uuidForPatrulCar", this.getUuidForPatrulCar() )
                .setUUID( "uuidForEscortCar", this.getUuidForEscortCar() );
    }
}
