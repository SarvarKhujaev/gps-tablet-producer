package com.ssd.mvd.gpstabletsservice.entity.responseForAndroid;

import java.util.*;
import reactor.core.publisher.Flux;

import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.task.card.Item;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.constants.Details;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.subscribers.CustomSubscriber;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;

public final class CardDetails extends DataValidateInspector {
    public void setCarDetails ( final CarDetails carDetails ) {
        this.carDetails = carDetails;
    }

    public void setPersonDetails ( final PersonDetails personDetails ) {
        this.personDetails = personDetails;
    }

    public Map< Details, List< Item > > getDetails() {
        return this.details;
    }

    private CarDetails carDetails;
    private PersonDetails personDetails;
    private final Map< Details, List< Item > > details = super.newMap();

    public static <T> CardDetails from ( final T object ) {
        if ( object instanceof CarDetails ) {
            return new CardDetails( (CarDetails) object );
        }
        else {
            return new CardDetails( (PersonDetails) object );
        }
    }

    private CardDetails ( final CarDetails carDetails ) {
        this.setCarDetails( carDetails );
    }

    private CardDetails ( final PersonDetails personDetails ) {
        this.setPersonDetails( personDetails );
    }

    public CardDetails ( final CarTotalData carTotalData ) {
        this.getDetails().clear();
        this.getDetails().put( Details.TONIROVKA, super.newList() );
        this.getDetails().put( Details.ISHONCHNOMA, super.newList() );
        this.getDetails().put( Details.AVTO_SUGURTA, super.newList() );
        this.getDetails().put( Details.TEX_PASSPORT, super.newList() );
        this.getDetails().put( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR, super.newList() );

        if ( super.objectIsNotNull( carTotalData.getDoverennostList() )
                && super.isCollectionNotEmpty(
                        carTotalData
                                .getDoverennostList()
                                .getDoverennostsList() ) ) {
            super.analyze(
                    carTotalData
                            .getDoverennostList()
                            .getDoverennostsList(),
                    doverennost -> {
                        this.getDetails().get( Details.ISHONCHNOMA )
                                .add( Item.generate( "TOMONIDAN BERILGAN", doverennost.getIssuedBy() ) );
                        this.getDetails().get( Details.ISHONCHNOMA )
                                .add( Item.generate( "BERILGAN SANASI", doverennost.getDateBegin() ) );
                        this.getDetails().get( Details.ISHONCHNOMA )
                                .add( Item.generate( "TUGASH SANASI", doverennost.getDateValid() ) );
                    }
            );
        }

        if ( super.objectIsNotNull(
                carTotalData
                .getPsychologyCard()
                .getModelForCarList() )
                && super.isCollectionNotEmpty(
                        carTotalData
                                .getPsychologyCard()
                                .getModelForCarList()
                                .getModelForCarList() ) ) {
            super.analyze(
                    carTotalData
                            .getPsychologyCard()
                            .getModelForCarList()
                            .getModelForCarList(),
                    modelForCar -> {
                        this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                                .add( Item.generate( "DAVLAT RAQAM BELGISI", modelForCar.getPlateNumber() ) );
                        this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                                .add( Item.generate( "BERILGAN SANASI", modelForCar.getRegistrationDate() ) );
                        this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                                .add( Item.generate( "ISHLAB CHIQARILGAN SANASI", modelForCar.getYear() ) );
                        this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                                .add( Item.generate( "AVTOMOBIL RUSUMI/MODELI", modelForCar.getModel() ) );
                        this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                                .add( Item.generate( "AVTOMOBIL RANGI", modelForCar.getColor() ) );
                        this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                                .add( Item.generate( "KUZOV RAQAMI", modelForCar.getKuzov() ) );
                        this.getDetails().get( Details.NOMIDAGI_MAVJUD_TRANSPORT_VOSITALAR )
                                .add( Item.generate( "TURI", modelForCar.getVehicleType() ) );
                    }
            );
        }

        if ( super.objectIsNotNull(
                carTotalData
                .getPsychologyCard()
                .getModelForPassport() )
                && super.objectIsNotNull(
                        carTotalData
                            .getPsychologyCard()
                            .getModelForPassport()
                            .getDocument() ) ) {
            this.getDetails()
                    .get( Details.TEX_PASSPORT )
                    .add( Item.generate( "Seriya va raqam", carTotalData
                            .getPsychologyCard()
                            .getModelForPassport()
                            .getDocument()
                            .getSerialNumber() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Avtomobil egasi", carTotalData.getModelForCar().getPerson() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Avtomobil modeli", carTotalData.getModelForCar().getModel() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Avtomobil rangi", carTotalData.getModelForCar().getColor() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Berilgan sanasi", carTotalData.getModelForCar().getRegistrationDate() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Avtomobil egasi", carTotalData.getModelForCar().getPerson() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "STIR", carTotalData.getModelForCar().getStir() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Organizatiya", carTotalData.getModelForCar().getOrganization() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Turi", carTotalData.getModelForCar().getVehicleType() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Manzil", carTotalData.getModelForCar().getAddress() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Kuzov ragami", carTotalData.getModelForCar().getKuzov() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Tola vazni", carTotalData.getModelForCar().getFullWeight() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Yuksiz vazni", carTotalData.getModelForCar().getEmptyWeight() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Dvigatel raqami", carTotalData.getModelForCar().getEngine() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Dvigatel quvvati", carTotalData.getModelForCar().getPower() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "Yonilg'i turi", carTotalData.getModelForCar().getFullWeight() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                            Item.generate( "Tola vazni", carTotalData.getModelForCar().getFuelType() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "O'TIRADIGAN JOYLAR SONI",
                    carTotalData.getModelForCar().getSeats() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "TIK TURADIGAN JOYLAR SONI",
                    carTotalData.getModelForCar().getStands() ) );

            this.getDetails().get( Details.TEX_PASSPORT ).add(
                    Item.generate( "ALOHIDA BELGILARI",
                    carTotalData.getModelForCar().getTexPassportSerialNumber() ) );
        }

        if ( super.objectIsNotNull( carTotalData.getModelForCar().getTonirovka() ) ) {
            this.getDetails().get( Details.TONIROVKA ).add(
                    Item.generate( "TURI",
                            carTotalData
                            .getModelForCar()
                            .getTonirovka()
                            .getTintinType() ) );

            this.getDetails().get( Details.TONIROVKA ).add(
                    Item.generate( "RUXSATNOMA BERILGAN SANASI",
                            carTotalData
                            .getModelForCar()
                            .getTonirovka()
                            .getDateOfPermission() ) );

            this.getDetails().get( Details.TONIROVKA ).add(
                    Item.generate( "RUXSATNOMA AMAL QILISH MUDDATI",
                            carTotalData
                            .getModelForCar()
                            .getTonirovka()
                            .getDateOfValidotion() ) );
        }

        if ( super.objectIsNotNull( carTotalData.getModelForCar().getInsurance() ) ) {
            this.getDetails().get( Details.AVTO_SUGURTA ).add(
                    Item.generate( "BERILGAN VAQTI", carTotalData
                    .getModelForCar()
                    .getInsurance()
                    .getDateBegin() ) );

            this.getDetails().get( Details.AVTO_SUGURTA ).add(
                    Item.generate( "MUDDAT TUGASH SANASI", carTotalData
                    .getModelForCar()
                    .getInsurance()
                    .getDateValid() ) );

            this.getDetails().get( Details.AVTO_SUGURTA ).add(
                    Item.generate( "SUG'URTA RAQAMI", carTotalData
                    .getModelForCar()
                    .getInsurance()
                    .getInsuranceSerialNumber() ) );
        }
    }

