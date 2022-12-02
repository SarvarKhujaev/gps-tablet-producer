package com.ssd.mvd.gpstabletsservice.constants;

public enum Status {
    LOGIN, LOGOUT,
    START_TO_WORK, STOP_TO_WORK,
    SET_IN_PAUSE, RETURNED_TO_WORK,

    AVAILABLE, NOT_AVAILABLE,
    CREATED, IN_GARAGE, CANCEL,
    ACCEPTED, LATE, IN_TIME,
    FREE, ARRIVED, ATTACHED, FINISHED,

    ACTIVE, IN_ACTIVE,

    OPTIONAL, // нужно обновить приложение по выбору
    FORCE, // нужно принудительно обновить приложение
    LAST, // последняя версия установлена
}
