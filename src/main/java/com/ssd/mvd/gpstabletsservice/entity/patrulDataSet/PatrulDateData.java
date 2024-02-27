package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import java.util.Date;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;

public final class PatrulDateData extends TimeInspector {
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

    public void update ( final Integer value ) {
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
    }

    public static PatrulDateData generateWithInitialValues () {
        return new PatrulDateData();
    }

    private PatrulDateData () {
        this.update( 2 );
    }

    public static <T> PatrulDateData generate ( final T object ) {
        return object instanceof Row
                ? new PatrulDateData( (Row) object )
                : new PatrulDateData( (UDTValue) object );
    }

    private PatrulDateData ( final Row row ) {
        this.setTaskDate( row.getTimestamp( "taskDate" ) );
        this.setLastActiveDate( row.getTimestamp( "lastActiveDate" ) );
        this.setStartedToWorkDate( row.getTimestamp( "startedToWorkDate" ) );
        this.setDateOfRegistration( row.getTimestamp( "dateOfRegistration" ) );
    }

    private PatrulDateData( final UDTValue udtValue ) {
        this.setTaskDate( udtValue.getTimestamp( "taskDate" ) );
        this.setLastActiveDate( udtValue.getTimestamp( "lastActiveDate" ) );
        this.setStartedToWorkDate( udtValue.getTimestamp( "startedToWorkDate" ) );
        this.setDateOfRegistration( udtValue.getTimestamp( "dateOfRegistration" ) );
    }
}
