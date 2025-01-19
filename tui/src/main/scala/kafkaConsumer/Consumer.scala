package kafkaConsumer

import FieldComponent.FieldBaseImpl.{DifficultyFactory, Stone}
import FieldComponent.FieldInterface
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

class Consumer(updateTUI: FieldInterface => Unit)(implicit system: ActorSystem) {

  private val bootstrapServers = "localhost:9092"
  private val groupId = "tui-consumer-group"
  private val topic = "field-topic"

  private val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId(groupId)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")


  var field: FieldInterface = DifficultyFactory.apply("1").run

  // kafka-console-consumer --bootstrap-server localhost:9092 --topic field-topic
  Consumer
    .plainSource(consumerSettings, Subscriptions.topics(topic))
    .map(record => (record.key(), record.value()))
    .to(Sink.foreach { case (key, value) =>
      key match {
        case "end" =>
          println("Spiel beendet!")
          updateTUI(field) // Aktualisiere die UI
        case _ => // Schlüssel im Format "i-j"
          val coordinates = key.split("-").map(_.toInt)
          val stoneValues = value.split(",")
          val cell = (
            parseStone(stoneValues(0).trim),
            parseStone(stoneValues(1).trim),
            stoneValues(2).trim.toInt
          )
          field = field.setCell(coordinates(0), coordinates(1), cell)
      }
    })
    .run()

  private def parseStone(value: String): Stone = {
    value match {
      case "□" => Stone.EmptyTracked
      case "■" => Stone.NotTracked
      case "✴" => Stone.Bomb
      case "⚑" => Stone.Flag
      case "1" => Stone.One
      case "2" => Stone.Two
      case "3" => Stone.Three
      case "4" => Stone.Four
      case "5" => Stone.Five
      case "6" => Stone.Six
      case "7" => Stone.Seven
      case "8" => Stone.Eight
      case _ => throw new IllegalArgumentException(s"Unbekanntes Stone-Symbol: $value")
    }
  }
}
