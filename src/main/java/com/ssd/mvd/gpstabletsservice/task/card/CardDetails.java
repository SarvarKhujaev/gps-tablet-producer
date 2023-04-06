package com.ssd.mvd.gpstabletsservice.task.card;

import java.util.*;
import reactor.core.publisher.Flux;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.tuple.TupleOfCar;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.constants.Details;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;

@lombok.Data
public class CardDetails {
    private CarDetails carDetails;
    private PersonDetails personDetails;
    private Map< Details, List< Item > > details = new HashMap<>();

    public CardDetails ( CarDetails carDetails ) { this.setCarDetails( carDetails ); }

    public CardDetails ( CarTotalData carTotalData ) {
        this.getDetails().clear();
        this.getDetails().put( Details.TONIROVKA, new ArrayList<>() );
        this.getDetails().put( Details.ISHONCHNOMA, new ArrayList<>() );
        this.getDetails().put( Details.AVTO_SUGURTA, new ArrayList<>() );
        this.getDetails().put( Details.TEX_PASSPORT, new ArrayList<>() );
        this.getDetails().put( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR, new ArrayList<>() );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam().test( carTotalData.getDoverennostList() )
                && DataValidateInspector
                .getInstance()
                .getCheckList().test( carTotalData
                .getDoverennostList()
                .getDoverennostsList() ) )
            carTotalData
                    .getDoverennostList()
                    .getDoverennostsList()
                    .parallelStream()
                    .forEach( doverennost -> {
                    this.getDetails().get( Details.ISHONCHNOMA )
                            .add( new Item( "TOMONIDAN BERILGAN", doverennost.getIssuedBy() ) );
                    this.getDetails().get( Details.ISHONCHNOMA )
                            .add( new Item( "BERILGAN SANASI", doverennost.getDateBegin() ) );
                    this.getDetails().get( Details.ISHONCHNOMA )
                            .add( new Item( "TUGASH SANASI", doverennost.getDateValid() ) ); } );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam().test( carTotalData
                .getPsychologyCard()
                .getModelForCarList() )
                && DataValidateInspector
                .getInstance()
                .getCheckList().test( carTotalData
                .getPsychologyCard()
                .getModelForCarList()
                .getModelForCarList() ) ) carTotalData
                .getPsychologyCard()
                .getModelForCarList()
                .getModelForCarList()
                .parallelStream()
                .forEach( modelForCar -> {
                    this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                            .add( new Item( "DAVLAT RAQAM BELGISI", modelForCar.getPlateNumber() ) );
                    this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                            .add( new Item( "BERILGAN SANASI", modelForCar.getRegistrationDate() ) );
                    this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                            .add( new Item( "ISHLAB CHIQARILGAN SANASI", modelForCar.getYear() ) );
                    this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                            .add( new Item( "AVTOMOBIL RUSUMI/MODELI", modelForCar.getModel() ) );
                    this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                            .add( new Item( "AVTOMOBIL RANGI", modelForCar.getColor() ) );
                    this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                            .add( new Item( "KUZOV RAQAMI", modelForCar.getKuzov() ) );
                    this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                            .add( new Item( "TURI", modelForCar.getVehicleType() ) ); } );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam().test( carTotalData
                .getPsychologyCard()
                .getModelForPassport() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam().test( carTotalData
                .getPsychologyCard()
                .getModelForPassport()
                .getDocument() ) ) {
            this.getDetails()
                    .get( Details.TEX_PASSPORT )
                    .add( new Item( "Seriya va raqam", carTotalData
                            .getPsychologyCard()
                            .getModelForPassport()
                            .getDocument()
                            .getSerialNumber() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Avtomobil egasi", carTotalData
                    .getModelForCar()
                    .getPerson() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Avtomobil modeli", carTotalData
                    .getModelForCar()
                    .getModel() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Avtomobil rangi", carTotalData
                    .getModelForCar()
                    .getColor() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Berilgan sanasi", carTotalData
                    .getModelForCar()
                    .getRegistrationDate() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Avtomobil egasi", carTotalData
                    .getModelForCar()
                    .getPerson() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "STIR", carTotalData
                    .getModelForCar()
                    .getStir() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Organizatiya", carTotalData
                    .getModelForCar()
                    .getOrganization() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Turi", carTotalData
                    .getModelForCar()
                    .getVehicleType() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Manzil", carTotalData
                    .getModelForCar()
                    .getAddress() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Kuzov ragami", carTotalData
                    .getModelForCar()
                    .getKuzov() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Tola vazni", carTotalData
                    .getModelForCar()
                    .getFullWeight() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Yuksiz vazni", carTotalData
                    .getModelForCar()
                    .getEmptyWeight() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Dvigatel raqami", carTotalData
                    .getModelForCar()
                    .getEngine() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Dvigatel quvvati", carTotalData
                    .getModelForCar()
                    .getPower() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Yonilg'i turi", carTotalData
                    .getModelForCar()
                    .getFullWeight() ) );this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "Tola vazni", carTotalData
                    .getModelForCar()
                    .getFuelType() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "O'TIRADIGAN JOYLAR SONI", carTotalData
                    .getModelForCar()
                    .getSeats() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "TIK TURADIGAN JOYLAR SONI", carTotalData
                    .getModelForCar()
                    .getStands() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add( new Item( "ALOHIDA BELGILARI", carTotalData
                    .getModelForCar()
                    .getTexPassportSerialNumber() ) ); }

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam().test( carTotalData.getModelForCar().getTonirovka() ) ) {
            this.getDetails().get( Details.TONIROVKA ).add( new Item( "TURI", carTotalData
                    .getModelForCar()
                    .getTonirovka()
                    .getTintinType() ) );

            this.getDetails().get( Details.TONIROVKA ).add( new Item( "RUXSATNOMA BERILGAN SANASI", carTotalData
                    .getModelForCar()
                    .getTonirovka()
                    .getDateOfPermission() ) );

            this.getDetails().get( Details.TONIROVKA ).add( new Item( "RUXSATNOMA AMAL QILISH MUDDATI", carTotalData
                    .getModelForCar()
                    .getTonirovka()
                    .getDateOfValidotion() ) ); }

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam().test( carTotalData.getModelForCar().getInsurance() ) ) {
            this.getDetails().get( Details.AVTO_SUGURTA ).add( new Item( "BERILGAN VAQTI", carTotalData
                    .getModelForCar()
                    .getInsurance()
                    .getDateBegin() ) );

            this.getDetails().get( Details.AVTO_SUGURTA ).add( new Item( "MUDDAT TUGASH SANASI", carTotalData
                    .getModelForCar()
                    .getInsurance()
                    .getDateValid() ) );

            this.getDetails().get( Details.AVTO_SUGURTA ).add( new Item( "SUG'URTA RAQAMI", carTotalData
                    .getModelForCar()
                    .getInsurance()
                    .getInsuranceSerialNumber() ) ); } }

    public CardDetails ( PersonDetails personDetails ) { this.setPersonDetails( personDetails ); }

    public CardDetails ( Card card, Patrul patrul, String language ) {
        this.getDetails().clear();
        this.getDetails().put( Details.DETAILS, new ArrayList<>() );
        this.getDetails().put( Details.APPLICANT_DATA, new ArrayList<>() );
        this.getDetails().put( Details.DATA_OF_VICTIM, new ArrayList<>() );
        this.getDetails().put( Details.ADDRESS_OF_VICTIM, new ArrayList<>() );
        this.getDetails().put( Details.ADDITIONAL_ADDRESS, new ArrayList<>() );
        this.getDetails().put( Details.ADDRESS_OF_INCIDENT, new ArrayList<>() );
        this.getDetails().put( Details.ADDRESS_OF_APPLICANT, new ArrayList<>() );
        this.getDetails().put( Details.ADDITIONAL_ADDRESS_OF_Victim, new ArrayList<>() );
        this.getDetails().put( Details.ADDITIONAL_ADDRESS_OF_APPLICANT, new ArrayList<>() );

        Flux.fromStream( Arrays.stream( Details.values() ).sorted() )
                .subscribe( details -> {
                    switch ( details ) {
                        case DETAILS -> DataValidateInspector
                                .getInstance()
                                .getDetailsList()
                                .parallelStream()
                                .forEach( s -> {
                                    switch ( s ) {
                                        case "ID" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getCardId() ) );
                                        case "ФАБУЛА" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getFabula() ) );
                                        case "ШИРОТА" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getLongitude() ) );
                                        case "ДОЛГОТА" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getLatitude() ) );
                                        case "ВИД ПРОИСШЕСТВИЯ" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, "102 Task" ) );
                                        case "КОНЕЦ СОБЫТИЯ" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getEventEnd() ) );
                                        case "НАЧАЛО СОБЫТИЯ" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getEventStart() ) );
                                        case "ДАТА И ВРЕМЯ" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getCreated_date() ) );
                                        case "КОЛ.СТВО ПОГИБШИХ" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getDeadQuantity() ) );
                                        case "КОЛ.СТВО ПОСТРАДАВШИХ" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getVictimHumans().size() ) );
                                        case "ПОДРАЗДЕЛЕНИЕ" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, patrul.getPoliceType() ) );
                                        case "Ф.И.О" -> this.getDetails().get( Details.DETAILS )
                                                .add( new Item( s, card.getEventHuman().getFirstName() + " "
                                                        + card.getEventHuman().getLastName() + " "
                                                        + card.getEventHuman().getMiddleName() ) ); } } );

                        case APPLICANT_DATA -> {
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "Телефон", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getPhone()
                                            : Errors.DATA_NOT_FOUND.name()) );
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "Имя", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getFirstName()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "Поступил", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getCheckin()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "Больница", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getHospital()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "Отчество", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getMiddleName()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "Фамилия", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getLastName()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "ID Заявителя", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getHumanId()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "Отделение", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getHospitaldept()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "Тип лечения", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getTreatmentkind()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.APPLICANT_DATA )
                                    .add( new Item( "Кто звонил", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getFirstName()
                                            : Errors.DATA_NOT_FOUND.name()
                                            + " "
                                            + ( DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventHuman() )
                                            ? card.getEventHuman().getMiddleName()
                                            : Errors.DATA_NOT_FOUND.name() ) ) ); }

                        case DATA_OF_VICTIM -> {
                            this.getDetails().get( Details.DATA_OF_VICTIM )
                                    .add( new Item( "Телефон", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getVictimHumans() )
                                            && card.getVictimHumans().size() > 0
                                            ? card.getVictimHumans().get( 0 ).getPhone()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.DATA_OF_VICTIM )
                                    .add( new Item( "Имя", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getVictimHumans() )
                                            && card.getVictimHumans().size() > 0
                                            ? card.getVictimHumans()
                                            .get( 0 )
                                            .getFirstName()
                                            : Errors.DATA_NOT_FOUND.name()  ) );
                            this.getDetails().get( Details.DATA_OF_VICTIM )
                                    .add( new Item( "Отчество", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getVictimHumans() )
                                            && card.getVictimHumans().size() > 0
                                            ? card.getVictimHumans()
                                            .get( 0 )
                                            .getMiddleName()
                                            : Errors.DATA_NOT_FOUND.name()  ) );
                            this.getDetails().get( Details.DATA_OF_VICTIM )
                                    .add( new Item( "Фамилия", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getVictimHumans() )
                                            && card.getVictimHumans().size() > 0
                                            ? card.getVictimHumans()
                                            .get( 0 )
                                            .getLastName()
                                            : Errors.DATA_NOT_FOUND.name()  ) );
                            this.getDetails().get( Details.DATA_OF_VICTIM )
                                    .add( new Item( "ID Потерпевшего", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getVictimHumans() )
                                            && card.getVictimHumans().size() > 0
                                            ? card.getVictimHumans()
                                            .get( 0 )
                                            .getVictimId()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.DATA_OF_VICTIM )
                                    .add( new Item( "Дата рождения", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getVictimHumans() )
                                            && card.getVictimHumans().size() > 0
                                            ? card.getVictimHumans()
                                            .get( 0 )
                                            .getDateOfBirth()
                                            : Errors.DATA_NOT_FOUND.name() ) ); }

                        case ADDRESS_OF_VICTIM -> {
                            if ( DataValidateInspector
                                    .getInstance()
                                    .getCheckParam().test( card.getEventHuman() )
                                    && DataValidateInspector
                                    .getInstance()
                                    .getCheckParam().test( card.getEventHuman().getHumanAddress() ) ) {
                                this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                        .add( new Item( "Улица", card.getEventHuman().getHumanAddress().getStreet() ) );
                                this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                        .add( new Item( "Район", card.getEventHuman().getHumanAddress().getSRegionId() ) );
                                this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                        .add( new Item( "Область", card.getEventHuman().getHumanAddress().getSOblastiId() ) );
                                this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                        .add( new Item( "Страна", card.getEventHuman().getHumanAddress().getSCountriesId() ) );
                                this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                        .add( new Item( "Махалля", card.getEventHuman().getHumanAddress().getSMahallyaId() ) );
                                this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                        .add( new Item( "Населенныый пункт", card.getEventHuman().getHumanAddress().getSNote() ) ); } }

                        case ADDITIONAL_ADDRESS -> {
                            this.getDetails().get( Details.ADDITIONAL_ADDRESS )
                                    .add( new Item( "Дом", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventAddress() )
                                            ? card.getEventAddress().getFlat()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.ADDITIONAL_ADDRESS )
                                    .add( new Item( "Адрес", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventAddress() )
                                            ? card.getEventAddress().getStreet()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.ADDITIONAL_ADDRESS )
                                    .add( new Item( "Квартира", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventAddress() )
                                            ? card.getEventAddress().getHouse()
                                            : Errors.DATA_NOT_FOUND.name() ) ); }

                        case ADDRESS_OF_INCIDENT -> {
                            if ( DataValidateInspector
                                    .getInstance()
                                    .getCheckParam().test( card.getEventAddress() ) ) {
                                this.getDetails().get( Details.APPLICANT_DATA )
                                        .add( new Item( "Улица", card.getEventAddress().getStreet() ) );
                                this.getDetails().get( Details.APPLICANT_DATA )
                                        .add( new Item( "Страна", card.getEventAddress().getSRegionId() ) );
                                this.getDetails().get( Details.APPLICANT_DATA )
                                        .add( new Item( "Область", card.getEventAddress().getSOblastiId() ) );
                                this.getDetails().get( Details.APPLICANT_DATA )
                                        .add( new Item( "Район", card.getEventAddress().getSCountriesId() ) );
                                this.getDetails().get( Details.APPLICANT_DATA )
                                        .add( new Item( "Махалля", card.getEventAddress().getSMahallyaId() ) );
                                this.getDetails().get( Details.APPLICANT_DATA )
                                        .add( new Item( "Населенныый пункт", card.getEventAddress().getSNote() ) ); } }

                        case ADDRESS_OF_APPLICANT -> {
                            if ( DataValidateInspector
                                    .getInstance()
                                    .getCheckParam().test( card.getEventHuman() )
                                    && DataValidateInspector
                                    .getInstance()
                                    .getCheckParam().test( card.getEventHuman().getHumanAddress() ) ) {
                                this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                        .add( new Item( "Улица", card.getEventHuman().getHumanAddress().getStreet() ) );
                                this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                        .add( new Item( "Район", card.getEventHuman().getHumanAddress().getSRegionId() ) );
                                this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                        .add( new Item( "Область", card.getEventHuman().getHumanAddress().getSOblastiId() ) );
                                this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                        .add( new Item( "Страна", card.getEventHuman().getHumanAddress().getSCountriesId() ) );
                                this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                        .add( new Item( "Махалля", card.getEventHuman().getHumanAddress().getSMahallyaId() ) );
                                this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                        .add( new Item( "Населенныый пункт", card.getEventHuman().getHumanAddress().getSNote() ) ); } }

                        case ADDITIONAL_ADDRESS_OF_Victim -> {
                            if ( DataValidateInspector
                                    .getInstance()
                                    .getCheckParam().test( card.getVictimHumans() )
                                    && DataValidateInspector
                                    .getInstance()
                                    .getCheckList().test( card.getVictimHumans() )
                                    && DataValidateInspector
                                    .getInstance()
                                    .getCheckParam().test(
                                            card.getVictimHumans()
                                                    .get( 0 )
                                                    .getVictimAddress() ) ) {
                                this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_Victim )
                                        .add( new Item( "Дом", card.getVictimHumans()
                                                .get( 0 )
                                                .getVictimAddress()
                                                .getFlat() ) );
                                this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_Victim )
                                        .add( new Item( "Адрес", card.getVictimHumans()
                                                .get( 0 )
                                                .getVictimAddress()
                                                .getHouse() ) );
                                this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_Victim )
                                        .add( new Item( "Квартира", card.getVictimHumans()
                                                .get( 0 )
                                                .getVictimAddress()
                                                .getStreet() ) ); } }

                        case ADDITIONAL_ADDRESS_OF_APPLICANT -> {
                            this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_APPLICANT )
                                    .add( new Item( "Дом", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventAddress() )
                                            ? card.getEventAddress().getFlat()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_APPLICANT )
                                    .add( new Item( "Адрес", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventAddress() )
                                            ? card.getEventAddress().getHouse()
                                            : Errors.DATA_NOT_FOUND.name() ) );
                            this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_APPLICANT )
                                    .add( new Item( "Квартира", DataValidateInspector
                                            .getInstance()
                                            .getCheckParam().test( card.getEventAddress() )
                                            ? card.getEventAddress().getStreet()
                                            : Errors.DATA_NOT_FOUND.name() ) ); } } } ); }

    public CardDetails ( EscortTuple escortTuple, String ru, TupleOfCar tupleOfCar ) {
        this.getDetails().putIfAbsent( Details.ESCORT, new ArrayList<>() );
        this.getDetails().putIfAbsent( Details.ESCORT_CAR, new ArrayList<>() );
        this.getDetails().get( Details.ESCORT )
                .add( new Item( "ID", escortTuple.getUuid() ) );
        this.getDetails().get( Details.ESCORT )
                .add( new Item( "Страна", escortTuple.getCountries() ) );
        this.getDetails().get( Details.ESCORT ).add( new Item( "Полигон", escortTuple.getUuidOfPolygon() ) );

        this.getDetails().get( Details.ESCORT_CAR )
                .add( new Item( "Гос. номер машины", tupleOfCar.getGosNumber() ) );
        this.getDetails().get( Details.ESCORT_CAR )
                .add( new Item( "Тип порученной машины", tupleOfCar.getCarModel() ) );
        this.getDetails().get( Details.ESCORT_CAR )
                .add( new Item( "Широта локации машины", tupleOfCar.getLatitude() ) );
        this.getDetails().get( Details.ESCORT_CAR )
                .add( new Item( "Долгота локации машины", tupleOfCar.getLongitude() ) ); }

    public CardDetails ( SelfEmploymentTask selfEmploymentTask, String language, Patrul patrul ) {
        this.getDetails().clear();
        this.getDetails().put( Details.SELF_EMPLOYMENT, new ArrayList<>() );
        Flux.fromStream( Arrays.stream( Details.values() ).sorted() )
                .subscribe( details -> {
            switch ( details ) {
                case NUMBER -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "№", selfEmploymentTask.getUuid() ) );
                case ADDRESS -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "Адрес", selfEmploymentTask.getAddress() ) );
                case DESCRIPTION -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "Описание", selfEmploymentTask.getDescription() ) );
                case ACCEPTED_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "Принятое время", selfEmploymentTask.getIncidentDate() ) );
                case REPORT_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "Время отправки рапорта", selfEmploymentTask.getIncidentDate() ) );
                case ACCEPTED_POINT_LATITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "Принятая точка", selfEmploymentTask.getLatOfPatrul() ) );
                case ARRIVED_POINT_LATITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "Точка прибытия", selfEmploymentTask.getLatOfAccident() ) );
                case ACCEPTED_POINT_LONGITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "Принятая точка", selfEmploymentTask.getLanOfPatrul() ) );
                case ARRIVED_POINT_LONGITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "Точка прибытия", selfEmploymentTask.getLanOfAccident() ) );
                case ARRIVED_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                        .add( new Item( "Время прибытия", selfEmploymentTask
                                .getPatruls()
                                .get( patrul.getUuid() )
                                .getTaskDate() ) ); } } ); }
}
