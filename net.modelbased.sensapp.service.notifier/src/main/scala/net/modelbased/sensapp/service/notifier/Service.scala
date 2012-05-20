/**
 * This file is part of SensApp [ http://sensapp.modelbased.net ]
 *
 * Copyright (C) 2012-  SINTEF ICT
 * Contact: Sebastien Mosser <sebastien.mosser@sintef.no>
 *
 * Module: net.modelbased.sensapp.service.notifier
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
package net.modelbased.sensapp.service.notifier

import cc.spray._
import cc.spray.http._
import cc.spray.json._
import cc.spray.json.DefaultJsonProtocol._
import cc.spray.directives._
import cc.spray.typeconversion.SprayJsonSupport
// Application specific:
import net.modelbased.sensapp.service.notifier.data.{Subscription, SubscriptionRegistry }
import net.modelbased.sensapp.service.notifier.data.SubscriptionJsonProtocol.format
import net.modelbased.sensapp.library.senml.Root
import net.modelbased.sensapp.library.senml.spec.Standard

import net.modelbased.sensapp.library.system.{Service => SensAppService} 

trait Service extends SensAppService {
  
  override val name = "notifier"
    
  val service = {
    path("notification" / "registered" ) {
      get { context =>
        val uris = (_registry retrieve(List())) map { s => 
          "/notification/registered/" + s.sensor
        }
        context complete uris
      } ~
      post {
        content(as[Subscription]) { subscription => context =>
          if (_registry exists ("sensor", subscription.sensor)){
            context fail (StatusCodes.Conflict, "A Subscription identified by ["+ subscription.sensor +"] already exists!")
          } else {
            _registry push subscription
            context complete (StatusCodes.Created, "/notification/registered/" + subscription.sensor)
          }
        }
      }
    } ~
    path("notification" / "registered" / Standard.NAME_VALIDATOR.r) { name =>
      get { context =>
        ifExists(context, name, {context complete (_registry pull ("sensor", name)).get})
      } ~
      delete { context =>
        ifExists(context, name, {  
          val subscr = (_registry pull ("sensor", name)).get
          _registry drop subscr 
          context complete "true"
        })
      } ~
      put {
        content(as[Subscription]) { subscription => context => 
          if (subscription.sensor != name) {
            context fail(StatusCodes.Conflict, "Request content does not match URL for update")
          } else {
            ifExists(context, name, { _registry push(subscription); context complete("true") })
	      } 
        }
      }
    }
  }
  
  private[this] val _registry = new SubscriptionRegistry()
  
  private def ifExists(context: RequestContext, id: String, lambda: => Unit) = {
    if (_registry exists ("sensor", id))
      lambda
    else
      context fail(StatusCodes.NotFound, "Unknown sensor database [" + id + "]") 
  } 
  
}