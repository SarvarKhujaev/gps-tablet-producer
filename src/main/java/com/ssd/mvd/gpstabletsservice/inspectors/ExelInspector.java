package com.ssd.mvd.gpstabletsservice.inspectors;

import java.io.File;
import java.io.FileOutputStream;

import java.util.Map;
import java.util.List;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.codec.binary.Base64;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;

public final class ExelInspector extends LogInspector {
    private static Integer i;
    private Sheet sheet;
    private String absolutePath;
    private final XSSFWorkbook workbook = new XSSFWorkbook();

    public ExelInspector () {}

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
                    .append( params.containsKey( "districtId" ) ? "_District_" + patruls.get( 0 ).getDistrictName() : "" );

            this.sheet = this.workbook.createSheet( stringBuilder.toString() );
            stringBuilder.append( ".xlsx" );

            this.sheet.setColumnWidth(0, 6000 );
            this.sheet.setColumnWidth(1, 4000 );

            final Row header = this.sheet.createRow( 0 );

            final CellStyle headerStyle = this.workbook.createCellStyle();
            headerStyle.setFillForegroundColor( IndexedColors.WHITE.getIndex() );
            headerStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );

            final CellRangeAddress region = new CellRangeAddress(1,patruls.size(),1,super.fields.size() );
            RegionUtil.setBorderBottom( BorderStyle.MEDIUM, region, sheet );
            RegionUtil.setBorderRight( BorderStyle.MEDIUM, region, sheet );
            RegionUtil.setBorderLeft( BorderStyle.MEDIUM, region, sheet );
            RegionUtil.setBorderTop( BorderStyle.MEDIUM, region,sheet );

            final XSSFFont font = this.workbook.createFont();
            font.setBold( false );
            font.setFontName( "Arial" );
            font.setFontHeightInPoints( (short) 14 );
            headerStyle.setFont( font );

            i = 0;
            super.fields.forEach( s -> {
                final Cell headerCell = header.createCell( i++ );
                headerCell.setCellValue( s );
                headerCell.setCellStyle( headerStyle ); } );

            final CellStyle style = this.workbook.createCellStyle();
            style.setWrapText( true );

            i = 0;
            patruls.forEach( patrul -> {
                final Row row = this.sheet.createRow( i + 1 );
                super.fields.forEach( field -> {
                    Cell headerCell = row.createCell( 0 );
                    headerCell.setCellValue( patruls.get( i ).getSurnameNameFatherName() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 1 );
                    headerCell.setCellValue( patruls.get( i ).getDateOfBirth().length() <= 10
                            ? patruls.get( i ).getDateOfBirth()
                            : TimeInspector
                            .getInspector()
                            .getConvertDateToString()
                            .apply( TimeInspector
                                    .getInspector()
                                    .getConvertDate()
                                    .apply( patruls.get( i ).getDateOfBirth() ) ) );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 2 );
                    headerCell.setCellValue( patruls.get( i ).getPhoneNumber() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 3 );
                    headerCell.setCellValue( patruls.get( i ).getRank() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 4 );
                    headerCell.setCellValue( patruls.get( i ).getRegionName() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 5 );
                    headerCell.setCellValue( patruls.get( i ).getDistrictName() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 6 );
                    headerCell.setCellValue( patruls.get( i ).getPoliceType() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 7 );
                    headerCell.setCellValue( TimeInspector.getInspector().getConvertDateToString().apply( patruls.get( i ).getLastActiveDate() ) );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 8 );
                    headerCell.setCellValue( TimeInspector.getInspector().getConvertDateToString().apply( patruls.get( i ).getStartedToWorkDate() ) );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 9 );
                    headerCell.setCellValue( TimeInspector.getInspector().getConvertDateToString().apply( patruls.get( i ).getDateOfRegistration() ) );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 10 );
                    headerCell.setCellValue( patruls.get( i ).getTotalActivityTime() );
                    headerCell.setCellStyle( headerStyle );

                    headerCell = row.createCell( 11 );
                    headerCell.setCellValue( patruls.get( i ).getBatteryLevel() );
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
//            final File file = new File( this.absolutePath );
//            super.logging( "File is deleted: " + ( file.exists() && file.delete() ) );
        }
        return Errors.SERVICE_WORK_ERROR.name(); }
}
