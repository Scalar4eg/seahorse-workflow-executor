/**
 * Copyright 2015, CodiLime Inc.
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

package io.deepsense.commons.exception.json

import io.deepsense.commons.exception.{FailureCode, DeepSenseFailure, FailureDescription}
import io.deepsense.commons.json.{EnumerationSerializer, UUIDJsonProtocol}

import spray.json._

trait FailureDescriptionJsonProtocol extends DefaultJsonProtocol with UUIDJsonProtocol {
  implicit val failureCodeFormat = EnumerationSerializer.jsonEnumFormat(FailureCode)
  implicit val failureIdFormat = jsonFormat1(DeepSenseFailure.Id.apply)
  implicit val failureDescriptionFormat = jsonFormat5(FailureDescription.apply)
}