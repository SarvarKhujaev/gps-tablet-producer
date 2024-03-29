package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;
import com.datastax.driver.core.UDTValue;

public final class ViolationsInformation implements ObjectCommonMethods< ViolationsInformation > {
    public int getDecreeStatus() {
        return this.decreeStatus;
    }

    public void setDecreeStatus ( final int decreeStatus ) {
        this.decreeStatus = decreeStatus;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount ( final int amount ) {
        this.amount = amount;
    }

    public String getDecreeSerialNumber() {
        return this.decreeSerialNumber;
    }

    public void setDecreeSerialNumber ( final String decreeSerialNumber ) {
        this.decreeSerialNumber = decreeSerialNumber;
    }

    public String getViolation() {
        return this.violation;
    }

    public void setViolation ( final String violation ) {
        this.violation = violation;
    }

    public String getDivision() {
        return this.division;
    }

    public void setDivision ( final String division ) {
        this.division = division;
    }

    public String getPayDate() {
        return this.payDate;
    }

    public void setPayDate ( final String payDate ) {
        this.payDate = payDate;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress ( final String address ) {
        this.address = address;
    }

    public String getArticle() {
        return this.article;
    }

    public void setArticle ( final String article ) {
        this.article = article;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner ( final String owner ) {
        this.owner = owner;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel ( final String model ) {
        this.model = model;
    }

    public String getBill() {
        return this.bill;
    }

    public void setBill ( final String bill ) {
        this.bill = bill;
    }

    private int decreeStatus;
    private int amount;

    private String decreeSerialNumber;
    private String violation;
    private String division;
    private String payDate;
    private String address;
    private String article;
    private String owner;
    private String model;
    private String bill;

    public static ViolationsInformation empty () {
        return new ViolationsInformation();
    }

    private ViolationsInformation () {}

    @Override
    public ViolationsInformation generate( final UDTValue udtValue ) {
        this.setAmount( udtValue.getInt( "amount" ) );
        this.setDecreeStatus( udtValue.getInt( "decreeStatus" ) );

        this.setBill( udtValue.getString( "bill" ) );
        this.setModel( udtValue.getString( "model" ) );
        this.setOwner( udtValue.getString( "owner" ) );
        this.setArticle( udtValue.getString( "article" ) );
        this.setAddress( udtValue.getString( "address" ) );
        this.setPayDate( udtValue.getString( "payDate" ) );
        this.setDivision( udtValue.getString( "division" ) );
        this.setViolation( udtValue.getString( "violation" ) );
        this.setDecreeSerialNumber( udtValue.getString( "decreeSerialNumber" ) );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setInt ( "amount", this.getAmount() )
                .setInt( "decreeStatus", this.getDecreeStatus() )
                .setString( "bill", this.getBill() )
                .setString( "model", this.getModel() )
                .setString( "owner", this.getOwner() )
                .setString( "article", this.getArticle() )
                .setString( "address", this.getAddress() )
                .setString( "payDate", this.getPayDate() )
                .setString( "division", this.getDivision() )
                .setString( "violation", this.getViolation() )
                .setString( "decreeSerialNumber", this.getDecreeSerialNumber() );
    }
}
