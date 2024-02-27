package com.ssd.mvd.gpstabletsservice.database;

import java.text.MessageFormat;
import com.datastax.driver.core.Session;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.constants.*;
import com.ssd.mvd.gpstabletsservice.tuple.Points;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.*;
import com.ssd.mvd.gpstabletsservice.tuple.PolygonForEscort;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.sos_task.PatrulSos;
import com.ssd.mvd.gpstabletsservice.entity.polygons.Polygon;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonType;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonEntity;
import com.ssd.mvd.gpstabletsservice.database.codec.CodecRegistration;
import com.ssd.mvd.gpstabletsservice.entity.notifications.Notification;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PositionInfo;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;

/*
создает все таблицы, типы, кодеки и пространство ключей
*/
public final class CassandraTablesAndTypesRegister extends CassandraConverter {
    private final Session session;

    private Session getSession() {
        return this.session;
    }

    public static void generate ( final Session session ) {
        new CassandraTablesAndTypesRegister( session );
    }

    private CassandraTablesAndTypesRegister ( final Session session ) {
        this.session = session;

        this.createAllKeyspace();
        this.createAllTypes();
        this.createAllTables();
        this.createAllIndexes();

        this.registerAllCodecs();

        super.logging( "All tables, keyspace and types were created" );
    }

    /*
    Хранит все данные для создания новой таблицы в БД
    */
    private static class TableRegistration extends CassandraConverter {
        public CassandraTables getTableName() {
            return this.tableName;
        }

        public CassandraTables getKeyspace() {
            return this.keyspace;
        }

        public String getConvertedValue() {
            return convertedValue;
        }

        public String getPrefix() {
            return this.prefix;
        }

        /*
        навзвание таблицы
         */
        private final CassandraTables tableName;
        /*
        навзвание пространства в котором находиться таблица
        */
        private final CassandraTables keyspace;

        /*
        сконвертированное значение объекта в CQL понятный язык
        */
        private final String convertedValue;
        /*
        хранит дом значение для CQL
        в особенности Primary key
         */
        private final String prefix;

        public static TableRegistration from (
                final CassandraTables keyspace,
                final CassandraTables tableName,
                final Class object,
                final String prefix
        ) {
            return new TableRegistration( keyspace, tableName, object, prefix );
        }

        private TableRegistration (
                final CassandraTables keyspace,
                final CassandraTables tableName,
                final Class object,
                final String prefix
        ) {
            this.convertedValue = super.convertClassToCassandra.apply( object );
            this.tableName = tableName;
            this.keyspace = keyspace;
            this.prefix = prefix;
        }
    }

    /*
    Хранит все данные для создания нового типа
    и сохранения в БД
    */
    private static class TypeRegistration extends CassandraConverter {
        public String getConvertedValues() {
            return this.convertedValues;
        }

        public CassandraTables getKeyspace() {
            return this.keyspace;
        }

        public CassandraTables getTable() {
            return this.table;
        }

        /*
        навзвание пространства в котором находиться таблица
        */
        private final CassandraTables keyspace;

        /*
        навзвание таблицы
         */
        private final CassandraTables table;

        /*
        сконвертированное значение объекта в CQL понятный язык
        */
        private final String convertedValues;

        public static TypeRegistration from (
                final CassandraTables keyspace,
                final CassandraTables table,
                final String prefix,
                final Class object
        ) {
            return new TypeRegistration( keyspace, table, prefix, object );
        }

        private TypeRegistration (
                final CassandraTables keyspace,
                final CassandraTables table,
                final String prefix,
                final Class object
        ) {
            this.convertedValues = super.convertClassToCassandra.apply( object ) + prefix;
            this.keyspace = keyspace;
            this.table = table;
        }
    }

    /*
    Хранит все данные для создания нового пространства в БД
    */
    private static class KeyspaceRegistration {
        public String getPrefix() {
            return this.prefix;
        }

        /*
        хранит CQL команду для оздания нового пространства
        */
        private final String prefix;

