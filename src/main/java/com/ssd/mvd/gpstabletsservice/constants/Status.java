package com.ssd.mvd.gpstabletsservice.constants;

public enum Status {
    LOGIN,
    LOGOUT {
        @Override
        public boolean isLogout() {
            return true;
        }
    },
    START_TO_WORK {
        @Override
        public boolean isStartToWork() {
            return true;
        }
    },
    STOP_TO_WORK,
    SET_IN_PAUSE,
    RETURNED_TO_WORK,

    CREATED {
        @Override
        public boolean isCreated() {
            return true;
        }
    },
    CANCEL {
        @Override
        public boolean isCanceled() {
            return true;
        }
    },
    ACCEPTED {
        @Override
        public boolean isAccepted() {
            return true;
        }
    },
    LATE,
    IN_TIME,
    FREE,
    ARRIVED {
        @Override
        public boolean isArrived() {
            return true;
        }
    },
    ATTACHED,
    FINISHED {
        @Override
        public boolean isFinished () {
            return true;
        }
    },

    ACTIVE {
        @Override
        public boolean isActive() {
            return true;
        }
    },
    IN_ACTIVE,

    OPTIONAL, // нужно обновить приложение по выбору
    FORCE, // нужно принудительно обновить приложение

    LAST; // последняя версия установлена

    public boolean isCanceled() {
        return false;
    };

    public boolean isFinished () { return false; }

    public boolean isLogout () { return false; }

    public boolean isStartToWork () { return false; }

    public boolean isArrived () { return false; }

    public boolean isAccepted () { return false; }

    public boolean isActive () { return false; }

    public boolean isCreated () { return false; }
}
