package com.ssd.mvd.gpstabletsservice.inspectors;

import java.io.File;
import java.io.FileOutputStream;

import java.util.Map;
import java.util.List;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.codec.binary.Base64;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.database.CassandraConverter;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;

public final class ExelInspector extends CassandraConverter {
    private static Integer i;
    private final Sheet sheet;
    private String absolutePath;
    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final List< Field > fields = super.getFields.apply( Patrul.class ).toList();

    public ExelInspector () { this.sheet = this.workbook.createSheet( this.getClass().getName() ); }

    public String download ( final List< Patrul > patruls,
                           final Map< String, String > params,
                           final List< String > policeTypes ) {
        try {
            final StringBuilder stringBuilder = new StringBuilder().append( switch ( Status.valueOf( params.get( "status" ) ) ) {
                case ACTIVE -> "faol_patrullar";
                case IN_ACTIVE -> "Nofaol_patrullar";
                case FORCE -> "kirmaganlar";
                default -> "kirganlar"; } ).append( "_" );
            policeTypes.forEach( s -> stringBuilder.append( s ).append( "_" ) );
            stringBuilder.append( "Region_" )
                    .append( patruls.get( 0 ).getRegionName() )
                    .append( params.containsKey( "districtId" ) ? "_District_" + patruls.get( 0 ).getDistrictName() : "" )
                    .append( ".xlsx" );

            this.sheet.setColumnWidth(0, patruls.size() );
            this.sheet.setColumnWidth(1, 70 );

            final Row header = this.sheet.createRow( 0 );

            final CellStyle headerStyle = this.workbook.createCellStyle();
            headerStyle.setFillForegroundColor( IndexedColors.LIGHT_BLUE.getIndex() );
            headerStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );

            final XSSFFont font = this.workbook.createFont();
            font.setBold( true );
            font.setFontName( "Arial" );
            font.setFontHeightInPoints( (short) 16 );
            headerStyle.setFont( font );

            i = 0;
            this.fields.forEach( field -> {
                final Cell headerCell = header.createCell( i++ );
                headerCell.setCellValue( field.getName() );
                headerCell.setCellStyle( headerStyle ); } );

            final CellStyle style = this.workbook.createCellStyle();
            style.setWrapText( true );

            i = 0;
            patruls.forEach( patrul -> {
                final Row row = this.sheet.createRow( i + 1 );
                this.fields.forEach( field -> {
                    Cell headerCell = row.createCell( 0 );
                    headerCell.setCellValue( patruls.get( i ).getTaskDate() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 1 );
                    headerCell.setCellValue( patruls.get( i ).getLastActiveDate() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 2 );
                    headerCell.setCellValue( patruls.get( i ).getStartedToWorkDate() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 3 );
                    headerCell.setCellValue( patruls.get( i ).getDateOfRegistration() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 4 );
                    headerCell.setCellValue( patruls.get( i ).getDistance() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 5 );
                    headerCell.setCellValue( patruls.get( i ).getLatitude() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 6 );
                    headerCell.setCellValue( patruls.get( i ).getLongitude() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 7 );
                    headerCell.setCellValue( patruls.get( i ).getLatitudeOfTask() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 8 );
                    headerCell.setCellValue( patruls.get( i ).getLongitudeOfTask() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 9 );
                    headerCell.setCellValue( patruls.get( i ).getUuid().toString() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 10 );
                    headerCell.setCellValue( patruls.get( i ).getOrgan().toString() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 11 );
                    headerCell.setCellValue( super.checkParam.test( patruls.get( i ).getSos_id() ) ? patruls.get( i ).getSos_id().toString() : "null" );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 12 );
                    headerCell.setCellValue( super.checkParam.test( patruls.get( i ).getUuidOfEscort() ) ? patruls.get( i ).getUuidOfEscort().toString() : "null" );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 13 );
                    headerCell.setCellValue( super.checkParam.test( patruls.get( i ).getUuidForPatrulCar() ) ? patruls.get( i ).getUuidForPatrulCar().toString() : "null" );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 14 );
                    headerCell.setCellValue( super.checkParam.test( patruls.get( i ).getUuidForEscortCar() ) ? patruls.get( i ).getUuidForEscortCar().toString() : "null" );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 15 );
                    headerCell.setCellValue( patruls.get( i ).getRegionId() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 16 );
                    headerCell.setCellValue( patruls.get( i ).getMahallaId() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 17 );
                    headerCell.setCellValue( patruls.get( i ).getDistrictId() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 18 );
                    headerCell.setCellValue( patruls.get( i ).getTotalActivityTime() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 19 );
                    headerCell.setCellValue( patruls.get( i ).getBatteryLevel() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 20 );
                    headerCell.setCellValue( patruls.get( i ).getInPolygon() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 21 );
                    headerCell.setCellValue( patruls.get( i ).getTuplePermission() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 22 );
                    headerCell.setCellValue( patruls.get( i ).getName() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 23 );
                    headerCell.setCellValue( patruls.get( i ).getRank() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 24 );
                    headerCell.setCellValue( patruls.get( i ).getEmail() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 25 );
                    headerCell.setCellValue( patruls.get( i ).getLogin() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 26 );
                    headerCell.setCellValue( patruls.get( i ).getTaskId() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 27 );
                    headerCell.setCellValue( patruls.get( i ).getCarType() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 28 );
                    headerCell.setCellValue( patruls.get( i ).getSurname() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 29 );
                    headerCell.setCellValue( patruls.get( i ).getPassword() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 30 );
                    headerCell.setCellValue( patruls.get( i ).getCarNumber() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 31 );
                    headerCell.setCellValue( patruls.get( i ).getOrganName() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 32 );
                    headerCell.setCellValue( patruls.get( i ).getRegionName() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 33 );
                    headerCell.setCellValue( patruls.get( i ).getPoliceType() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 34 );
                    headerCell.setCellValue( patruls.get( i ).getFatherName() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 35 );
                    headerCell.setCellValue( patruls.get( i ).getDateOfBirth() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 36 );
                    headerCell.setCellValue( patruls.get( i ).getPhoneNumber() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 37 );
                    headerCell.setCellValue( patruls.get( i ).getDistrictName() );
                    headerCell.setCellStyle( headerStyle );


                    headerCell = row.createCell( 38 );
                    headerCell.setCellValue( patruls.get( i ).getSpecialToken() );
                    headerCell.setCellStyle( headerStyle );


                    headerCell = row.createCell( 39 );
                    headerCell.setCellValue( patruls.get( i ).getTokenForLogin() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 40 );
                    headerCell.setCellValue( patruls.get( i ).getSimCardNumber() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 41 );
                    headerCell.setCellValue( patruls.get( i ).getPassportNumber() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 42 );
                    headerCell.setCellValue( patruls.get( i ).getPatrulImageLink() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 43 );
                    headerCell.setCellValue( patruls.get( i ).getSurnameNameFatherName() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 44 );
                    headerCell.setCellValue( patruls.get( i ).getStatus().toString() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 45 );
                    headerCell.setCellValue( patruls.get( i ).getTaskTypes().toString() );
                    headerCell.setCellStyle( headerStyle ); } );
                i++; } );

            final File currDir = new File( "." );
            this.absolutePath = currDir.getAbsolutePath().substring( 0, currDir.getAbsolutePath().length() - 1 ) + stringBuilder;
            this.workbook.write( new FileOutputStream( this.absolutePath ) );
            this.workbook.close();
            MinIoController.getInstance().test( this.absolutePath, stringBuilder );
            return new String( Base64.encodeBase64( FileUtils.readFileToByteArray( new File( this.absolutePath ) ) ), StandardCharsets.UTF_8 ); }
        catch ( final Exception e ) { super.logging( e ); }
        finally {
            final File file = new File( this.absolutePath );
            super.logging( "File is deleted: " + ( file.exists() && file.delete() ) ); }
        return Errors.SERVICE_WORK_ERROR.name(); }
}
