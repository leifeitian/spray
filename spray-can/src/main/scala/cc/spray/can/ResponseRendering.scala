/*
 * Copyright (C) 2011, 2012 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.spray
package can

import rendering.{HttpResponsePartRenderingContext, ResponseRenderer}
import io._
import akka.actor.ActorContext

object ResponseRendering {

  def apply(serverHeader: String) = new CommandPipelineStage {
    val renderer = new ResponseRenderer(serverHeader)

    def build(context: ActorContext, commandPL: Pipeline[Command], eventPL: Pipeline[Event]) = {
      case ctx: HttpResponsePartRenderingContext =>
        val rendered = renderer.render(ctx)
        commandPL(IoPeer.Send(rendered.buffers))
        if (rendered.closeConnection) {
          commandPL(IoPeer.Close(ProtocolClose))
        }

      case cmd => commandPL(cmd)
    }
  }
}