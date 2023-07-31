package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import com.datastax.driver.core.UDTValue;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class ViolationsInformation {
    private Integer decreeStatus;
    private Integer amount;

    private String decreeSerialNumber;
    private String violation;
    private String division;
    private String payDate;
    private String address;
    private String article;
    private String owner;
    private String model;
    private String bill;

    public ViolationsInformation ( final UDTValue value ) {
        this.setAmount( value.getInt( "amount" ) );
        this.setDecreeStatus( value.getInt( "decreeStatus" ) );

        this.setBill( value.getString( "bill" ) );
        this.setModel( value.getString( "model" ) );
        this.setOwner( value.getString( "owner" ) );
        this.setArticle( value.getString( "article" ) );
        this.setAddress( value.getString( "address" ) );
        this.setPayDate( value.getString( "payDate" ) );
        this.setDivision( value.getString( "division" ) );
        this.setViolation( value.getString( "violation" ) );
        this.setDecreeSerialNumber( value.getString( "decreeSerialNumber" ) ); }
}
