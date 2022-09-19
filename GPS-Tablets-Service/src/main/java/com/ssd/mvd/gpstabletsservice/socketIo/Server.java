package com.ssd.mvd.gpstabletsservice.socketIo;//package com.ssd.mvd.gpstabletsservice.socketIo;
//
//import com.corundumstudio.socketio.SocketIOServer;
//import com.corundumstudio.socketio.Configuration;
//import com.ssd.mvd.gpstabletsservice.entity.Notification;
//import lombok.Data;
//
//@Data
//public class Server {
//    private SocketIOServer server;
//    private static Server instance = new Server();
//    private final Configuration configuration = new Configuration();
//
//    public static Server getInstance() { return instance != null ? instance : ( instance = new Server() ); }
//
//    private Server () {
//        this.getConfiguration().setPort( 7777 );
//        this.getConfiguration().setPingTimeout( 300 );
//        this.getConfiguration().setUpgradeTimeout( 300 );
//        this.getConfiguration().setFirstDataTimeout( 600 );
//        this.getConfiguration().setHostname( "localhost" );
//        this.setServer( new SocketIOServer( this.getConfiguration() ) ); }
//
//    public Notification send ( Notification notification ) {
//        this.getServer().getBroadcastOperations().sendEvent( "notification", Notification.class, "new message" );
//        return notification; }
//
//    public void setSettings () {
//        this.getServer().addEventListener( "request", String.class, ( ( socketIOClient, message, ackRequest ) -> System.out.println( "Client sent message: " + message ) ) );
//        this.getServer().addConnectListener( socketIOClient -> System.out.println( "New client; " + socketIOClient.getRemoteAddress() + " connected successfully" ) );
//        this.getServer().addDisconnectListener( socketIOClient -> System.out.println( socketIOClient.getRemoteAddress() + " disconnected" ) );
//        this.getServer().start();
//        while ( true ) { this.getServer().getBroadcastOperations().sendEvent( "response", "hello world" );
//            try { Thread.sleep( 5 * 1000 ); } catch ( InterruptedException e ) { this.getServer().stop(); } } }
//}
