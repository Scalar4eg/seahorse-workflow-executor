/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.params.custom

import spray.json.JsObject

import io.deepsense.deeplang.DOperation
import io.deepsense.deeplang.doperations.custom.{Sink, Source}
import io.deepsense.graph.DeeplangGraph.DeeplangNode
import io.deepsense.graph.{Node, DeeplangGraph}

case class InnerWorkflow(
   graph: DeeplangGraph,
   thirdPartyData: JsObject,
   publicParams: List[PublicParam] = List.empty) {

  require(findNodeOfType(Source.id).isDefined, "Inner workflow must have source node")
  require(findNodeOfType(Sink.id).isDefined, "Inner workflow must have sink node")

  val source: DeeplangNode = findNodeOfType(Source.id).get
  val sink: DeeplangNode = findNodeOfType(Sink.id).get

  private def findNodeOfType(operationId: DOperation.Id): Option[DeeplangNode] = {
    graph.nodes.find(_.value.id == operationId)
  }

}

object InnerWorkflow {
  val empty = InnerWorkflow(
    DeeplangGraph(Set(Node(Node.Id.randomId, Source()), Node(Node.Id.randomId, Sink()))),
    JsObject())
}

case class PublicParam(nodeId: Node.Id, paramName: String, publicName: String)