        public static KeyspaceRegistration from (
                final CassandraTables keyspace
        ) {
            return new KeyspaceRegistration( keyspace );
        }

        private KeyspaceRegistration (
                final CassandraTables keyspace
        ) {
            this.prefix = String.format(
                    """
                    %s %s %s
                    WITH REPLICATION = {
                        'class' : 'SimpleStrategy',
                        'replication_factor': 1
                        }
                    AND DURABLE_WRITES = false;
                    """,
                    CassandraCommands.CREATE_KEYSPACE,
                    CassandraCommands.IF_NOT_EXISTS.replaceAll( ";", "" ),
                    keyspace );
        }
    }

    private void createAllKeyspace () {
        /*
        создаем все пространства в БД
         */
        this.createKeyspace( KeyspaceRegistration.from( CassandraTables.TABLETS ) );
        this.createKeyspace( KeyspaceRegistration.from( CassandraTables.ESCORT ) );
    }

    private void createAllTables () {
        /*
        создаем все таблицы в БД
         */
        this.createTable(
                TableRegistration.from(
                        CassandraTables.TABLETS,
                        CassandraTables.CARS,
                        ReqCar.class,
                        """
                        , PRIMARY KEY ( uuid ) ) WITH %s;
                        """.formatted(
                                super.generateID()
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.TABLETS,
                        CassandraTables.POLICE_TYPE,
                        PoliceType.class,
                        """
                        , PRIMARY KEY ( uuid ) ) WITH %s;
                        """.formatted(
                                super.generateID()
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.TABLETS,
                        CassandraTables.POLYGON_TYPE,
                        PolygonType.class,
                        """
                        , PRIMARY KEY ( uuid ) ) WITH %s;
                        """.formatted(
                                super.generateID()
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.TABLETS,
                        CassandraTables.TABLETS_USAGE_TABLE,
                        TabletUsage.class,
                        """
                        , PRIMARY KEY ( uuidOfPatrul, simCardNumber ) ) %s AND %s AND %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_CLUSTERING_ORDER.formatted( "simCardNumber ASC" ),
                                CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.LEVELED_COMPACTION ),
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о том как долго патрульный пользовался планшетом
                                        """
                                ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                CassandraCommands.WITH_TTL.formatted( TimeInspector.DAY_IN_SECOND * 7 ),
                                super.generateID()
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.TABLETS,
                        CassandraTables.PATRULS,
                        Patrul.class,
                        MessageFormat.format(
                                """
                                   , listOfTasks {11}< {10}, {10} >, -- хранит список всех задач которые выполнил патрульный
                                   patrulFIOData {0}, -- данные о ФИО
                                   patrulCarInfo {1}, -- данные о машине патрульного
                                   patrulDateData {2}, -- данные обо всех параметрах связанных с датами
                                   patrulAuthData {3}, -- данные о аутентификации патрульного
                                   patrulTaskInfo {4}, -- данные касающиеся задач патрульного
                                   patrulTokenInfo {5}, -- данные о токенах
                                   patrulRegionData {6}, -- данные о регионе службы патрульного
                                   patrulLocationData {7}, -- данные о локации патрульного
                                   patrulUniqueValues {8}, -- все уникальные параметры патрульного
                                   patrulMobileAppInfo {9}, -- мобильные данные патрульного
                                   PRIMARY KEY ( (uuid), passportNumber ) ) {12};
                                """,
                                CassandraTables.PATRUL_FIO_DATA,
                                CassandraTables.PATRUL_CAR_DATA,
                                CassandraTables.PATRUL_DATE_DATA,
                                CassandraTables.PATRUL_AUTH_DATA,
                                CassandraTables.PATRUL_TASK_DATA,
                                CassandraTables.PATRUL_TOKEN_DATA,
                                CassandraTables.PATRUL_REGION_DATA,
                                CassandraTables.PATRUL_LOCATION_DATA,
                                CassandraTables.PATRUL_UNIQUE_DATA,
                                CassandraTables.PATRUL_MOBILE_DATA,

                                CassandraDataTypes.TEXT,
                                CassandraDataTypes.MAP,

                                """
                                %s AND %s AND %s AND %s AND %s AND %s AND %s
                                """.formatted(
                                        CassandraCommands.WITH_CLUSTERING_ORDER.formatted( "passportNumber ASC" ),
                                        CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.LEVELED_COMPACTION ),
                                        CassandraCommands.WITH_COMMENT.formatted(
                                                """
                                                хранит данные, обо всех патрульных, является основной таблицой
                                                """
                                        ),
                                        CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                        CassandraCommands.WITH_TTL.formatted( TimeInspector.DAY_IN_SECOND * 365 * 8 ),
                                        super.generateID(),
                                        CassandraCommands.WITH_CACHING.formatted(
                                                CassandraDataTypes.ALL, CassandraDataTypes.NONE
                                        )
                                )
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.TABLETS,
                        CassandraTables.POLYGON,
                        Polygon.class,
                        MessageFormat.format(
                                """
                                , polygonType {2}< {0} >,
                                patrulList {3}< {4} >, -- список патрульных которые привязаны к этому полигону
                                latlngs {3} < {2}< {1} > >, -- список точек обозначающих границы полигона
                                PRIMARY KEY ( uuid ) ) WITH {5};
                                """,
                                CassandraTables.POLYGON_TYPE,
                                CassandraTables.POLYGON_ENTITY,

                                CassandraDataTypes.FROZEN,
                                CassandraDataTypes.LIST,
                                CassandraDataTypes.UUID,

                                super.generateID()
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.TABLETS,
                        CassandraTables.POLYGON_FOR_PATRUl,
                        Polygon.class,
                        MessageFormat.format(
                                """
                                , polygonType {2}< {0} >,
                                patrulList {3}< {4} >,-- список патрульных которые привязаны к этому полигону
                                latlngs {3} < {2}< {1} > >,-- список точек обозначающих границы полигона
                                PRIMARY KEY ( uuid ) ) WITH {5};
                                """,
                                CassandraTables.POLYGON_TYPE,
                                CassandraTables.POLYGON_ENTITY,
                                CassandraDataTypes.FROZEN,
                                CassandraDataTypes.LIST,
                                CassandraDataTypes.UUID,

                                super.generateID()
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.TABLETS,
                        CassandraTables.LUSTRA,
                        AtlasLustra.class,
                        MessageFormat.format(
                                """
                                , cameraLists {0}< {1}< {2} > >,
                                PRIMARY KEY (uuid) ) WITH {3};
                                """,
                                CassandraDataTypes.LIST,
                                CassandraDataTypes.FROZEN,
                                CassandraTables.CAMERA_LIST,

                                super.generateID()
                        )
                )
        );

        this.createTable (
                TableRegistration.from(
                        CassandraTables.TABLETS,
                        CassandraTables.NOTIFICATION,
                        Notification.class,
                        """
                        , PRIMARY KEY( (uuid), notificationWasCreated, passportSeries, taskTypes, id ) )
                        %s AND %s AND %s AND %s AND %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_CLUSTERING_ORDER.formatted(
                                        """
                                        notificationWasCreated DESC, passportSeries ASC, taskTypes ASC
                                        """
                                ),
                                CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.DATE_TIERED_COMPACTION ),
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, обо всех уведомлениях которые генерируются на уровне сервиса
                                        """
                                ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                CassandraCommands.MEM_TABLE_FLUSH_PERIOD.formatted( TimeInspector.DAY_IN_SECOND * 1000 ),
                                CassandraCommands.WITH_TTL.formatted( TimeInspector.DAY_IN_SECOND * 10 ),
                                super.generateID(),
                                CassandraCommands.WITH_CACHING.formatted(
                                        CassandraDataTypes.ALL, CassandraDataTypes.NONE
                                )
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.ESCORT,
                        CassandraTables.POLYGON_FOR_ESCORT,
                        PolygonForEscort.class,
                        MessageFormat.format(
                        """
                                , pointsList {0}< {1} < {2} > >,
                                latlngs {0}< {1} < {3} > >, -- список точек обозначающих границы полигона
                                PRIMARY KEY( uuid ) ) WITH {4};
                                """,
                                CassandraDataTypes.LIST,
                                CassandraDataTypes.FROZEN,

                                CassandraTables.POINTS_ENTITY,
                                CassandraTables.POLYGON_ENTITY,

                                super.generateID()
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.ESCORT,
                        CassandraTables.TUPLE_OF_ESCORT,
                        EscortTuple.class,
                        MessageFormat.format(
                                """
                                 , tupleOfCarsList {0}< {1} >, -- список машин, которые привязаны к эскорту
                                 patrulList {0}< {1} >, -- список патрульных, которые привязаны к эскорту
                                 PRIMARY KEY ( uuid ) ) WITH {2};
                                 """,
                                CassandraDataTypes.LIST,
                                CassandraDataTypes.UUID,

                                super.generateID()
                        )
                )
        );

        this.createTable(
                TableRegistration.from(
                        CassandraTables.ESCORT,
                        CassandraTables.COUNTRIES,
                        Country.class,
                        """
                        , PRIMARY KEY ( (uuid), countryNameEn, countryNameUz, countryNameRu ) )
                        %s AND %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_CLUSTERING_ORDER.formatted(
                                            """
                                            countryNameEn ASC, countryNameUz ASC, countryNameRu ASC
                                        """
                                ),
                                CassandraCommands.WITH_COMPACTION.formatted(
                                        CassandraCompactionTypes.SIZE_TIERED_COMPACTION
                                ),
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о названиях стран на разных языках
                                        """
                                ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                super.generateID()
                        )
                )
        );

        this.getSession().execute(
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( login {3}, -- уникальный логин для каждого пользователей
                        password {3}, -- пароль
                        uuid {4}, -- уникальный ID пользователей
                        {5}
                        """,
                        CassandraCommands.CREATE_TABLE,
                        CassandraTables.TABLETS,
                        CassandraTables.PATRULS_LOGIN_TABLE,

                        CassandraDataTypes.TEXT,
                        CassandraDataTypes.UUID,

                        """
                        PRIMARY KEY ( (login), uuid ) ) WITH %s AND %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.SIZE_TIERED_COMPACTION ),
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о том логине и паролях для входа в аккаунт патрульного
                                        """
                                ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                CassandraCommands.WITH_CACHING.formatted( CassandraDataTypes.NONE, CassandraDataTypes.NONE ),
                                super.generateID()
                        )
                )
        );

        this.getSession().execute(
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( uuid {3}, -- уникальный ID пользователей
                        date {4}, -- дата записи в таблицу
                        status {5}, -- текущий статус патрульного
                        message {5}, -- генерируемое сообщение
                        totalActivityTime {6}, -- общее время работы патрульного в секундах
                        PRIMARY KEY( uuid, date, status ) ) {7}
                        """,
                        CassandraCommands.CREATE_TABLE,
                        CassandraTables.TABLETS,
                        CassandraTables.PATRULS_STATUS_TABLE,

                        CassandraDataTypes.UUID,
                        CassandraDataTypes.TIMESTAMP,
                        CassandraDataTypes.TEXT,
                        CassandraDataTypes.BIGINT,

                        """
                        %s AND %s AND %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_CLUSTERING_ORDER.formatted( "date DESC, status DESC" ),
                                CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.DATE_TIERED_COMPACTION ),
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, обо всех изменениях в статусе каждого патрульного
                                        """
                                ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                CassandraCommands.MEM_TABLE_FLUSH_PERIOD.formatted( TimeInspector.DAY_IN_SECOND * 1000 ),
                                super.generateID()
                        )
                )
        );

        this.getSession().execute(
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( id {3} PRIMARY KEY,
                        version {3}, -- название последней версии приложения
                        link {4} ) {5}; -- ссылка на последнюю версию
                        """,
                        CassandraCommands.CREATE_TABLE,
                        CassandraTables.TABLETS,
                        CassandraTables.ANDROID_VERSION_CONTROL_TABLE,

                        CassandraDataTypes.TEXT,
                        CassandraDataTypes.INET,

                        """
                        WITH %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о последней версии приложения
                                        """
                                ),
                                super.generateID(),
                                CassandraCommands.WITH_CACHING.formatted(
                                        CassandraDataTypes.ALL, CassandraDataTypes.ALL
                                )
                        )
                )
        );

        this.getSession().execute(
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( gosnumber {3} PRIMARY KEY, -- номер машины
                        cameraImage {7}, -- изображения машины
                        violationsInformationsList {4}< {5} < {6} > >, -- список всех штрафов машины
                        object {7} ) {8}; -- сериализованный объект
                        """,
                        CassandraCommands.CREATE_TABLE,
                        CassandraTables.TABLETS,
                        CassandraTables.CARTOTALDATA,

                        CassandraDataTypes.TEXT,
                        CassandraDataTypes.LIST,
                        CassandraDataTypes.FROZEN,

                        CassandraTables.VIOLATION_LIST_TYPE,
                        CassandraDataTypes.BLOB,

                        """
                        WITH %s AND %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о штрафах машины
                                        """
                                ),
                                super.generateID(),
                                CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.SIZE_TIERED_COMPACTION ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                CassandraCommands.WITH_CACHING.formatted(
                                        CassandraDataTypes.ALL, CassandraDataTypes.ALL
                                )
                        )
                )
        );

