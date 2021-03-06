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
package net.modelbased.sensapp.library.ws.Server


import org.java_websocket.drafts.Draft_17
import org.java_websocket.WebSocket
import org.java_websocket.framing.FrameBuilder
import org.java_websocket.handshake.ClientHandshake
import java.nio.ByteBuffer
import org.java_websocket.framing.Framedata

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 18/07/13
 * Time: 12:12
 */

class WsServerScala(port: Int) extends WsServer(port, new Draft_17){
  private var clientList: List[ServerWebSocketClient] = List()

  override def onOpen(conn: WebSocket, handshake: ClientHandshake) {
    println("New client connected")
  }

  override def onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
    println("A client has been disconnected")
    clientList = clientList diff getClientsByWebSocket(conn)
  }

  override def onError(conn: WebSocket, ex: Exception) {
    println("Error:")
    ex.printStackTrace
  }

  override def onMessage(conn: WebSocket, order: String) {
    println("Received Message String: " + order)
    conn.send(order)
  }

  override def onMessage(conn: WebSocket, blob: ByteBuffer) {
    println("Received Message Byte")
    conn.send(blob)
  }

  override def onWebsocketMessageFragment(conn: WebSocket, frame: Framedata) {
    println("Received Frame Message")
    val builder: FrameBuilder = frame.asInstanceOf[FrameBuilder]
    builder.setTransferemasked(false)
    conn.sendFrame(frame)
  }

  def getClientsByWebSocket(ws: WebSocket): List[ServerWebSocketClient] = {
    clientList.filter(client => client.getWebSocket == ws)
  }

  def getClientsById(id: String): List[ServerWebSocketClient] = {
    clientList.filter(client => client.getId == id)
  }

  def addClientFromMessage(id: String, ws: WebSocket) {
    println("Client identified")
    var exists = false
    clientList.foreach(client => {
      if(client.getId == id && client.getWebSocket == ws)
        exists = true
    })
    if(!exists)
      clientList = clientList :+ new ServerWebSocketClient(ws, id)
  }
}
