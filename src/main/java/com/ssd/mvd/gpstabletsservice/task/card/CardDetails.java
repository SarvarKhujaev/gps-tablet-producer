package com.ssd.mvd.gpstabletsservice.task.card;

import java.util.*;
import lombok.Data;
import reactor.core.publisher.Flux;

import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.constants.Details;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;

@Data
public class CardDetails {
    private Map< Details, List< Item > > details = new HashMap<>();

    public CardDetails ( SelfEmploymentTask selfEmploymentTask, String language, String passportSeries ) {
        this.getDetails().putIfAbsent( Details.SELF_EMPLOYMENT, new ArrayList<>() );
        Flux.fromStream( Arrays.stream( Details.values() ).sorted() ).subscribe( details -> {
            switch ( details ) {
                case NUMBER -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "№", selfEmploymentTask.getUuid() ) );
                case ADDRESS -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Адрес", selfEmploymentTask.getAddress() ) );
                case DESCRIPTION -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Описание", selfEmploymentTask.getDescription() ) );
                case ACCEPTED_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Принятое время", selfEmploymentTask.getIncidentDate() ) );
                case REPORT_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Время отрпавки рапорта", selfEmploymentTask.getIncidentDate() ) );
                case ACCEPTED_POINT_LATITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Принятая точка", selfEmploymentTask.getLatOfPatrul() ) );
                case ARRIVED_POINT_LATITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Точка прибытия", selfEmploymentTask.getLatOfAccident() ) );
                case ACCEPTED_POINT_LONGITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Принятая точка", selfEmploymentTask.getLanOfPatrul() ) );
                case ARRIVED_POINT_LONGITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Точка прибытия", selfEmploymentTask.getLanOfAccident() ) );
                case ARRIVED_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Время прибытия", selfEmploymentTask.getPatruls().get( passportSeries ).getTaskDate() ) );
                case REPORT -> Flux.fromStream( selfEmploymentTask.getReportForCards().stream() )
                        .filter( reportForCard -> reportForCard.getPassportSeries().equals( passportSeries ) )
                        .subscribe( reportForCard -> {
                            this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Отчет", reportForCard ) );
                            this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Время отчета", reportForCard ) ); } ); } } ); }

    public CardDetails ( EventBody eventBody ) {
        this.getDetails().putIfAbsent( Details.FIND_FACE_EVENT_BODY, new ArrayList<>() );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "F.I.O",
                eventBody.getPsychologyCard().getModelForPassport().getPerson().getNameLatin() + " "
               + eventBody.getPsychologyCard().getModelForPassport().getPerson().getSurnameLatin() + " "
                        + eventBody.getPsychologyCard().getModelForPassport().getPerson().getPatronymLatin() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Pasport Seriyasi",
                eventBody.getPsychologyCard().getModelForPassport().getDocument().getSerialNumber() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Ip", eventBody.getCameraIp() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Image", eventBody.getFullframe() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Sana", eventBody.getCreated_date() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "O'XSHASHLIGI: ", eventBody.getConfidence() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Vaqt", eventBody.getCreated_date().getTime() ) ); }

    public CardDetails ( EventFace eventBody ) {
        this.getDetails().putIfAbsent( Details.FIND_FACE_EVENT_BODY, new ArrayList<>() );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "F.I.O",
                eventBody.getPsychologyCard().getModelForPassport().getPerson().getNameLatin() + " "
               + eventBody.getPsychologyCard().getModelForPassport().getPerson().getSurnameLatin() + " "
                        + eventBody.getPsychologyCard().getModelForPassport().getPerson().getPatronymLatin() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Pasport Seriyasi",
                eventBody.getPsychologyCard().getModelForPassport().getDocument().getSerialNumber() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Ip", eventBody.getCameraIp() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Image", eventBody.getFullframe() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Sana", eventBody.getCreated_date() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "O'XSHASHLIGI: ", eventBody.getConfidence() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Vaqt", eventBody.getCreated_date().getTime() ) ); }

    public CardDetails ( EventCar eventBody ) {
        this.getDetails().putIfAbsent( Details.FIND_FACE_EVENT_BODY, new ArrayList<>() );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "F.I.O",
                eventBody.getCarTotalData().getModelForCar().getModel() + ", "
               + eventBody.getCarTotalData().getModelForCar().getKuzov() + ", RANG: "
                        + eventBody.getCarTotalData().getModelForCar().getColor() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Номер машины до угона: ",
                eventBody.getCarTotalData().getModelForCar().getPlateNumber() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Ip", eventBody.getCameraIp() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Image", eventBody.getFullframe() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Sana", eventBody.getCreated_date() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "O'XSHASHLIGI: ", eventBody.getConfidence() ) );
        this.getDetails().get( Details.FIND_FACE_EVENT_BODY ).add( new Item( "Vaqt", eventBody.getCreated_date().getTime() ) ); }

    public CardDetails ( Card card, String language ) {
        this.getDetails().putIfAbsent( Details.DETAILS, new ArrayList<>() );
        this.getDetails().putIfAbsent( Details.APPLICANT_DATA, new ArrayList<>() );
        this.getDetails().putIfAbsent( Details.DATA_OF_VICTIM, new ArrayList<>() );
        this.getDetails().putIfAbsent( Details.ADDRESS_OF_VICTIM, new ArrayList<>() );
        this.getDetails().putIfAbsent( Details.ADDITIONAL_ADDRESS, new ArrayList<>() );
        this.getDetails().putIfAbsent( Details.ADDRESS_OF_INCIDENT, new ArrayList<>() );
        this.getDetails().putIfAbsent( Details.ADDRESS_OF_APPLICANT, new ArrayList<>() );
        this.getDetails().putIfAbsent( Details.ADDITIONAL_ADDRESS_OF_Victim, new ArrayList<>() );
        this.getDetails().putIfAbsent( Details.ADDITIONAL_ADDRESS_OF_APPLICANT, new ArrayList<>() );

        Flux.fromStream( Arrays.stream( Details.values() ).sorted() ).subscribe( details -> {
            switch ( details ) {
                case DETAILS -> Archive.getAchieve().getDetailsList().forEach(s -> {
                    switch ( s ) {
                        case  "ID" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getCardId() ) );
                        case  "ФАБУЛА" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getFabula() ) );
                        case  "ШИРОТА" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getLongitude() ) );
                        case  "ДОЛГОТА" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getLatitude() ) );
                        case  "ВИД ПРОИСШЕСТВИЯ" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, "102 Task" ) );
                        case  "КОНЕЦ СОБЫТИЯ" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getEventEnd() ) );
                        case  "НАЧАЛО СОБЫТИЯ" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getEventStart() ) );
                        case  "ДАТА И ВРЕМЯ" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getCreated_date() ) );
                        case  "КОЛ.СТВО ПОШИБЩИХ" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getDeadQuantity() ) );
                        case  "КОЛ.СТВО ПОСТРАДАВШИХ" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getVictimHumans().size() ) );
