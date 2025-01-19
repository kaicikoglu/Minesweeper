package kafkaProducer

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.collection.mutable.ListBuffer

class Producer(val records: ListBuffer[ProducerRecord[String, String]]) {
  implicit val system: ActorSystem = ActorSystem("QuickStart")

  val bootstrapServers = "localhost:9092"

  private val producerSettings =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  Source(records.toList)
    .runWith(Producer.plainSink(producerSettings))
}