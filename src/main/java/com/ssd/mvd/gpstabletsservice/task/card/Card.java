package com.ssd.mvd.gpstabletsservice.task.card;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.interfaces.TaskCommonMethods;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.CARD_102;

@JsonIgnoreProperties ( ignoreUnknown = true )
public final class Card implements TaskCommonMethods< Card > {
    public long getCardId() {
        return this.cardId;
    }

    public void setCardId( final long cardId ) {
        this.cardId = cardId;
    }

    public long getGomNum() {
        return this.gomNum;
    }

    public void setGomNum( final long gomNum ) {
        this.gomNum = gomNum;
    }

    public long getFirstOfAll() {
        return this.firstOfAll;
    }

    public void setFirstOfAll( final long firstOfAll ) {
        this.firstOfAll = firstOfAll;
    }

    public long getDeadQuantity() {
        return this.deadQuantity;
    }

    public void setDeadQuantity( final long deadQuantity ) {
        this.deadQuantity = deadQuantity;
    }

    public long getTraumaQuantity() {
        return this.traumaQuantity;
    }

    public void setTraumaQuantity( final long traumaQuantity ) {
        this.traumaQuantity = traumaQuantity;
    }

    public int getBranchId() {
        return this.branchId;
    }

    public void setBranchId( final int branchId ) {
        this.branchId = branchId;
    }

    public int getsEventFormsAddId() {
        return this.sEventFormsAddId;
    }

    public void setsEventFormsAddId( final int sEventFormsAddId ) {
        this.sEventFormsAddId = sEventFormsAddId;
    }

    public int getInitSeventFormsId() {
        return this.initSeventFormsId;
    }

    public void setInitSeventFormsId( final int initSeventFormsId ) {
        this.initSeventFormsId = initSeventFormsId;
    }

    public String getFabula() {
        return this.fabula;
    }

    public void setFabula( final String fabula ) {
        this.fabula = fabula;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress( final String address ) {
        this.address = address;
    }

    public String getUserFio() {
        return this.userFio;
    }

    public void setUserFio( final String userFio ) {
        this.userFio = userFio;
    }

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude( final double latitude ) {
        this.latitude = latitude;
    }

    @Override
    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude( final double longitude ) {
        this.longitude = longitude;
    }

    public boolean isHospitalApplication() {
        return this.hospitalApplication;
    }

    public void setHospitalApplication( final boolean hospitalApplication ) {
        this.hospitalApplication = hospitalApplication;
    }

    public Date getEventEnd() {
        return this.eventEnd;
    }

    public void setEventEnd( final Date eventEnd ) {
        this.eventEnd = eventEnd;
    }

    public Date getEventStart() {
        return this.eventStart;
    }

    public void setEventStart( final Date eventStart ) {
        this.eventStart = eventStart;
    }

    public Date getCreated_date() {
        return this.created_date;
    }

    public void setCreated_date( final Date created_date ) {
        this.created_date = created_date;
    }

    public EventHuman getEventHuman() {
        return this.eventHuman;
    }

    public void setEventHuman( final EventHuman eventHuman ) {
        this.eventHuman = eventHuman;
    }

    public EventAddress getEventAddress() {
        return this.eventAddress;
    }

    public void setEventAddress( final EventAddress eventAddress ) {
        this.eventAddress = eventAddress;
    }

    public List< VictimHumans > getVictimHumans() {
        return this.victimHumans;
    }

    public void setVictimHumans( final List< VictimHumans > victimHumans ) {
        this.victimHumans = victimHumans;
    }

    @Override
    public TaskCommonParams getTaskCommonParams () {
        return this.taskCommonParams;
    }

    @JsonProperty( value = "id" )
    private long cardId;
    private long gomNum;  //??
    @JsonProperty( value = "FirstOfAll" )
    private long firstOfAll;  //??
    private long deadQuantity;   //O'lganlar soni
    private long traumaQuantity;   //Jarohatlanganlar soni

    private int branchId;   //???
    private int sEventFormsAddId;  //??
    private int initSeventFormsId;  //??

    private String fabula;   //????
    private String address;
    private String userFio; //Ariza berivchining F.I.SH

    private double latitude;   // Hodisa bo'lgan joy
    private double longitude;   // Hodisa bo'lgan joy
    private boolean hospitalApplication;   // Ariza shifoxonadan kelgan-kelmaganligi

    private Date eventEnd;   // Tugallangan vaqt
    private Date eventStart;  // Yaratilish vaqt
    @JsonProperty( value = "dateCreateCard" )
    private Date created_date;   // Qachon yaratilgani

    @JsonDeserialize
    private EventHuman eventHuman;   // Aybdor inson

    @JsonDeserialize
    private EventAddress eventAddress;   //Voqea manzili

    @JsonDeserialize
    private List< VictimHumans > victimHumans;

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            CARD_102,
            String.valueOf( this.getCardId() )
    );

    @Override
    public Card update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }

    public Card () {}
}