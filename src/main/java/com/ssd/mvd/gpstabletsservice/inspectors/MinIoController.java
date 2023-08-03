package com.ssd.mvd.gpstabletsservice.inspectors;

import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import io.minio.BucketExistsArgs;
import io.minio.UploadObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

public final class MinIoController extends LogInspector {
    private final MinioClient minioClient = MinioClient
            .builder()
            .endpoint( GpsTabletsServiceApplication
                    .context
                    .getEnvironment()
                    .getProperty( "variables.MINIO_VARIABLES.ENDPOINT" ) )
            .credentials( GpsTabletsServiceApplication
                    .context
                    .getEnvironment()
                    .getProperty( "variables.MINIO_VARIABLES.ACCESS_KEY" ),
                    GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.MINIO_VARIABLES.SECRET_KEY" ) )
            .build();

    private static final MinIoController INSTANCE  = new MinIoController();

    public static MinIoController getInstance() { return INSTANCE; }

    public void test ( final String fileName, final StringBuilder stringBuilder ) {
        try {
            if ( !this.minioClient.bucketExists( BucketExistsArgs.builder().bucket( "miniocontroller" ).build() ) )
                this.minioClient.makeBucket( MakeBucketArgs.builder().bucket( "miniocontroller" ).build() );

            this.minioClient.uploadObject(
                    UploadObjectArgs
                            .builder()
                            .bucket( GpsTabletsServiceApplication
                                    .context
                                    .getEnvironment()
                                    .getProperty( "variables.MINIO_VARIABLES.BUCKET_NAME" ) )
                            .object( stringBuilder.toString() )
                            .filename( fileName )
                            .build() );

            super.logging( "image created" );
        } catch ( final Exception e ) {
            System.out.println( "Error in MinIo: " + e );
            super.logging( e ); } }
}
