package com.ssd.mvd.gpstabletsservice.inspectors;

import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulTimeConsumedToArriveToTaskLocation;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.ActiveTask;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;

/*
отвечает за все операции связанные с задачами патрульных
*/
public class TaskOperations {
    /*
    когда патрульный добрался до назначенной точки,
    то сохраняем данные о том, сколько времени он потратил,
    и успел ли он вовремя
    */
    protected void update (
            final Patrul patrul,
            final PatrulTimeConsumedToArriveToTaskLocation patrulStatus,
            final TaskCommonParams taskCommonParams ) {
        taskCommonParams.getPatrulStatuses().put( patrul.getPassportNumber(), patrulStatus );
    }

    /*
    когда патрульному поручают задачу или его добавили как доп помощь,
    то сохраняем его ID в список к остальным патрульным
    внутри самой задачи
    */
    protected void update (
            final Patrul patrul,
            final TaskCommonParams taskCommonParams ) {
        taskCommonParams.getPatruls().put( patrul.getUuid(), patrul );
    }

    /*
    в случае когда патрульный завершил задачу или если его убрали из задачи
    то убираем его ID из общего списка
    */
    protected void remove (
            final Patrul patrul,
            final TaskCommonParams taskCommonParams ) {
        taskCommonParams.getPatruls().remove(
                CassandraDataControlForTasks
                        .getInstance()
                        .deleteRowFromTaskTimingTable
                        .apply( patrul ) );
    }

    protected void update (
            final TaskCommonParams taskCommonParams,
            final Object object
    ) {
        // в случае если количество патрульных равно количеству рапортов, то значит что таск закрыт
        if ( taskCommonParams.checkCollectionsLengthEquality(
                taskCommonParams.getPatruls(),
                taskCommonParams.getReportForCardList() ) ) {
            taskCommonParams.setStatus( FINISHED );

            /*
            удаляем объект ActiveTask, чтобы н больше не воявлялся в списке активных задач
            */
            CassandraDataControlForTasks
                    .getInstance()
                    .deleteActiveTask
                    .accept( taskCommonParams.getUuid().toString() );

            /*
            в работе появился баг, когда из задачи удаляли всех патрульных,
            то он появлялся в списке завершенных,
            поэтому сначала проверяем что у задачи есть прикрепленные патрульные,
            а потом отправляем данные через Кафку
            */
            if ( !taskCommonParams.getPatruls().isEmpty() ) {
                // если таск закончен без удаления всех патрульных, то есть удачно завершен
                KafkaDataControl
                        .getKafkaDataControl()
                        .getWriteActiveTaskToKafka()
                        .accept( ActiveTask.generate( object, taskCommonParams ) );
            }
        }
    }
}
