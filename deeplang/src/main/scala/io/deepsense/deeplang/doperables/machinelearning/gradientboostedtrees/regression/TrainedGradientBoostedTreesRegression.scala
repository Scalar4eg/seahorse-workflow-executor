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

package io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.regression

import org.apache.spark.mllib.linalg
import org.apache.spark.mllib.tree.model.GradientBoostedTreesModel
import org.apache.spark.rdd.RDD

import io.deepsense.commons.types.ColumnType
import io.deepsense.deeplang.doperables._
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.machinelearning.gradientboostedtrees.GradientBoostedTreesParameters
import io.deepsense.deeplang.{DOperable, ExecutionContext}
import io.deepsense.reportlib.model.{ReportContent, Table}

case class TrainedGradientBoostedTreesRegression(
    modelParameters: GradientBoostedTreesParameters,
    model: GradientBoostedTreesModel,
    featureColumns: Seq[String],
    targetColumn: String)
  extends GradientBoostedTreesRegressor
  with Scorable
  with VectorScoring
  with DOperableSaver {

  def this() = this(null, null, null, null)

  override def toInferrable: DOperable = new TrainedGradientBoostedTreesRegression()

  override def url: Option[String] = None

  override def report(executionContext: ExecutionContext): Report = {
    DOperableReporter("Trained Gradient Boosted Trees Regression")
      .withParameters(
        description = model.toString,
        ("Num iterations", ColumnType.numeric, modelParameters.numIterations.toString),
        ("Loss", ColumnType.string, modelParameters.loss),
        ("Impurity", ColumnType.string, modelParameters.impurity),
        ("Max depth", ColumnType.numeric, modelParameters.maxDepth.toString),
        ("Max bins", ColumnType.numeric, modelParameters.maxBins.toString)
      )
      .withVectorScoring(this)
      .report
  }

  override def save(context: ExecutionContext)(path: String): Unit = ???

  override def vectors(dataFrame: DataFrame): RDD[linalg.Vector] =
    dataFrame.selectSparkVectorRDD(featureColumns, ColumnTypesPredicates.isNumericOrCategorical)

  override def predict(vectors: RDD[linalg.Vector]): RDD[Double] = model.predict(vectors)

  override def transformFeatures(v: RDD[linalg.Vector]): RDD[linalg.Vector] = v

}
