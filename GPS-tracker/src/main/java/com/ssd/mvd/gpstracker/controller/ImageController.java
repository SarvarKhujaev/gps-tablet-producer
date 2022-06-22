package com.ssd.mvd.gpstracker.controller;

import com.ssd.mvd.gpstracker.entity.Data;
import com.ssd.mvd.gpstracker.request.ImageRequest;
import com.ssd.mvd.gpstracker.response.ApiResponseModel;
import com.ssd.mvd.gpstracker.response.Status;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@RestController
public class ImageController {
    @MessageMapping ( value = "savePhoto" )
    public Mono< ApiResponseModel > savePhoto ( ImageRequest imageRequest ) { return imageRequest.getAbsolutePath().flatMap( s -> {
            try { ImageIO.write( ImageIO.read( new ByteArrayInputStream( imageRequest.getBytes() ) ), "jpg", new File( s ) ); } catch ( IOException e ) { e.printStackTrace(); }
            return Mono.just( ApiResponseModel.builder().data( Data.builder().type( s ).build() ).status( Status.builder().message( "Image was saved" ).code( 200 ).build() ).build() ); } ); }
}
