package de.twiechert.peel.extension.experimen.java

import java.io.FileWriter
import java.nio.file._

import com.typesafe.config.Config
import de.twiechert.peel.extension.kafka.Kafka
import org.peelframework.core.beans.data.{DataSet, ExperimentOutput}
import org.peelframework.core.beans.experiment.Experiment
import org.peelframework.core.beans.system.System
import org.peelframework.core.util.shell
import org.peelframework.spark.beans.system.Spark
import spray.json._

/** An `Expriment` implementation which handles the execution of a single Spark job. */
class KafkaExperiment(
                      command: String,
                      systems: Set[System],
                      runner : Kafka,
                      runs   : Int,
                      //inputs : Set[DataSet],
                    //  outputs: Set[ExperimentOutput],
                      name   : String,
                      config : Config) extends Experiment(command, systems, runner, runs, null, null, name, config) {

  def this(
            command: String,
            runner : Kafka,
            runs   : Int,
          //  inputs : Set[DataSet],
          //  outputs: Set[ExperimentOutput],
            name   : String,
            config : Config) = this(command, Set.empty[System], runner, runs, name, config)

  override def run(id: Int, force: Boolean): Experiment.Run[Kafka] = {
    new KafkaExperiment.SingleJobRun(id, this, force)
  }

  def copy(name: String = name, config: Config = config) = {
    new KafkaExperiment(command, systems, runner, runs, name, config)
  }
}

object KafkaExperiment {

  case class State(
                    command         : String,
                    runnerID        : String,
                    runnerName      : String,
                    runnerVersion   : String,
                    var runExitCode : Option[Int] = None,
                    var runTime     : Long = 0) extends Experiment.RunState

  object StateProtocol extends DefaultJsonProtocol with NullOptions {
    implicit val stateFormat = jsonFormat6(State)
  }

  /**
    * A private inner class encapsulating the logic of single run.
    */
  class SingleJobRun(val id: Int, val exp: KafkaExperiment, val force: Boolean) extends Experiment.SingleJobRun[Kafka, State] {

    import org.peelframework.spark.beans.experiment.SparkExperiment.StateProtocol._

    val runnerLogPath = exp.config.getString(s"system.${exp.runner.configKey}.path.log")

    override def isSuccessful = state.runExitCode.getOrElse(-1) == 0

    override protected def loadState(): State = {

        State(command, exp.runner.beanName, exp.runner.name, exp.runner.version)

    }

    override protected def writeState() = {
     // val fw = new FileWriter(s"$home/state.json")
   //   fw.write(state.toJson.prettyPrint)
    //  fw.close()
    }

    override protected def runJob() = {
      // try to execute the experiment run plan
      val (runExit, t) = Experiment.time(this !(command, s"$home/run.out", s"$home/run.err"))
      state.runTime = t
      state.runExitCode = Some(runExit)
    }

    override def cancelJob() = {

    }

    private def !(command: String, outFile: String, errFile: String) = {
      shell ! command.trim
    }
  }

}