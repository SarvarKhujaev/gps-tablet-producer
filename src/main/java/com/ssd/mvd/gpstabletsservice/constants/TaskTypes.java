package com.ssd.mvd.gpstabletsservice.constants;

public enum TaskTypes {
    CARD_102,
    SELF_EMPLOYMENT,
    FREE {
        @Override
        public boolean isFree () {
            return true;
        }
    },
    ESCORT,
    ACTIVE_TASK,
    CARD_DETAILS,

    FIND_FACE_PERSON, FIND_FACE_CAR, // FOR TASKS FROM ASSOMIDDIN
    FIND_FACE_EVENT_BODY, FIND_FACE_EVENT_CAR, FIND_FACE_EVENT_FACE; // FOR TASKS FROM SHAMSIDDIN

    public boolean isFree () {
        return false;
    }
}
