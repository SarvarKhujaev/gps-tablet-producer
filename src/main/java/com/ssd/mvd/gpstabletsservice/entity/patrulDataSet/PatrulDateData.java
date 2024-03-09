package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import java.util.Date;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

public final class PatrulDateData extends TimeInspector implements ObjectCommonMethods< PatrulDateData > {
    public Date getTaskDate() {
        return this.taskDate;
    }

    public void setTaskDate( final Date taskDate ) {
        this.taskDate = taskDate;
    }

    public Date getLastActiveDate() {
        return this.lastActiveDate;
    }

    public void setLastActiveDate( final Date lastActiveDate ) {
        this.lastActiveDate = lastActiveDate;
    }

    public Date getStartedToWorkDate() {
        return this.startedToWorkDate;
    }

    public void setStartedToWorkDate( final Date startedToWorkDate ) {
        this.startedToWorkDate = startedToWorkDate;
    }

    public Date getDateOfRegistration() {
        return this.dateOfRegistration;
    }

    public void setDateOfRegistration( final Date dateOfRegistration ) {
        this.dateOfRegistration = dateOfRegistration;
    }

    /*
        хранит данные о патрульном связанные с датами
    */
    // дата начала выполнения задачи
    private Date taskDate;
    // хранит дату последней активности патрульного, обновляется при каждом действии
    private Date lastActiveDate;
    // хранит дату
    private Date startedToWorkDate;
    private Date dateOfRegistration;

    public PatrulDateData update ( final Integer value ) {
        final Date date = super.newDate();
        switch ( value ) {
            case 2 -> {
                this.setTaskDate( date );
                this.setLastActiveDate( date );
                this.setStartedToWorkDate( date );
                this.setDateOfRegistration( date );
            }
            case 1 -> this.setTaskDate( date );
            default -> this.setStartedToWorkDate( date );
        }

        return this;
    }

    private PatrulDateData () {}

    public static PatrulDateData empty() {
        return new PatrulDateData();
    }

    @Override
    public PatrulDateData generate( final Row row ) {
        this.setDateOfRegistration( row.getTimestamp( "dateOfRegistration" ) );
        this.setStartedToWorkDate( row.getTimestamp( "startedToWorkDate" ) );
        this.setLastActiveDate( row.getTimestamp( "lastActiveDate" ) );
        this.setTaskDate( row.getTimestamp( "taskDate" ) );

        return this;
    }

    @Override
    public PatrulDateData generate( final UDTValue udtValue ) {
        this.setDateOfRegistration( udtValue.getTimestamp( "dateOfRegistration" ) );
        this.setStartedToWorkDate( udtValue.getTimestamp( "startedToWorkDate" ) );
        this.setLastActiveDate( udtValue.getTimestamp( "lastActiveDate" ) );
        this.setTaskDate( udtValue.getTimestamp( "taskDate" ) );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setTimestamp( "taskDate", this.getTaskDate() )
                .setTimestamp( "lastActiveDate", this.getLastActiveDate() )
                .setTimestamp( "startedToWorkDate", this.getStartedToWorkDate() )
                .setTimestamp( "dateOfRegistration", this.getDateOfRegistration() );
    }
}
