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

package io.deepsense.deeplang.doperables.spark.wrappers.transformers

import org.apache.spark.sql.types.{ArrayType, DataType, StringType}

import io.deepsense.deeplang.doperables.multicolumn.MultiColumnParams.SingleOrMultiColumnChoices.SingleColumnChoice
import io.deepsense.deeplang.doperables.multicolumn.SingleColumnParams.SingleTransformInPlaceChoices.NoInPlaceChoice
import io.deepsense.deeplang.params.selections.NameSingleColumnSelection

class StringTokenizerSmokeTest
  extends AbstractTransformerWrapperSmokeTest[StringTokenizer]
  with MultiColumnTransformerWrapperTestSupport {

  override def transformerWithParams: StringTokenizer = {
     val inPlace = NoInPlaceChoice()
      .setOutputColumn("tokenized")

    val single = SingleColumnChoice()
      .setInputColumn(NameSingleColumnSelection("s"))
      .setInPlace(inPlace)

    val transformer = new StringTokenizer()
    transformer.set(Seq(
      transformer.singleOrMultiChoiceParam -> single
    ): _*)
  }

  override def testValues: Seq[(Any, Any)] = {
    val strings = Seq(
      "this is a test",
      "this values should be separated",
      "Bla bla bla!"
    )

    val tokenized = strings.map { _.toLowerCase.split("\\s") }
    strings.zip(tokenized)
  }

  override def inputType: DataType = StringType

  override def outputType: DataType = new ArrayType(StringType, true)
}
