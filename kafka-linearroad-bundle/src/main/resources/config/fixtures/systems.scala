package config.fixtures

import com.samskivert.mustache.Mustache
import com.samskivert.mustache.Mustache.Compiler
import de.twiechert.peel.extension.kafka.Kafka
import org.peelframework.core.beans.system.Lifespan
import org.peelframework.flink.beans.system.Flink
import org.peelframework.hadoop.beans.system.HDFS2
import org.peelframework.spark.beans.system.Spark
import org.peelframework.zookeeper.beans.system.Zookeeper
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.context.{ApplicationContext, ApplicationContextAware}

/** System beans for the 'kafka-Linearroad' bundle. */
@Configuration
class systems extends ApplicationContextAware {

  /* The enclosing application context. */
  var ctx: ApplicationContext = null

  def setApplicationContext(ctx: ApplicationContext): Unit = {
    this.ctx = ctx
  }

  // ---------------------------------------------------
  // Systems
  // ---------------------------------------------------

  @Bean(name = Array("kafka-0.10.0"))
  def `kafka-0.10.0`: Kafka = new Kafka(
    version      = "0.10.0",
    configKey    = "kafka",
    lifespan     = Lifespan.EXPERIMENT,
    dependencies = Set(ctx.getBean("zookeeper-3.4.5", classOf[Zookeeper])),
    mc           = ctx.getBean(classOf[Compiler])
  )

  @Bean(name = Array("zookeeper-3.4.5"))
  def `zookeeper-3.4.5`: Zookeeper = new Zookeeper(
    version      = "3.4.5",
    configKey    = "zookeeper",
    lifespan     = Lifespan.EXPERIMENT,
    mc           = ctx.getBean(classOf[Compiler])
  )


}