    public CardDetails (
            final Card card,
            final Patrul patrul,
            final String language ) {
        this.getDetails().clear();
        this.getDetails().put( Details.DETAILS, super.newList() );
        this.getDetails().put( Details.APPLICANT_DATA, super.newList() );
        this.getDetails().put( Details.DATA_OF_VICTIM, super.newList() );
        this.getDetails().put( Details.ADDRESS_OF_VICTIM, super.newList() );
        this.getDetails().put( Details.ADDITIONAL_ADDRESS, super.newList() );
        this.getDetails().put( Details.ADDRESS_OF_INCIDENT, super.newList() );
        this.getDetails().put( Details.ADDRESS_OF_APPLICANT, super.newList() );
        this.getDetails().put( Details.ADDITIONAL_ADDRESS_OF_Victim, super.newList() );
        this.getDetails().put( Details.ADDITIONAL_ADDRESS_OF_APPLICANT, super.newList() );

        Flux.fromStream( Arrays.stream( Details.values() ).sorted() )
                .subscribe( new CustomSubscriber<>(
                        details -> {
                            switch ( details ) {
                                case DETAILS -> super.analyze(
                                        super.detailsList,
                                        s -> {
                                            switch ( s ) {
                                                case "ID" -> this.getDetails().get( Details.DETAILS )
                                                    .add( Item.generate( s, card.getCardId() ) );
                                                case "ФАБУЛА" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, card.getFabula() ) );
                                                case "ШИРОТА" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, card.getLongitude() ) );
                                                case "ДОЛГОТА" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, card.getLatitude() ) );
                                                case "ВИД ПРОИСШЕСТВИЯ" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, "102 Task" ) );
                                                case "КОНЕЦ СОБЫТИЯ" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, card.getEventEnd() ) );
                                                case "НАЧАЛО СОБЫТИЯ" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, card.getEventStart() ) );
                                                case "ДАТА И ВРЕМЯ" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, card.getCreated_date() ) );
                                                case "КОЛ.СТВО ПОГИБШИХ" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, card.getDeadQuantity() ) );
                                                case "КОЛ.СТВО ПОСТРАДАВШИХ" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, card.getVictimHumans().size() ) );
                                                case "ПОДРАЗДЕЛЕНИЕ" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, patrul.getPoliceType() ) );
                                                case "Ф.И.О" -> this.getDetails().get( Details.DETAILS )
                                                        .add( Item.generate( s, card.getEventHuman().getFirstName() + " "
                                                                + card.getEventHuman().getLastName() + " "
                                                                + card.getEventHuman().getMiddleName() ) );
                                            }
                                        }
                                );

                                case APPLICANT_DATA -> {
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "Телефон", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getPhone()
                                                    : Errors.DATA_NOT_FOUND.name()) );
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "Имя", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getFirstName()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "Поступил", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getCheckin()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "Больница", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getHospital()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "Отчество", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getMiddleName()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "Фамилия", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getLastName()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "ID Заявителя", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getHumanId()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "Отделение", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getHospitaldept()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "Тип лечения", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getTreatmentkind()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.APPLICANT_DATA )
                                            .add( Item.generate( "Кто звонил", super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getFirstName()
                                                    : Errors.DATA_NOT_FOUND.name()
                                                    + " "
                                                    + ( super
                                                    .objectIsNotNull( card.getEventHuman() )
                                                    ? card.getEventHuman().getMiddleName()
                                                    : Errors.DATA_NOT_FOUND.name() ) ) );
                                }

                                case DATA_OF_VICTIM -> {
                                    this.getDetails().get( Details.DATA_OF_VICTIM )
                                            .add( Item.generate( "Телефон", super
                                                    .objectIsNotNull( card.getVictimHumans() )
                                                    && card.getVictimHumans().isEmpty()
                                                    ? card.getVictimHumans().get( 0 ).getPhone()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.DATA_OF_VICTIM )
                                            .add( Item.generate( "Имя", super
                                                    .objectIsNotNull( card.getVictimHumans() )
                                                    && card.getVictimHumans().isEmpty()
                                                    ? card.getVictimHumans()
                                                    .get( 0 )
                                                    .getFirstName()
                                                    : Errors.DATA_NOT_FOUND.name()  ) );
                                    this.getDetails().get( Details.DATA_OF_VICTIM )
                                            .add( Item.generate( "Отчество", super
                                                    .objectIsNotNull( card.getVictimHumans() )
                                                    && card.getVictimHumans().isEmpty()
                                                    ? card.getVictimHumans()
                                                    .get( 0 )
                                                    .getMiddleName()
                                                    : Errors.DATA_NOT_FOUND.name()  ) );
                                    this.getDetails().get( Details.DATA_OF_VICTIM )
                                            .add( Item.generate( "Фамилия", super
                                                    .objectIsNotNull( card.getVictimHumans() )
                                                    && card.getVictimHumans().isEmpty()
                                                    ? card.getVictimHumans()
                                                    .get( 0 )
                                                    .getLastName()
                                                    : Errors.DATA_NOT_FOUND.name()  ) );
                                    this.getDetails().get( Details.DATA_OF_VICTIM ).add(
                                            Item.generate( "ID Потерпевшего", super
                                                    .objectIsNotNull( card.getVictimHumans() )
                                                    && card.getVictimHumans().isEmpty()
                                                    ? card.getVictimHumans()
                                                    .get( 0 )
                                                    .getVictimId()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.DATA_OF_VICTIM ).add(
                                            Item.generate( "Дата рождения", super
                                                    .objectIsNotNull( card.getVictimHumans() )
                                                    && card.getVictimHumans().isEmpty()
                                                    ? card.getVictimHumans()
                                                    .get( 0 )
                                                    .getDateOfBirth()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                }

                                case ADDRESS_OF_VICTIM -> {
                                    if ( super.objectIsNotNull( card.getEventHuman() )
                                            && super.objectIsNotNull( card.getEventHuman().getHumanAddress() ) ) {
                                        this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                                .add( Item.generate( "Улица", card.getEventHuman().getHumanAddress().getStreet() ) );
                                        this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                                .add( Item.generate( "Район", card.getEventHuman().getHumanAddress().getSRegionId() ) );
                                        this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                                .add( Item.generate( "Область", card.getEventHuman().getHumanAddress().getSOblastiId() ) );
                                        this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                                .add( Item.generate( "Страна", card.getEventHuman().getHumanAddress().getSCountriesId() ) );
                                        this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                                .add( Item.generate( "Махалля", card.getEventHuman().getHumanAddress().getSMahallyaId() ) );
                                        this.getDetails().get( Details.ADDRESS_OF_VICTIM )
                                                .add( Item.generate( "Населенныый пункт", card.getEventHuman().getHumanAddress().getSNote() ) );
                                    } }

                                case ADDITIONAL_ADDRESS -> {
                                    this.getDetails().get( Details.ADDITIONAL_ADDRESS )
                                            .add( Item.generate( "Дом",
                                                    super
                                                            .objectIsNotNull( card.getEventAddress() )
                                                            ? card.getEventAddress().getFlat()
                                                            : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.ADDITIONAL_ADDRESS )
                                            .add( Item.generate( "Адрес",
                                                    super
                                                            .objectIsNotNull( card.getEventAddress() )
                                                            ? card.getEventAddress().getStreet()
                                                            : Errors.DATA_NOT_FOUND.name() ) );
                                    this.getDetails().get( Details.ADDITIONAL_ADDRESS )
                                            .add( Item.generate( "Квартира",
                                                    super
                                                            .objectIsNotNull( card.getEventAddress() )
                                                            ? card.getEventAddress().getHouse()
                                                            : Errors.DATA_NOT_FOUND.name() ) );
                                }

                                case ADDRESS_OF_INCIDENT -> {
                                    if ( super.objectIsNotNull( card.getEventAddress() ) ) {
                                        this.getDetails().get( Details.APPLICANT_DATA )
                                                .add( Item.generate( "Улица", card.getEventAddress().getStreet() ) );
                                        this.getDetails().get( Details.APPLICANT_DATA )
                                                .add( Item.generate( "Страна", card.getEventAddress().getSRegionId() ) );
                                        this.getDetails().get( Details.APPLICANT_DATA )
                                                .add( Item.generate( "Область", card.getEventAddress().getSOblastiId() ) );
                                        this.getDetails().get( Details.APPLICANT_DATA )
                                                .add( Item.generate( "Район", card.getEventAddress().getSCountriesId() ) );
                                        this.getDetails().get( Details.APPLICANT_DATA )
                                                .add( Item.generate( "Махалля", card.getEventAddress().getSMahallyaId() ) );
                                        this.getDetails().get( Details.APPLICANT_DATA )
                                                .add( Item.generate( "Населенныый пункт", card.getEventAddress().getSNote() ) );
                                    } }

                                case ADDRESS_OF_APPLICANT -> {
                                    if ( super.objectIsNotNull( card.getEventHuman() )
                                            && super.objectIsNotNull( card.getEventHuman().getHumanAddress() ) ) {
                                        this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                                .add( Item.generate( "Улица", card
                                                        .getEventHuman()
                                                        .getHumanAddress()
                                                        .getStreet() ) );

                                        this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                                .add( Item.generate( "Район", card
                                                        .getEventHuman()
                                                        .getHumanAddress()
                                                        .getSRegionId() ) );

                                        this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                                .add( Item.generate( "Область", card
                                                        .getEventHuman()
                                                        .getHumanAddress()
                                                        .getSOblastiId() ) );

                                        this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                                .add( Item.generate( "Страна", card
                                                        .getEventHuman()
                                                        .getHumanAddress()
                                                        .getSCountriesId() ) );

                                        this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                                .add( Item.generate( "Махалля", card
                                                        .getEventHuman()
                                                        .getHumanAddress()
                                                        .getSMahallyaId() ) );

                                        this.getDetails().get( Details.ADDRESS_OF_APPLICANT )
                                                .add( Item.generate( "Населенныый пункт", card.getEventHuman().getHumanAddress().getSNote() ) );
                                    } }

                                case ADDITIONAL_ADDRESS_OF_Victim -> {
                                    if ( super.objectIsNotNull( card.getVictimHumans() )
                                            && super.isCollectionNotEmpty( card.getVictimHumans() )
                                            && super.objectIsNotNull( card
                                                    .getVictimHumans()
                                                    .get( 0 )
                                                    .getVictimAddress() ) ) {
                                        this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_Victim )
                                                .add( Item.generate( "Дом", card
                                                        .getVictimHumans()
                                                        .get( 0 )
                                                        .getVictimAddress()
                                                        .getFlat() ) );
                                        this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_Victim )
                                                .add( Item.generate( "Адрес", card
                                                        .getVictimHumans()
                                                        .get( 0 )
                                                        .getVictimAddress()
                                                        .getHouse() ) );
                                        this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_Victim )
                                                .add( Item.generate( "Квартира", card
                                                        .getVictimHumans()
                                                        .get( 0 )
                                                        .getVictimAddress()
                                                        .getStreet() ) );
                                    } }

                                case ADDITIONAL_ADDRESS_OF_APPLICANT -> {
                                    this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_APPLICANT )
                                            .add( Item.generate( "Дом", super
                                                    .objectIsNotNull( card.getEventAddress() )
                                                    ? card.getEventAddress().getFlat()
                                                    : Errors.DATA_NOT_FOUND.name() ) );

                                    this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_APPLICANT )
                                            .add( Item.generate( "Адрес", super
                                                    .objectIsNotNull( card.getEventAddress() )
                                                    ? card.getEventAddress().getHouse()
                                                    : Errors.DATA_NOT_FOUND.name() ) );

                                    this.getDetails().get( Details.ADDITIONAL_ADDRESS_OF_APPLICANT )
                                            .add( Item.generate( "Квартира", super
                                                    .objectIsNotNull( card.getEventAddress() )
                                                    ? card.getEventAddress().getStreet()
                                                    : Errors.DATA_NOT_FOUND.name() ) );
                                }
                            }
                        }
                ) );
    }

    public CardDetails (
            final SelfEmploymentTask selfEmploymentTask,
            final String language,
            final Patrul patrul ) {
        this.getDetails().clear();
        this.getDetails().put( Details.SELF_EMPLOYMENT, super.newList() );

        Flux.fromStream( Arrays.stream( Details.values() ).sorted() )
                .subscribe( new CustomSubscriber<>(
                        details -> {
                            switch ( details ) {
                                case NUMBER -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "№", selfEmploymentTask.getTaskCommonParams().getUuid() ) );
                                case ADDRESS -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "Адрес", selfEmploymentTask.getAddress() ) );
                                case DESCRIPTION -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "Описание", selfEmploymentTask.getDescription() ) );
                                case ACCEPTED_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "Принятое время", selfEmploymentTask.getIncidentDate() ) );
                                case REPORT_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "Время отправки рапорта", selfEmploymentTask.getIncidentDate() ) );
                                case ACCEPTED_POINT_LATITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "Принятая точка", selfEmploymentTask.getLatOfPatrul() ) );
                                case ARRIVED_POINT_LATITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "Точка прибытия", selfEmploymentTask.getLatOfAccident() ) );
                                case ACCEPTED_POINT_LONGITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "Принятая точка", selfEmploymentTask.getLanOfPatrul() ) );
                                case ARRIVED_POINT_LONGITUDE -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "Точка прибытия", selfEmploymentTask.getLanOfAccident() ) );
                                case ARRIVED_TIME -> this.getDetails().get( Details.SELF_EMPLOYMENT )
                                        .add( Item.generate( "Время прибытия", selfEmploymentTask
                                                .getTaskCommonParams()
                                                .getPatruls()
                                                .get( patrul.getUuid() )
                                                .getPatrulDateData()
                                                .getTaskDate() ) );
                            }
                        }
                ) );
    }
}
