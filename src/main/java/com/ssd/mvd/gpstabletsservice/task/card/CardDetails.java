package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.constants.Details;

import reactor.core.publisher.Flux;
import java.util.*;
import lombok.Data;

@Data
public class CardDetails {
    private Map< Details, List< Item > > details = new HashMap<>();
    private final List< String > selfEmploymentList = List.of( "№", "Принятое время", "Принятая точка", "Принятая точка", "Время прибытия", "Точка прибытия", "Точка прибытия", "Отчет", "Время отчета", "Описание", "Адрес" );
    private final List< String > detailsList = List.of( "Ф.И.О", "", "ПОДРАЗДЕЛЕНИЕ", "ДАТА И ВРЕМЯ", "ID", "ШИРОТА", "ДОЛГОТА", "ВИД ПРОИСШЕСТВИЯ", "НАЧАЛО СОБЫТИЯ", "КОНЕЦ СОБЫТИЯ", "КОЛ.СТВО ПОСТРАДАВШИХ", "КОЛ.СТВО ПОШИБЩИХ", "ФАБУЛА" );

    public CardDetails ( SelfEmploymentTask selfEmploymentTask, String language, String passportSeries ) {
        this.getDetails().putIfAbsent( Details.SELF_EMPLOYMENT, new ArrayList<>() );
        Flux.fromStream( Arrays.stream( Details.values() ).sorted() ).subscribe( details -> {
            switch ( details ) {
                case NUMBER -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "№", selfEmploymentTask.getUuid() ) );
                case ADDRESS -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Адрес", selfEmploymentTask.getIncidentDate() ) );
                case DESCRIPTION -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Описание", selfEmploymentTask.getDescription() ) );
                case ARRIVED_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Время прибытия", selfEmploymentTask.getArrivedTime() ) );
                case ACCEPTED_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Принятое время", selfEmploymentTask.getIncidentDate() ) );
                case REPORT_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Время отрпавки рапорта", selfEmploymentTask.getIncidentDate() ) );
                case ACCEPTED_POINT_LATITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Принятая точка", selfEmploymentTask.getLatOfPatrul() ) );
                case ARRIVED_POINT_LATITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Точка прибытия", selfEmploymentTask.getLatOfAccident() ) );
                case ACCEPTED_POINT_LONGITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Принятая точка", selfEmploymentTask.getLanOfPatrul() ) );
                case ARRIVED_POINT_LONGITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Точка прибытия", selfEmploymentTask.getLanOfAccident() ) );
                case REPORT -> Flux.fromStream( selfEmploymentTask.getReportForCards().stream() ).filter( reportForCard -> reportForCard.getPassportSeries().equals( passportSeries ) ).subscribe( reportForCard -> {
                    this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Отчет", reportForCard ) );
                    this.getDetails().get( Details.SELF_EMPLOYMENT ).add( new Item( "Время отчета", reportForCard ) ); } ); } } ); }

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
                case DETAILS -> this.getDetailsList().forEach( s -> {
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
