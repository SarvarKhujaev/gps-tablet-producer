package com.ssd.mvd.gpstabletsservice.interfaces;

import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulTimeConsumedToArriveToTaskLocation;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.ActiveTask;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;

/*
хранит общие функции и логику всех задач
*/
public interface TaskCommonMethods< T > {
    double getLatitude();

    double getLongitude();

    TaskCommonParams getTaskCommonParams ();

    TaskCommonMethods<T> update ( final ReportForCard reportForCard );

    default void update (
            final Patrul patrul,
            final PatrulTimeConsumedToArriveToTaskLocation patrulStatus,
            final TaskCommonParams taskCommonParams
    ) {
        taskCommonParams.getPatrulStatuses().put( patrul.getPassportNumber(), patrulStatus );
    }

    default void update (
            final Patrul patrul,
            final TaskCommonParams taskCommonParams
    ) {
        taskCommonParams.getPatruls().remove(
                CassandraDataControlForTasks
                        .getInstance()
                        .deleteRowFromTaskTimingTable
                        .apply( patrul )
        );
    }

    default void remove (
            final Patrul patrul,
            final TaskCommonParams taskCommonParams
    ) {
        taskCommonParams.getPatruls().remove( patrul.getUuid() );
    }

    default void update () {
        // в случае если количество патрульных равно количеству рапортов, то значит что таск закрыт
        if (
                this.getTaskCommonParams().checkCollectionsLengthEquality(
                        this.getTaskCommonParams().getPatruls(),
                        this.getTaskCommonParams().getReportForCardList()
                )
        ) {
            this.getTaskCommonParams().setStatus( FINISHED );

            /*
            удаляем объект ActiveTask, чтобы н больше не воявлялся в списке активных задач
            */
            CassandraDataControlForTasks
                    .getInstance()
                    .deleteActiveTask
                    .accept( this.getTaskCommonParams().getUuid().toString() );

            /*
            в работе появился баг, когда из задачи удаляли всех патрульных,
            то он появлялся в списке завершенных,
            поэтому сначала проверяем что у задачи есть прикрепленные патрульные,
            а потом отправляем данные через Кафку
            */
            if ( !this.getTaskCommonParams().getPatruls().isEmpty() ) {
                // если таск закончен без удаления всех патрульных, то есть удачно завершен
                KafkaDataControl
                        .getKafkaDataControl()
                        .getWriteActiveTaskToKafka()
                        .accept( ActiveTask.generate( this ) );
            }
        }
    }
}
