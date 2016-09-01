package de.twiechert.peel.extension.kafka

import java.nio.file.Paths

import com.samskivert.mustache.Mustache
import org.peelframework.core.beans.system.Lifespan.Lifespan
import org.peelframework.core.beans.system.System
import org.peelframework.core.config.{Model, SystemConfig}
import org.peelframework.core.util.shell

import scala.collection.JavaConverters._
/**
  * Created by tafyun on 31.07.16.
  */
class Kafka(
                 version      : String,
                 configKey    : String,
                 lifespan     : Lifespan,
                 dependencies : Set[System] = Set(),
                 mc           : Mustache.Compiler) extends System("kafka", version, configKey, lifespan, dependencies, mc) {

  override def configuration() = SystemConfig(config, {
    val conf = config.getString(s"system.$configKey.path.config")
    List(
      SystemConfig.Entry[Model.Site](s"system.$configKey.config", s"$conf/server.properties", templatePath("conf/kafka.cfg"), mc)
    )
  })

  override def start(): Unit = {
    if (!isUp) {
      this.servers.foreach(start)
      isUp = true
    }
  }

  override def stop(): Unit = this.servers.foreach(stop)

  def isRunning = this.servers.forall(s => isRunning(s))

  //def cli = new Zookeeper.Cli(config.getString(s"system.$configKey.path.home"), servers.head.host, config.getInt(s"system.$configKey.config.clientPort"))

  private def start(s: Kafka.Server) = {
    logger.info(s"Starting Kafka at ${s.host}")
    val user = config.getString(s"system.$configKey.user")
    shell !
      s"""
         |ssh -t -t "$user@${s.host}" << SSHEND
         |  ${config.getString(s"system.$configKey.path.home")}/bin/kafka-server-start.sh  -daemon ${config.getString(s"system.$configKey.path.config")}/server.properties
         |  exit
         |SSHEND
      """.stripMargin.trim
  }

  private def stop(s: Kafka.Server) = {
    logger.info(s"Stopping Kafka at ${s.host}")
    val user = config.getString(s"system.$configKey.user")
    shell !
      s"""
         |ssh -t -t "$user@${s.host}" << SSHEND
         |  ${config.getString(s"system.$configKey.path.home")}/bin/kafka-server-stop.sh
         |  ; rm -r ${config.getString(s"system.$configKey.path.log")}; mkdir ${config.getString(s"system.$configKey.path.log")}
         |  exit
         |SSHEND
      """.stripMargin.trim

  }

  private def isRunning(s: Kafka.Server) = {
    logger.info(s"${Paths.get(config.getString(s"system.$configKey.path.home"))}")

    val user = config.getString(s"system.$configKey.user")

    logger.info(s"checking if Kafka is running at ${s.host} using user ${user}")
    val exitcode =  shell ! s""" ssh $user@${s.host} "ps ax | grep -i 'kafka\\.Kafka'" """
    logger.info(s"exitcode is ${exitcode}")
    exitcode == 0
  }

  private def servers = {
    // grab servers from config
    val serverConfigs = config.getConfig(s"system.$configKey.config.server").entrySet().asScala.map(v => v.getKey.substring(1, v.getKey.length() - 1) + ":" + v.getValue.unwrapped().toString)
    // match and return valid server configs
    serverConfigs.collect({
      case Kafka.ServerConf(id, host) => Kafka.Server(id.toInt, host)
    })
  }
}

object Kafka {

  val ServerConf = "(\\d+):([\\w\\-\\_\\.]+)".r

  case class Server(id: Int, host: String)

  /*
  class Cli(home: String, serverHost: String, serverPort: Int) {

    def !(cmd: String) = shell ! s"$home/bin/zkCli.sh -server $serverHost:$serverPort $cmd"

    def !!(cmd: String) = shell !! s"$home/bin/zkCli.sh -server $serverHost:$serverPort $cmd"
  }
*/
}