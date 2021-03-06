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
 * Module: net.modelbased.sensapp.backyard.gatling.ws
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
package net.modelbased.sensapp.backyard.gatling.ws

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import com.giltgroupe.util.gatling.websocket.Predef._
import akka.util.duration._
import bootstrap._
import assertions._

/**
 * Created with IntelliJ IDEA.
 * User: Jonathan
 * Date: 05/08/13
 * Time: 12:45
 */
class SensorFillSimulation  extends Simulation {

  val numberOfUsers: Int = 5 // 10
  val timeframe: Int = 100
  val numberOfData = 5 // 500


  def apply = {
    setUp(sensorFilling.users(numberOfUsers).ramp(timeframe))
  }

  val headers = Map("Content-Type" -> "application/json", "Accept" -> "text/plain,application/json")

  val sensorFilling =
    scenario("Filling the database with random data")
      .exec { (session: Session) => // Preparing the session
      session.setAttribute("sensorId", RandomSensor())
        .setAttribute("stamp", (System.currentTimeMillis / 1000))
    }
      .exec{   // 0. Is SensApp alive?

        websocket("socket").open("ws://"+Target.serverName, "open")
      /*http("Is SensApp alive?")
        .get("http://"+Target.serverName+"/databases/raw/sensors")
        .check(status is 200)*/
    }.pause(100, 200/*, MILLISECONDS*/)
      .exec {  // 1. Creating the database
      websocket("socket").sendMessage("registerRawSensor({\"sensor\": \"${sensorId}\", \"baseTime\": ${stamp}, \"schema\": \"Numerical\"})", "create")
      /*http("Creating the database")
        .post("http://"+Target.serverName+"/databases/raw/sensors")
        .headers(headers)
        .body("{\"sensor\": \"${sensorId}\", \"baseTime\": ${stamp}, \"schema\": \"Numerical\"}")*/
    }.pause(100, 200/*, MILLISECONDS*/)
      //.loop{ chain // Pushing data
      .repeat(numberOfData){
      exec { session: Session =>
        session.setAttribute("data", RandomData(session.getAttribute("sensorId").asInstanceOf[String],
          session.getAttribute("stamp").asInstanceOf[Long]))
      }.exec {
        websocket("socket").sendMessage("registerData(${data})", "push")
        /*http("Pushing random data")
          .put("http://"+Target.serverName+"/databases/raw/data/${sensorId}")
          .headers(headers).body("${data}")*/
      }.exec { (session: Session) =>
        session.setAttribute("stamp", session.getAttribute("stamp").asInstanceOf[Long] + 1)
      }.pause(100, 400/*, MILLISECONDS*/)
    }.exec{
      websocket("socket").close("close")
    }

  setUp(sensorFilling.users(numberOfUsers).ramp(timeframe))
}
