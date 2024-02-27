package com.ssd.mvd.gpstabletsservice.inspectors;

import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import io.minio.BucketExistsArgs;
import io.minio.UploadObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;

public final class MinIoController extends LogInspector {
    private final String BUCKET_NAME = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.MINIO_VARIABLES.BUCKET_NAME" );

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

    public void sendFileToMinio ( final String fileName, final StringBuilder stringBuilder ) {
        try {
            if ( !this.minioClient.bucketExists( BucketExistsArgs.builder().bucket( this.BUCKET_NAME ).build() ) )
                this.minioClient.makeBucket( MakeBucketArgs.builder().bucket( this.BUCKET_NAME ).build() );

            this.minioClient.uploadObject(
                    UploadObjectArgs
                            .builder()
                            .bucket( this.BUCKET_NAME )
                            .object( stringBuilder.toString() )
                            .filename( fileName )
                            .build() );
        } catch ( final Exception e ) { super.logging( e ); } }
}
