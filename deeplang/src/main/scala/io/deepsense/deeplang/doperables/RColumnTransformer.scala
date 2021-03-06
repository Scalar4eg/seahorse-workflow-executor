/**
 * Copyright 2016, deepsense.io
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

package io.deepsense.deeplang.doperables

import java.util.UUID

import io.deepsense.deeplang.ExecutionContext
import io.deepsense.deeplang.OperationExecutionDispatcher.Result
import io.deepsense.deeplang.params.{CodeSnippetLanguage, CodeSnippetParam, Param}
import org.apache.spark.sql.types.DataType


class RColumnTransformer() extends CustomCodeColumnTransformer {

  override val codeParameter = CodeSnippetParam(
    name = "column operation code",
    description = None,
    language = CodeSnippetLanguage(CodeSnippetLanguage.r)
  )
  setDefault(codeParameter ->
    """transform.column <- function(column, column.name) {
      |  return(column)
      |}""".stripMargin
  )

  override def getSpecificParams: Array[Param[_]] =
    Array(codeParameter, targetType)

  override def getComposedCode(
      userCode: String,
      inputColumn: String,
      outputColumn: String,
      targetType: DataType): String = {
    val newFieldName = UUID.randomUUID().toString.replace("-", "")

    s"""
       |$userCode
       |
       |transform <- function(dataframe) {
       |  new.column <- cast(transform.column(dataframe$$'$inputColumn', '$inputColumn'),
       |    '${targetType.simpleString}')
       |  return(withColumn(dataframe, '$newFieldName', new.column))
       |}
    """.stripMargin
  }

  override def runCode(context: ExecutionContext, code: String): Result =
    context.customCodeExecutor.runR(code)

  override def isValid(context: ExecutionContext, code: String): Boolean =
    context.customCodeExecutor.isRValid(code)
}