        this.getSession().execute (
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( uuid {3}, -- ID генериемое на стороне сервиса
                            id {4}, -- ID самой задачи, может повторяться
                            taskType {4}, -- тип задачи
                            object {5}, -- сериализованный объект
                            PRIMARY KEY( (uuid), id, taskType )
                        ) {6};
                        """,
                        CassandraCommands.CREATE_TABLE,
                        CassandraTables.TABLETS,
                        CassandraTables.TASKS_STORAGE_TABLE,

                        CassandraDataTypes.UUID,
                        CassandraDataTypes.TEXT,
                        CassandraDataTypes.BLOB,

                        """
                        %s AND %s AND %s AND %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_CLUSTERING_ORDER.formatted(
                                        """
                                        id DESC, taskType ASC
                                        """
                                ),
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о задаче
                                        """
                                ),
                                CassandraCommands.WITH_CACHING.formatted(
                                        CassandraDataTypes.ALL, CassandraDataTypes.NONE
                                ),
                                CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.LEVELED_COMPACTION ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                CassandraCommands.MEM_TABLE_FLUSH_PERIOD.formatted( TimeInspector.DAY_IN_SECOND * 1000 ),
                                super.generateID()
                        )
                )
        );

        this.getSession().execute (
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( id {3} PRIMARY KEY, object {4} ) {5};
                        """,
                        CassandraCommands.CREATE_TABLE,
                        CassandraTables.TABLETS,

                        CassandraTables.ACTIVE_TASK,
                        CassandraDataTypes.TEXT,
                        CassandraDataTypes.BLOB,

                        """
                        WITH %s AND %s AND %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_CACHING.formatted(
                                        CassandraDataTypes.ALL, CassandraDataTypes.NONE
                                ),
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о коротком сожержании задачи
                                        """
                                ),
                                CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.LEVELED_COMPACTION ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                CassandraCommands.MEM_TABLE_FLUSH_PERIOD.formatted( TimeInspector.DAY_IN_SECOND * 1000 ),
                                super.generateID()
                        )
                )
        );

        this.getSession().execute (
                MessageFormat.format(
                        """
                        {0} {1}.{2} {3}
                        , patrulStatuses {4}< {5}, {6} >, -- хранит статусы всех патрульных которые получили этот СОС сигнал
                        {7}
                        """,
                        CassandraCommands.CREATE_TABLE,
                        CassandraTables.TABLETS,
                        CassandraTables.PATRUL_SOS_TABLE,

                        super.convertClassToCassandra.apply( PatrulSos.class ),

                        CassandraDataTypes.MAP,
                        CassandraDataTypes.UUID,
                        CassandraDataTypes.TEXT,

                        """
                        PRIMARY KEY ( uuid ) ) WITH %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.SIZE_TIERED_COMPACTION ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о сос сигнале
                                        """
                                ),
                                super.generateID()
                        )
                )
        );

        this.getSession().execute(
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( taskId                {3},                -- уникальное ID задачи
                        status                  {3},                -- статус задачи
                        taskTypes               {3},                -- тип задачи
                        patrulUUID              {10},               -- ID патрульного который привязан к задаче
                        totalTimeConsumption    {4},                -- полное время затраченное на выполнение задачи
                        timeWastedToArrive      {4},                -- время затраченное на то, чтобы добраться ло точки назначения
                        dateOfComing            {5},                -- дата прихода к локации
                        inTime                  {6},                -- показывает во время ли успел патрульный
                        positionInfoList        {7}< {8}< {9} > >,  -- история перемещения патрульного пока он шел к точке назначения
                        {11}
                        """,
                        CassandraCommands.CREATE_TABLE,
                        CassandraTables.TABLETS,
                        CassandraTables.TASKS_TIMING_TABLE,

                        CassandraDataTypes.TEXT,
                        CassandraDataTypes.BIGINT,
                        CassandraDataTypes.TIMESTAMP,
                        CassandraDataTypes.BOOLEAN,
                        CassandraDataTypes.LIST,
                        CassandraDataTypes.FROZEN,
                        CassandraTables.POSITION_INFO,
                        CassandraDataTypes.UUID,

                        """
                        PRIMARY KEY( (taskId), patrulUUID, taskTypes ) ) %s AND %s AND %s AND %s AND %s AND %s AND %s;
                        """.formatted(
                                CassandraCommands.WITH_CLUSTERING_ORDER.formatted(
                                        "patrulUUID DESC, taskTypes ASC"
                                ),
                                CassandraCommands.WITH_CACHING.formatted(
                                        CassandraDataTypes.NONE, CassandraDataTypes.NONE
                                ),
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о истории перемещении патрульного пока он шел к точке назначения
                                        """
                                ),
                                CassandraCommands.WITH_COMPACTION.formatted( CassandraCompactionTypes.DATE_TIERED_COMPACTION ),
                                CassandraCommands.WITH_COMPRESSION.formatted( CassandraCompressionTypes.LZ4, 64 ),
                                CassandraCommands.MEM_TABLE_FLUSH_PERIOD.formatted( TimeInspector.DAY_IN_SECOND * 5000 ),
                                super.generateID()
                        )
                )
        );

        this.getSession().execute (
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( patrulUUID {3} PRIMARY KEY,
                        sentSosList {4}< {3} >, -- список отправленных сос сигналов
                        attachedSosList {4}< {3} >, -- список закрепленных сос сигналов
                        cancelledSosList {4}< {3} >, -- список закрепленных сос сигналов
                        acceptedSosList {4}< {3} > ) {5}; -- список принятых сос сигналов
                        """,
                        CassandraCommands.CREATE_TABLE,
                        CassandraTables.TABLETS,
                        CassandraTables.PATRUL_SOS_LIST,

                        CassandraDataTypes.UUID,
                        CassandraDataTypes.SET,

                        """
                        WITH %s AND %s
                        """.formatted(
                                CassandraCommands.WITH_COMMENT.formatted(
                                        """
                                        хранит данные, о всех сосс сигналах для каждого патрульного
                                        """
                                ),
                                super.generateID()
                        )
                )
        );
    }

    private void createAllTypes () {
        /*
        создаем все UDT в БД
         */
        this.createType( TypeRegistration.from( CassandraTables.ESCORT, CassandraTables.POINTS_ENTITY, "", Points.class ) );
        this.createType( TypeRegistration.from( CassandraTables.ESCORT, CassandraTables.POLYGON_ENTITY, "", PolygonEntity.class ) );

        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_TYPE, "", Patrul.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.POLICE_TYPE, "", PoliceType.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.CAMERA_LIST, "", CameraList.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.POLYGON_TYPE, "", PolygonType.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.POSITION_INFO, "", PositionInfo.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.POLYGON_ENTITY, "", PolygonEntity.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.VIOLATION_LIST_TYPE, "", ViolationsInformation.class ) );

        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_CAR_DATA, "", PatrulCarInfo.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_FIO_DATA, "", PatrulFIOData.class ) );

        this.createType( TypeRegistration.from(
                CassandraTables.TABLETS,
                CassandraTables.REPORT_FOR_CARD,
                MessageFormat.format(
                        """
                        , imagesIds {2}< {0}< {1} > >
                        """,
                        CassandraDataTypes.LIST,
                        CassandraDataTypes.TEXT,
                        CassandraDataTypes.FROZEN
                ),
                ReportForCard.class ) );

        this.createType( TypeRegistration.from(
                CassandraTables.TABLETS,
                CassandraTables.PATRUL_TASK_DATA,
                MessageFormat.format(
                        """
                        , listOfTasks frozen< {1}< {0}, {0} > >
                        """,
                        CassandraDataTypes.TEXT,
                        CassandraDataTypes.MAP
                ),
                PatrulTaskInfo.class ) );

        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_DATE_DATA, "", PatrulDateData.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_AUTH_DATA, "", PatrulAuthData.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_TOKEN_DATA, "", PatrulTokenInfo.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_REGION_DATA, "", PatrulRegionData.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_MOBILE_DATA, "", PatrulMobileAppInfo.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_UNIQUE_DATA, "", PatrulUniqueValues.class ) );
        this.createType( TypeRegistration.from( CassandraTables.TABLETS, CassandraTables.PATRUL_LOCATION_DATA, "", PatrulLocationData.class ) );
    }

    private void createAllIndexes () {
        this.getSession().execute(
                MessageFormat.format(
                        """
                                {0} {1}.{2}({3});
                         """,
                        CassandraCommands.CREATE_INDEX,
                        CassandraTables.TABLETS,
                        CassandraTables.PATRULS,
                        "passportNumber"
                )
        );
    }

    /*
    функция создает новые типы в БД
    */
    private void createType (
            final TypeRegistration typeRegistration
    ) {
        this.getSession().execute(
                MessageFormat.format(
                        """
                        {0} {1}.{2} {3} );
                        """,
                        CassandraCommands.CREATE_TYPE,
                        typeRegistration.getKeyspace(),
                        typeRegistration.getTable(),
                        typeRegistration.getConvertedValues()
                )
        );
    }

    /*
    функция создает новые таблицы в БД
    */
    private void createTable (
            final TableRegistration tableRegistration
    ) {
        this.getSession().execute(
                MessageFormat.format(
                        """
                        {0} {1}.{2} {3} {4};
                        """,
                        CassandraCommands.CREATE_TABLE,
                        tableRegistration.getKeyspace(),
                        tableRegistration.getTableName(),
                        tableRegistration.getConvertedValue(),
                        tableRegistration.getPrefix()
                )
        );
    }

    /*
    функция создает новые пространства в БД
    */
    private void createKeyspace (
            final KeyspaceRegistration keyspaceRegistration
    ) {
        this.getSession().execute( keyspaceRegistration.getPrefix() );
    }

    private static byte i;

    /*
    создаем и регистрируем все кодеки для БД
    */
    private void registerAllCodecs () {
        i = 0;
        super.analyze(
                super.getMapOfKeyspaceAndTypes(),
                ( keyspace, udtTypes ) -> super.analyze(
                        udtTypes,
                        udtType -> {
                            CassandraDataControl
                                    .getInstance()
                                    .getCodecRegistry()
                                    .register( new CodecRegistration(
                                            CassandraDataControl
                                                    .getInstance()
                                                    .getCodecRegistry()
                                                    .codecFor( CassandraDataControl
                                                            .getInstance()
                                                            .getCluster()
                                                            .getMetadata()
                                                            .getKeyspace( keyspace.name() )
                                                            .getUserType( udtType.name() ) ),
                                            super.listOfClasses.get( i ),
                                            i ) );
                            i++;
                        }
                )
        );
    }
}
