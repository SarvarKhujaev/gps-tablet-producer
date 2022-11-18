package com.ssd.mvd.gpstabletsservice.constants;

public enum CassandraTables {
    // tables for CRUD
    TABLETS,
    REPORT_FOR_CARD, POLYGON_FOR_PATRUl, TABLETS_USAGE_TABLE,
    CARS, LUSTRA, PATRULS, POLYGON, NOTIFICATION, PATRULS_LOGIN_TABLE, PATRULS_STATUS_TABLE,
    PATRUL_TYPE, POLICE_TYPE, CAMERA_LIST, POLYGON_TYPE, POLYGON_ENTITY, VIOLATION_LIST_TYPE,

    // tables for Tasks
    FACECAR, EVENTCAR, EVENTFACE, EVENTBODY, FACEPERSON,
    ACTIVE_TASK, CARTOTALDATA, SELFEMPLOYMENT, TASK_TIMING_TABLE,

    // tables for ESCORT Entity
    ESCORT,
    COUNTRIES, TUPLE_OF_CAR, TUPLE_OF_ESCORT, POINTS_ENTITY, POLYGON_FOR_ESCORT,

    // tables for TRACKERS
    TRACKERS,
    TRACKERS_LOCATION_TABLE, TRACKERSID, TRACKER_FUEL_CONSUMPTION,

    // tables for TABLETS
    GPSTABLETS,
    TABLETS_LOCATION_TABLE, POSITION_INFO,

    // For Sos entity
    SOS_TABLE, STATUS_TYPE, PATRUL_STATUS_TYPE, PATRUL_SOS_TABLE, // <- после тестов удалить
    PATRUL_SOS_LIST, // <- хранит список всех сос сигналов которые принял патрульный
}