//                        case  "ПОДРАЗДЕЛЕНИЕ" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getPatruls().get(0).getPoliceType() ) );
                        case  "Ф.И.О" -> this.getDetails().get( Details.DETAILS ).add( new Item( s, card.getEventHuman().getFirstName() + " " + card.getEventHuman().getLastName() + " " + card.getEventHuman().getMiddleName() ) ); } } );

                case ADDRESS_OF_INCIDENT -> {
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Улица", card.getEventAddress().getStreet() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Страна", card.getEventAddress().getSRegionId() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Область", card.getEventAddress().getSOblastiId() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Район", card.getEventAddress().getSCountriesId() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Махалля", card.getEventAddress().getSMahallyaId() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Населенныый пункт", card.getEventAddress().getSNote() ) ); }

                case APPLICANT_DATA -> {
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Телефон", card.getEventHuman().getPhone() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Имя", card.getEventHuman().getFirstName() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Поступил", card.getEventHuman().getCheckin() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Больница", card.getEventHuman().getHospital() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Отчество", card.getEventHuman().getLastName() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Фамилия", card.getEventHuman().getMiddleName() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "ID Заявителя", card.getEventHuman().getHumanId() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Отделение", card.getEventHuman().getHospitaldept() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Тип лечения", card.getEventHuman().getTreatmentkind() ) );
                    this.getDetails().get( Details.APPLICANT_DATA ).add( new Item( "Кто звонил", card.getEventHuman().getFirstName() + " " + card.getEventHuman().getMiddleName() ) ); }

                case ADDITIONAL_ADDRESS -> {
                    this.getDetails().get( Details.ADDITIONAL_ADDRESS ).add( new Item( "Дом", card.getEventAddress().getFlat() ) );
                    this.getDetails().get( Details.ADDITIONAL_ADDRESS ).add( new Item( "Адрес", card.getEventAddress().getStreet() ) );
                    this.getDetails().get( Details.ADDITIONAL_ADDRESS ).add( new Item( "Квартира", card.getEventAddress().getHouse() ) ); }

                case ADDRESS_OF_APPLICANT -> {
                    this.getDetails().get( Details.ADDRESS_OF_APPLICANT ).add( new Item( "Улица", card.getEventHuman().getHumanAddress().getStreet() ) );
                    this.getDetails().get( Details.ADDRESS_OF_APPLICANT ).add( new Item( "Район", card.getEventHuman().getHumanAddress().getSRegionId() ) );
                    this.getDetails().get( Details.ADDRESS_OF_APPLICANT ).add( new Item( "Область", card.getEventHuman().getHumanAddress().getSOblastiId() ) );
                    this.getDetails().get( Details.ADDRESS_OF_APPLICANT ).add( new Item( "Страна", card.getEventHuman().getHumanAddress().getSCountriesId() ) );
                    this.getDetails().get( Details.ADDRESS_OF_APPLICANT ).add( new Item( "Махалля", card.getEventHuman().getHumanAddress().getSMahallyaId() ) );
                    this.getDetails().get( Details.ADDRESS_OF_APPLICANT ).add( new Item( "Населенныый пункт", card.getEventHuman().getHumanAddress().getSNote() ) ); }

                case ADDITIONAL_ADDRESS_OF_APPLICANT -> {
                    this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_APPLICANT ).add( new Item( "Дом", card.getEventAddress().getFlat() ) );
                    this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_APPLICANT ).add( new Item( "Адрес", card.getEventAddress().getHouse() ) );
                    this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_APPLICANT ).add( new Item( "Квартира", card.getEventAddress().getStreet() ) ); }

                case DATA_OF_VICTIM -> {
                    this.getDetails().get( Details.DATA_OF_VICTIM ).add( new Item( "Телефон", card.getVictimHumans().get( 0 ).getPhone() ) );
                    this.getDetails().get( Details.DATA_OF_VICTIM ).add( new Item( "Имя", card.getVictimHumans().get( 0 ).getFirstName() ) );
                    this.getDetails().get( Details.DATA_OF_VICTIM ).add( new Item( "Отчество", card.getVictimHumans().get( 0 ).getLastName() ) );
                    this.getDetails().get( Details.DATA_OF_VICTIM ).add( new Item( "Фамилия", card.getVictimHumans().get( 0 ).getMiddleName() ) );
                    this.getDetails().get( Details.DATA_OF_VICTIM ).add( new Item( "ID Потерпевшего", card.getVictimHumans().get( 0 ).getVictimId() ) );
                    this.getDetails().get( Details.DATA_OF_VICTIM ).add( new Item( "Дата рождения", card.getVictimHumans().get( 0 ).getDateOfBirth() ) ); }

                case ADDRESS_OF_VICTIM -> {
                    this.getDetails().get( Details.ADDRESS_OF_VICTIM ).add( new Item( "Улица", card.getEventHuman().getHumanAddress().getStreet() ) );
                    this.getDetails().get( Details.ADDRESS_OF_VICTIM ).add( new Item( "Район", card.getEventHuman().getHumanAddress().getSRegionId() ) );
                    this.getDetails().get( Details.ADDRESS_OF_VICTIM ).add( new Item( "Область", card.getEventHuman().getHumanAddress().getSOblastiId() ) );
                    this.getDetails().get( Details.ADDRESS_OF_VICTIM ).add( new Item( "Страна", card.getEventHuman().getHumanAddress().getSCountriesId() ) );
                    this.getDetails().get( Details.ADDRESS_OF_VICTIM ).add( new Item( "Махалля", card.getEventHuman().getHumanAddress().getSMahallyaId() ) );
                    this.getDetails().get( Details.ADDRESS_OF_VICTIM ).add( new Item( "Населенныый пункт", card.getEventHuman().getHumanAddress().getSNote() ) ); }

                case ADDITIONAL_ADDRESS_OF_Victim -> {
                    this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_Victim ).add( new Item( "Дом", card.getVictimHumans().get( 0 ).getVictimAddress().getFlat() ) );
                    this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_Victim ).add( new Item( "Адрес", card.getVictimHumans().get( 0 ).getVictimAddress().getHouse() ) );
                    this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_Victim ).add( new Item( "Квартира", card.getVictimHumans().get( 0 ).getVictimAddress().getStreet() ) ); } } } );
    }
}
