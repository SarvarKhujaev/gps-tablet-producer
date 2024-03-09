package com.ssd.mvd.gpstabletsservice.inspectors;

import java.util.*;
import java.util.stream.Stream;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.ResultSet;

import com.ssd.mvd.gpstabletsservice.tuple.Points;
import com.ssd.mvd.gpstabletsservice.entity.PoliceType;
import com.ssd.mvd.gpstabletsservice.entity.CameraList;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.*;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonType;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonEntity;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PositionInfo;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;

public class CollectionsInspector {
    protected CollectionsInspector () {}

    protected final List< Class > listOfClasses = List.of(
            Patrul.class,
            CameraList.class,
            PoliceType.class,
            PolygonType.class,
            PositionInfo.class,
            PolygonEntity.class,
            ReportForCard.class,
            PatrulCarInfo.class,
            PatrulFIOData.class,
            PatrulTaskInfo.class,
            PatrulDateData.class,
            PatrulAuthData.class,
            PatrulTokenInfo.class,
            PatrulRegionData.class,
            PatrulUniqueValues.class,
            PatrulLocationData.class,
            PatrulMobileAppInfo.class,
            ViolationsInformation.class,

            PolygonEntity.class,
            Points.class
    );

    protected Map< CassandraTables, List< CassandraTables > > getMapOfKeyspaceAndTypes () {
        final Map< CassandraTables, List< CassandraTables > > keyspaceAndTypes = this.newMap();

        keyspaceAndTypes.put(
                CassandraTables.TABLETS,
                List.of(
                        CassandraTables.PATRUL_TYPE,
                        CassandraTables.CAMERA_LIST,
                        CassandraTables.POLICE_TYPE,
                        CassandraTables.POLYGON_TYPE,
                        CassandraTables.POSITION_INFO,
                        CassandraTables.POLYGON_ENTITY,
                        CassandraTables.REPORT_FOR_CARD,
                        CassandraTables.PATRUL_CAR_DATA,
                        CassandraTables.PATRUL_FIO_DATA,
                        CassandraTables.PATRUL_TASK_DATA,
                        CassandraTables.PATRUL_DATE_DATA,
                        CassandraTables.PATRUL_AUTH_DATA,
                        CassandraTables.PATRUL_TOKEN_DATA,
                        CassandraTables.PATRUL_REGION_DATA,
                        CassandraTables.PATRUL_UNIQUE_DATA,
                        CassandraTables.PATRUL_LOCATION_DATA,
                        CassandraTables.PATRUL_MOBILE_DATA,
                        CassandraTables.VIOLATION_LIST_TYPE
                )
        );

        keyspaceAndTypes.put(
                CassandraTables.ESCORT,
                List.of(
                        CassandraTables.POLYGON_ENTITY,
                        CassandraTables.POINTS_ENTITY
                )
        );

        return keyspaceAndTypes;
    }

    protected final Set< String > detailsList = Set.of(
            "Ф.И.О",
            "",
            "ПОДРАЗДЕЛЕНИЕ",
            "ДАТА И ВРЕМЯ",
            "ID",
            "ШИРОТА",
            "ДОЛГОТА",
            "ВИД ПРОИСШЕСТВИЯ",
            "НАЧАЛО СОБЫТИЯ",
            "КОНЕЦ СОБЫТИЯ",
            "КОЛ.СТВО ПОСТРАДАВШИХ",
            "КОЛ.СТВО ПОГИБШИХ",
            "ФАБУЛА"
    );

    protected final List< String > fields = List.of(
            "F.I.O",
            "Tug'ilgan sana",
            "Telefon raqam",
            "Unvon",
            "Viloyat",
            "Tuman/Shahar",
            "Patrul turi",
            "Oxirgi faollik vaqti",
            "Ishlashni boshlagan vaqti",
            "Ro'yxatdan o'tgan vaqti",
            "Umumiy faollik vaqti",
            "Planshet quvvati"
    );

    protected <T> List<T> emptyList () {
        return Collections.emptyList();
    }

    public <T> ArrayList<T> newList () {
        return new ArrayList<>();
    }

    protected <T, V> TreeMap<T, V> newTreeMap () {
        return new TreeMap<>();
    }

    protected <T, V> Map<T, V> newMap () {
        return new HashMap<>();
    }

    protected boolean checkCollectionsLengthEquality (
            final Map firstCollection,
            final Collection secondCollection
    ) {
        return firstCollection.size() == secondCollection.size();
    }

    public <T> void analyze (
            final Collection<T> someList,
            final Consumer<T> someConsumer
    ) {
        someList.forEach( someConsumer );
    }

    protected <T, V> void analyze (
            final Map< T, V > someList,
            final BiConsumer<T, V> someConsumer
    ) {
        someList.forEach( someConsumer );
    }

    protected <T> boolean isCollectionNotEmpty ( final Collection<T> collection ) {
        return collection != null && !collection.isEmpty();
    }

    protected Stream< Row > convertRowToStream ( final ResultSet resultSet ) {
        return resultSet.all().stream();
    }

    protected <T> List<T> convertArrayToList (
            final T[] objects
    ) {
        return Arrays.asList( objects );
    }
}
