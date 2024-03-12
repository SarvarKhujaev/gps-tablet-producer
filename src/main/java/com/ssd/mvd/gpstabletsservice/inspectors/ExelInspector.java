package com.ssd.mvd.gpstabletsservice.inspectors;

import java.io.File;
import java.io.FileOutputStream;

import java.util.Map;
import java.util.List;
import java.util.function.Supplier;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.codec.binary.Base64;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;

public final class ExelInspector extends LogInspector {
    private Sheet sheet;
    private static Integer i, j;
    private String absolutePath;
    private final XSSFWorkbook workbook = new XSSFWorkbook();
    private final StringBuilder stringBuilder = new StringBuilder();

    public ExelInspector () {}

    private void setTitle( final Status status ) {
        this.stringBuilder.append( switch ( status ) {
            case IN_ACTIVE -> "Nofaol_patrullar";
            case ACTIVE -> "faol_patrullar";
            case FORCE -> "kirmaganlar";
            default -> "kirganlar";
        } ).append( "_" );
    }

    private final Supplier< XSSFFont > createFont = () -> {
        final XSSFFont font = this.workbook.createFont();
        font.setBold( false );
        font.setFontName( "Arial" );
        font.setFontHeightInPoints( (short) 14 );
        return font;
    };

    private final Supplier< CellStyle > createCellStyle = () -> {
        final CellStyle headerStyle = this.workbook.createCellStyle();
        headerStyle.setFillForegroundColor( IndexedColors.WHITE.getIndex() );
        headerStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );
        headerStyle.setBorderBottom( BorderStyle.THIN );
        headerStyle.setBorderRight( BorderStyle.THIN );
        headerStyle.setBorderLeft( BorderStyle.THIN );
        headerStyle.setBorderTop( BorderStyle.THIN );
        return headerStyle;
    };

    /*
    save info about Region and District
    the current Patrul is attached to
    */
    private void saveRegionAndDistrict (
            final Patrul patrul,
            // shows that Patrul is attached to some District or not
            final Boolean hasDistrict
    ) {
        this.stringBuilder.append( "Region_" )
                .append( patrul.getPatrulRegionData().getRegionName() )
                .append( hasDistrict ? "_District_" + patrul.getPatrulRegionData().getDistrictName() : "" );
    }

    private void saveCellValue (
            final Row row,
            final String value,
            final CellStyle headerStyle
    ) {
        final Cell headerCell = row.createCell( i++ );
        headerCell.setCellStyle( headerStyle );
        headerCell.setCellValue( value );
    }

    private String saveResultInExelFileAndReturnParsedFile () {
        try {
            final File currDir = new File( "." );
            this.absolutePath = currDir.getAbsolutePath().substring( 0, currDir.getAbsolutePath().length() - 1 ) + this.stringBuilder;
            this.workbook.write( new FileOutputStream( this.absolutePath ) );
            this.workbook.close();
            MinIoController.getInstance().sendFileToMinio( this.absolutePath, this.stringBuilder );
            return new String(
                    Base64.encodeBase64(
                            FileUtils.readFileToByteArray( new File( this.absolutePath ) )
                    ),
                    StandardCharsets.UTF_8 );
        }
        catch ( final Exception e ) { super.logging( e ); }
        finally {
            final File file = new File( this.absolutePath );
            super.logging( "File is deleted: " + ( file.exists() && file.delete() ) );
        }
        return Errors.SERVICE_WORK_ERROR.name();
    }

    private void writePatrulDataIntoExel (
            final Row row,
            final Patrul patrul,
            final CellStyle headerStyle
    ) {
        this.saveCellValue( row, patrul.getPatrulFIOData().getSurnameNameFatherName(), headerStyle );

        this.saveCellValue(
                row,
                patrul.getDateOfBirth().length() <= 10
                        ? patrul.getDateOfBirth()
                        : super.convertDateToString( super.convertDate( patrul.getDateOfBirth() ) ),
                headerStyle
        );

        this.saveCellValue( row, patrul.getPatrulMobileAppInfo().getPhoneNumber(), headerStyle );
        this.saveCellValue( row, patrul.getRank(), headerStyle );

        this.saveCellValue( row, patrul.getPatrulRegionData().getRegionName(), headerStyle );
        this.saveCellValue( row, patrul.getPatrulRegionData().getDistrictName(), headerStyle );

        this.saveCellValue( row, patrul.getPoliceType(), headerStyle );

        this.saveCellValue(
                row,
                super.convertDateToString(
                        patrul.getPatrulDateData().getLastActiveDate()
                ),
                headerStyle
        );

        this.saveCellValue(
                row,
                super.convertDateToString(
                        patrul
                                .getPatrulDateData()
                                .getStartedToWorkDate()
                ),
                headerStyle
        );

        this.saveCellValue(
                row,
                super.convertDateToString(
                        patrul
                                .getPatrulDateData()
                                .getDateOfRegistration()
                ),
                headerStyle
        );

        this.saveCellValue( row, String.valueOf( patrul.getTotalActivityTime() ), headerStyle );
        this.saveCellValue( row, String.valueOf( patrul.getPatrulMobileAppInfo().getBatteryLevel() ), headerStyle );
    }

    public String download (
            final List< Patrul > patruls,
            final Map< String, String > params,
            final List< String > policeTypes
    ) {
        this.setTitle( Status.valueOf( params.get( "status" ) ) );
        policeTypes.forEach( s -> this.stringBuilder.append( s ).append( "_" ) );

        this.saveRegionAndDistrict(
                patruls.get( 0 ),
                params.containsKey( "districtId" )
        );

        this.sheet = this.workbook.createSheet( this.stringBuilder.toString() );
        this.stringBuilder.append( ".xlsx" );

        this.sheet.setColumnWidth(0, 6000 );
        this.sheet.setColumnWidth(1, 4000 );

        final CellStyle headerStyle = this.createCellStyle.get();
        headerStyle.setFont( this.createFont.get() );

        super.setListForExcel();

        i = 0;
        super.fields.forEach( s -> this.saveCellValue( this.sheet.createRow( 0 ), s, headerStyle ) );

        j = 0;

        super.analyze(
                patruls,
                patrul -> {
                    i = 0;
                    super.analyze(
                            super.fields,
                            field -> this.writePatrulDataIntoExel(
                                    this.sheet.createRow( j + 1 ),
                                    patrul,
                                    headerStyle
                            )
                    );
                    j++;
                }
        );

        super.close();

        return this.saveResultInExelFileAndReturnParsedFile();
    }
}
