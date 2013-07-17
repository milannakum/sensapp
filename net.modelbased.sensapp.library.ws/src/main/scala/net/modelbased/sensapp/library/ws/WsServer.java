/**
 * ====
 *     This file is part of SensApp [ http://sensapp.modelbased.net ]
 *
 *     Copyright (C) 2011-  SINTEF ICT
 *     Contact: SINTEF ICT <nicolas.ferry@sintef.no>
 *
 *     Module: net.modelbased.sensapp
 *
 *     SensApp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as
 *     published by the Free Software Foundation, either version 3 of
 *     the License, or (at your option) any later version.
 *
 *     SensApp is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General
 *     Public License along with SensApp. If not, see
 *     <http://www.gnu.org/licenses/>.
 * ====
 *
 * This file is part of SensApp [ http://sensapp.modelbased.net ]
 *
 * Copyright (C) 2012-  SINTEF ICT
 * Contact: SINTEF ICT <nicolas.ferry@sintef.no>
 *
 * Module: net.modelbased.sensapp.library.ws
 *
 * SensApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SensApp is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with SensApp. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package net.modelbased.sensapp.library.ws;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 16/07/13
 * Time: 10:29
 */
public class WsServer extends WebSocketServer {

    final static public String thisIsMyId = "thisIsMyId=";

    private static int counter = 0;
    private List<ServerWebSocketClient> clientList = new ArrayList<ServerWebSocketClient>();

    public WsServer(int port, Draft d) throws UnknownHostException {
        super( new InetSocketAddress( port ), Collections.singletonList(d) );
    }

    public WsServer(InetSocketAddress address, Draft d) {
        super( address, Collections.singletonList( d ) );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        counter++;
        java.lang.System.out.println("New client connected" + counter);
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        java.lang.System.out.println( "A client has been disconnected" );
        clientList.removeAll(getClientsByWebSocket(conn));
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        java.lang.System.out.println( "Error:" );
        ex.printStackTrace();
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        if(message.contains(thisIsMyId)){
            addClientFromMessage(message, conn);
        }
        else{
            java.lang.System.out.println( "Received Message String: " + message );
            conn.send( message );
        }
    }

    @Override
    public void onMessage( WebSocket conn, ByteBuffer blob ) {
        java.lang.System.out.println( "Received Message Byte" );
        conn.send( blob );
    }

    @Override
    public void onWebsocketMessageFragment( WebSocket conn, Framedata frame ) {
        java.lang.System.out.println( "Received Frame Message" );
        FrameBuilder builder = (FrameBuilder) frame;
        builder.setTransferemasked( false );
        conn.sendFrame( frame );
    }

    public List<ServerWebSocketClient> getClientsByWebSocket(WebSocket ws){
        List<ServerWebSocketClient> l = new ArrayList<ServerWebSocketClient>();
        for(ServerWebSocketClient wsc: clientList)
            if(wsc.getWebSocket() == ws)
                l.add(wsc);
        return l;
    }

    public List<ServerWebSocketClient> getClientsById(String id){
        List<ServerWebSocketClient> l = new ArrayList<ServerWebSocketClient>();
        for(ServerWebSocketClient wsc: clientList)
            if(wsc.getId().equals(id))
                l.add(wsc);
        return l;
    }

    private void addClientFromMessage(String m, WebSocket ws){
        java.lang.System.out.println( "Client identified" );
        ws.send("You have been identified successfully");
        String id = m.substring(thisIsMyId.length(), m.length());
        clientList.add(new ServerWebSocketClient(ws, id));
    }

    private void addClientFromUrl(String m, WebSocket ws){
        java.lang.System.out.println( "Client identified" );
        String id = m.substring(thisIsMyId.length(), m.length());
        clientList.add(new ServerWebSocketClient(ws, id));
    }

    public List<ServerWebSocketClient> getClientWebSocketList(){
        return clientList;
    }
}
