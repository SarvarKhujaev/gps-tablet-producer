package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.CollectionsInspector;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

import java.util.Map;

import static com.ssd.mvd.gpstabletsservice.constants.Status.FREE;

public final class PatrulTaskInfo extends CollectionsInspector {
    public static PatrulTaskInfo generateWithInitialValues () {
        return new PatrulTaskInfo();
    }

    private PatrulTaskInfo () {
        this.setInitialValues();
    }

    public static <T> PatrulTaskInfo generate ( final T object ) {
        return object instanceof Row
                ? new PatrulTaskInfo( (Row) object )
                : new PatrulTaskInfo( (UDTValue) object );
    }

    private PatrulTaskInfo ( final Row row ) {
        this.setStatus( Status.valueOf( row.getString( "status" ) ) );
        this.setTaskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) );
        this.setListOfTasks( row.getMap( "listOfTasks", String.class, String.class ) );
    }

    private PatrulTaskInfo( final UDTValue udtValue ) {
        this.setStatus( Status.valueOf( udtValue.getString( "status" ) ) );
        this.setTaskTypes( TaskTypes.valueOf( udtValue.getString( "taskTypes" ) ) );
        this.setListOfTasks( udtValue.getMap( "listOfTasks", String.class, String.class ) );
    }

    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId( final String taskId ) {
        this.taskId = taskId;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus( final Status status ) {
        this.status = status;
    }

    public TaskTypes getTaskTypes() {
        return this.taskTypes;
    }

    public void setTaskTypes( final TaskTypes taskTypes ) {
        this.taskTypes = taskTypes;
    }

    public Map< String, String > getListOfTasks() {
        return this.listOfTasks;
    }

    public void setListOfTasks( final Map< String, String > listOfTasks ) {
        this.listOfTasks = listOfTasks;
    }

    private String taskId;
    // busy, free by default, available or not available
    private Status status;
    // task type which was attached to the current patrul
    private TaskTypes taskTypes;
    // the list which will store ids of all tasks which have been completed by Patrul
    private Map< String, String > listOfTasks = super.newMap();

    /*
    когда патрульного только создали
    то присваиваем ему все дефолтные значения
    связанные с задачами
    */
    private void setInitialValues () {
        this.setTaskId( "" );
        this.setStatus( FREE );
        this.setTaskTypes( TaskTypes.FREE );
        this.setListOfTasks( super.newMap() );
    }

    /*
    после выполнения задачи, сохраняем его ID и тип задачи в список выполненных
    */
    public void saveNewTaskInTheMapOfCompletedTasks ( final TaskTypes taskTypes ) {
        this.getListOfTasks().putIfAbsent( this.getTaskId(), taskTypes.name() );
    }

    /*
    если патрульный выполнил задачу или же его оттуда убрали,
    то обнуляем его связь с задачей и помечаем как свободного
    */
    public void unlinkPatrulFromTask () {
        this.setTaskTypes( TaskTypes.FREE );
        this.setStatus( Status.FREE );
        this.setTaskId( null );
    }
}
