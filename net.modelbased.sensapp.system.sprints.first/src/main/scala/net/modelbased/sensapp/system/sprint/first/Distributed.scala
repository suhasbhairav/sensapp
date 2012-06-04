/**
 * This file is part of SensApp [ http://sensapp.modelbased.net ]
 *
 * Copyright (C) 2012-  SINTEF ICT
 * Contact: Sebastien Mosser <sebastien.mosser@sintef.no>
 *
 * Module: net.modelbased.sensapp.system.sprints.first
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
package net.modelbased.sensapp.system.sprint.first

import akka.actor.ActorSystem
import net.modelbased.sensapp.service.database.raw.RawDatabaseService
import net.modelbased.sensapp.service.registry.RegistryService
import net.modelbased.sensapp.service.dispatch.{ Service => DispatchService }
import net.modelbased.sensapp.service.notifier.{ Service => NotifierService }
import net.modelbased.sensapp.library.system._ 

class BackEnd(override val system: ActorSystem) extends System {
  trait iod { 
    lazy val partners = new TopologyFileBasedDistribution { implicit val actorSystem = system }
    implicit def actorSystem = system 
  }
  
  def services = {
    List(new RawDatabaseService with iod {}, new DispatchService with iod {})
  }  
}

class FrontEnd(override val system: ActorSystem) extends System {
  trait iod { 
    lazy val partners = new TopologyFileBasedDistribution { implicit val actorSystem = system }
    implicit def actorSystem = system 
  }
  
  def services = {
    List(new RegistryService with iod {}, new NotifierService with iod {})
  }  
}

