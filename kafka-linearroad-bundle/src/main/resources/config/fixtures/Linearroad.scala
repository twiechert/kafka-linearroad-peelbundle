package config.fixtures

import com.typesafe.config.ConfigFactory
import de.twiechert.peel.extension.experimen.java.{KafkaExperiment}
import de.twiechert.peel.extension.kafka.Kafka
import org.peelframework.core.beans.data.{CopiedDataSet, DataSet, ExperimentOutput, GeneratedDataSet}
import org.peelframework.core.beans.experiment.ExperimentSequence.SimpleParameters
import org.peelframework.core.beans.experiment.{Experiment, ExperimentSequence, ExperimentSuite}
import org.peelframework.flink.beans.job.FlinkJob
import org.peelframework.flink.beans.system.Flink
import org.peelframework.hadoop.beans.system.HDFS2
import org.peelframework.spark.beans.experiment.SparkExperiment
import org.peelframework.spark.beans.system.Spark
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

/** `WordCount` experiment fixtures for the 'kafka-Linearroad' bundle. */
@Configuration
class Linearroad extends ApplicationContextAware {

  /* The enclosing application context. */
  var ctx: ApplicationContext = null

  def setApplicationContext(ctx: ApplicationContext): Unit = {
    this.ctx = ctx
  }


  // ---------------------------------------------------
  // Experiments
  // ---------------------------------------------------

  @Bean(name = Array("Linearroad.kafka"))
  def `linearroad.kafka`: ExperimentSuite =  {
    val `linearroad.kafka.default` = new KafkaExperiment(
      name    = "Linearroad.kafka.default",
      command =
        """
          java -version
        """.stripMargin.trim,
      config  = ConfigFactory.parseString(""),
      runs    = 1,
      runner  = ctx.getBean("kafka-0.10.0.1", classOf[Kafka])
     // inputs  = Set(ctx.getBean("dataset.words.static", classOf[DataSet])),
     // outputs = Set(ctx.getBean("wordcount.output", classOf[ExperimentOutput]))
    )


    new ExperimentSuite(Seq(
      `linearroad.kafka.default`))
  }



  // ---------------------------------------------------
  // Data Generators
  // ---------------------------------------------------
/*
  @Bean(name = Array("datagen.words"))
  def `datagen.words`: FlinkJob = new FlinkJob(
    runner  = ctx.getBean("flink-1.0.3", classOf[Flink]),
    command =
      """
        |-v -c de.twiechert.benchmarks.Linearroad.datagen.flink.WordGenerator        \
        |${app.path.datagens}/kafka-Linearroad-datagens-1.0-SNAPSHOT.jar        \
        |${system.default.config.parallelism.total}                           \
        |${datagen.tuples.per.task}                                           \
        |${datagen.dictionary.dize}                                           \
        |${datagen.data-distribution}                                         \
        |${system.hadoop-2.path.input}/rubbish.txt
      """.stripMargin.trim
  )
*/
  // ---------------------------------------------------
  // Data Sets
  // ---------------------------------------------------

  /*
  @Bean(name = Array("dataset.words.static"))
  def `dataset.words.static`: DataSet = new CopiedDataSet(
    src = "${app.path.datasets}/rubbish.txt",
    dst = "${system.hadoop-2.path.input}/rubbish.txt",
    fs  = ctx.getBean("hdfs-2.7.1", classOf[HDFS2])
  )

  @Bean(name = Array("dataset.words.generated"))
  def `dataset.words.generated`: DataSet = new GeneratedDataSet(
    src = ctx.getBean("datagen.words", classOf[FlinkJob]),
    dst = "${system.hadoop-2.path.input}/rubbish.txt",
    fs  = ctx.getBean("hdfs-2.7.1", classOf[HDFS2])
  )

  @Bean(name = Array("wordcount.output"))
  def `wordcount.output`: ExperimentOutput = new ExperimentOutput(
    path = "${system.hadoop-2.path.output}/wordcount",
    fs  = ctx.getBean("hdfs-2.7.1", classOf[HDFS2])
  )
*/

}