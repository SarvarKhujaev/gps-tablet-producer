package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.CollectionsInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FREE;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

import java.util.Map;

public final class PatrulTaskInfo extends CollectionsInspector implements ObjectCommonMethods< PatrulTaskInfo > {
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
    после выполнения задачи, сохраняем его ID и тип задачи в список выполненных
    */
    public void saveNewTaskInTheMapOfCompletedTasks (
            final TaskTypes taskTypes
    ) {
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

    /*
    когда патрульного только создали
    то присваиваем ему все дефолтные значения
    связанные с задачами
    */
    public PatrulTaskInfo setInitialValues () {
        this.setListOfTasks( super.newMap() );
        this.setTaskTypes( TaskTypes.FREE );
        this.setStatus( FREE );
        this.setTaskId( "" );
        return this;
    }

    public static PatrulTaskInfo empty() {
        return new PatrulTaskInfo();
    }

    private PatrulTaskInfo () {}

    @Override
    public PatrulTaskInfo generate( final Row row ) {
        this.setListOfTasks( row.getMap( "listOfTasks", String.class, String.class ) );
        this.setTaskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) );
        this.setStatus( Status.valueOf( row.getString( "status" ) ) );
        return this;
    }

    @Override
    public PatrulTaskInfo generate( final UDTValue udtValue ) {
        this.setListOfTasks( udtValue.getMap( "listOfTasks", String.class, String.class ) );
        this.setTaskTypes( TaskTypes.valueOf( udtValue.getString( "taskTypes" ) ) );
        this.setStatus( Status.valueOf( udtValue.getString( "status" ) ) );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setString( "taskId", this.getTaskId() )
                .setString( "status", this.getStatus().name() )
                .setString( "taskTypes", this.getTaskTypes().name() )
                .setMap( "listOfTasks", this.getListOfTasks() );
    }
}
