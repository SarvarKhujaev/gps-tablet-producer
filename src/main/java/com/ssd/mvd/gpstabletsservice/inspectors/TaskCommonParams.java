package com.ssd.mvd.gpstabletsservice.inspectors;

import java.util.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.interfaces.ServiceCommonMethods;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulTimeConsumedToArriveToTaskLocation;

public class TaskCommonParams extends CollectionsInspector implements ServiceCommonMethods {
    /*
    показывает не завершено ли задание
    */
    public boolean isNotFinished() {
        return !this.getStatus().isFinished();
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getTaskId() {
        return this.taskId;
    }

    public TaskTypes getTaskTypes() {
        return this.taskTypes;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus( final Status status ) {
        this.status = status;
    }

    public Map< UUID, Patrul > getPatruls() {
        return this.patruls;
    }

    public List< ReportForCard > getReportForCardList() {
        return this.reportForCardList;
    }

    public Map< String, PatrulTimeConsumedToArriveToTaskLocation > getPatrulStatuses() {
        return this.patrulStatuses;
    }

    private final UUID uuid;
    private final String taskId;
    private final TaskTypes taskTypes;
    private Status status = Status.CREATED;

    /*
    хранит данные обо всех патрульных, которые участвуют в задании
    */
    @JsonDeserialize
    private final Map< UUID, Patrul > patruls;

    /*
    хранит данные обо всех рапортах от патрульных, которые участвуют в задании
    */
    @JsonDeserialize
    private final List< ReportForCard > reportForCardList;

    /*
    хранит данные об статусе патрульных, которые участвуют в задании
    */
    @JsonDeserialize
    private final Map< String, PatrulTimeConsumedToArriveToTaskLocation > patrulStatuses;

    public static TaskCommonParams generate (
            final TaskTypes taskTypes,
            final String taskId
    ) {
        return new TaskCommonParams( taskTypes, taskId );
    }

    private TaskCommonParams (
            final TaskTypes taskTypes,
            final String taskId
    ) {
        // сохраняем какой тип задачи использует этот Объект
        this.taskTypes = taskTypes;
        this.uuid = UUID.randomUUID();
        // если ID самой задачи не указан, то берем сгенерированный самим классом
        this.taskId = taskId != null ? taskId : this.uuid.toString();

        this.patruls = super.newMap();
        this.patrulStatuses = super.newMap();
        this.reportForCardList = super.newList();
    }

    /*
    если патрульного удаляют, то находим задачу которую он выполнил
    и убираем все ID связанные с этим патрульным
    */
    public TaskCommonParams unlinkTaskInfoFromPatrul ( final Patrul patrul ) {
        // убираем патрульного из списка патрульных в самом объекте
        this.getPatruls().remove( patrul.getUuid() );
        // убираем данные о статусе патрульного из списка патрульных в самом объекте
        this.getPatrulStatuses().remove( patrul.getPassportNumber() );
        // убираем данные о рапорте патрульного из списка патрульных в самом объекте
        this.getReportForCardList().remove(
                /*
                по ID патрульного находим индекс его рапорта в списке
                */
                TaskInspector
                        .getInstance()
                        .getReportIndex
                        .apply( this.getReportForCardList(), patrul.getUuid() )
        );

        return this;
    }

    @Override
    public void close () {
        this.getReportForCardList().clear();
        this.getPatruls().clear();
    }
}
