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

package io.deepsense.deeplang.doperations

import scala.reflect.runtime.{universe => ru}

import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.rdd.RDD

import io.deepsense.commons.utils.DoubleUtils
import io.deepsense.deeplang.DOperation.Id
import io.deepsense.deeplang.doperables.dataframe.DataFrame
import io.deepsense.deeplang.doperables.{Evaluator, Report}
import io.deepsense.deeplang.parameters.NameSingleColumnSelection
import io.deepsense.reportlib.model.{ReportContent, Table}

case class EvaluateClassification() extends Evaluator {

  override val name: String = "Evaluate Classification"

  override val id: Id = "1163bb76-ba65-4471-9632-dfb761d20dfb"

  override val parameters = evaluatorParameters

  override protected def report(
      dataFrame: DataFrame,
      predictionsAndLabels: RDD[(Double, Double)]): Report = {
    logger.debug("Computing DataFrame size")
    val dataFrameSize = dataFrame.sparkDataFrame.count()
    logger.debug("Preparing BinaryClassificationMetrics object")
    val metrics =
      new BinaryClassificationMetrics(predictionsAndLabels, EvaluateClassification.MetricsNumBins)

    logger.debug("Computing LogarithmicLoss metric")
    val logLossSum = predictionsAndLabels.map {
      case (prediction, label) =>
        label * math.log(prediction) + (1.0 - label) * math.log(1.0 - prediction)
    }.sum()
    val logLoss = logLossSum * -1.0 / dataFrameSize

    logger.debug("Computing accuracyByThreshold metric")
    // TODO: This implementation of accuracy computing is inefficient
    val predictionsAndBooleanLabels =
      predictionsAndLabels.map { case (prediction, label) => (prediction, label < 0.5) }
    predictionsAndBooleanLabels.cache()
    val accuracyByThreshold = metrics.thresholds().collect().map {
      threshold =>
        (threshold, accuracyForThreshold(threshold, predictionsAndBooleanLabels, dataFrameSize))
    }
    predictionsAndBooleanLabels.unpersist()
    val accuracyTable = Table(
      "accuracy",
      "Accuracy",
      Some(
        List(
          "Threshold",
          "Accuracy")),
      None,
      reportTableValues(accuracyByThreshold)
    )

    logger.debug("Computing fMeasureByThreshold metric")
    val fMeasureByThresholdTable = Table(
      "fMeasureByThreshold",
      "F-Measure (F1 score) by threshold",
      Some(
        List(
          "Threshold",
          "F-Measure")),
      None,
      reportTableValues(metrics.fMeasureByThreshold().collect())
    )

    logger.debug("Computing Receiver Operating Characteristic curve")
    val rocTable = Table(
      "roc",
      "Receiver Operating Characteristic curve",
      Some(
        List(
          "False positive rate",
          "True positive rate")),
      None,
      reportTableValues(metrics.roc().collect())
    )

    logger.debug("Preparing summary table")
    val summaryTable = Table(
      "summary",
      "Evaluate classification summary",
      Some(
        List(
          "DataFrame size",
          "AUC",
          "Logarithmic Loss")),
      None,
      List(
        List(
          Some(dataFrameSize.toString),
          Some(DoubleUtils.double2String(metrics.areaUnderROC())),
          Some(DoubleUtils.double2String(logLoss))
        ))
    )

    logger.debug("Assembling evaluation report")
    val report = Report(ReportContent(
      EvaluateClassification.ReportName,
      Map(
        summaryTable.name -> summaryTable,
        accuracyTable.name -> accuracyTable,
        fMeasureByThresholdTable.name -> fMeasureByThresholdTable,
        rocTable.name -> rocTable)
    ))

    import spray.json._
    import io.deepsense.reportlib.model.ReportJsonProtocol._
    logger.debug("EvaluateClassification report = " + report.content.toJson.prettyPrint)
    report
  }

  private def accuracyForThreshold(
      threshold: Double,
      predictionsAndBooleanLabels: RDD[(Double, Boolean)],
      dataFrameSize: Long): Double = {
    val eps = EvaluateClassification.Epsilon
    predictionsAndBooleanLabels.filter { case (prediction, label) =>
      (prediction < threshold) == label
    }.count() * 1.0 / dataFrameSize
  }

  private def reportTableValues(valuesRdd: Array[(Double, Double)]): List[List[Option[String]]] = {
    valuesRdd.map {
      case (a: Double, b: Double) =>
        List(Some(DoubleUtils.double2String(a)), Some(DoubleUtils.double2String(b)))
    }.toList
  }

  @transient
  override lazy val tTagTI_0: ru.TypeTag[DataFrame] = ru.typeTag[DataFrame]
  @transient
  override lazy val tTagTO_0: ru.TypeTag[Report] = ru.typeTag[Report]
}

object EvaluateClassification {

  private val Epsilon = 0.01

  private val MetricsNumBins = 10

  val ReportName = "Evaluate Classification Report"

  def apply(
      targetColumnName: String,
      predictionColumnName: String): EvaluateClassification = {
    val operation = new EvaluateClassification
    val targetColumnParam =
      operation.parameters.getSingleColumnSelectorParameter(Evaluator.targetColumnParamKey)
    targetColumnParam.value = Some(NameSingleColumnSelection(targetColumnName))
    val predictionColumnParam =
      operation.parameters.getSingleColumnSelectorParameter(Evaluator.predictionColumnParamKey)
    predictionColumnParam.value = Some(NameSingleColumnSelection(predictionColumnName))
    operation
  }
}