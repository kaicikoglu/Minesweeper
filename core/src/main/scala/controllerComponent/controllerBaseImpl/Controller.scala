package controllerComponent.controllerBaseImpl

import FieldComponent.*
import FieldComponent.FieldBaseImpl.*
import FileIOComponent.FileIOInterface
import FileIOComponent.fileIoJsonImpl.FileIOJson
import com.google.inject.{Guice, Inject, Injector}
import controllerComponent.ControllerInterface
import controllerComponent.controllerBaseImpl.*
import kafkaProducer.Producer
import lib.Servers.{modelServer, persistenceServer}
import lib.{Event, Observable, UndoManager}
import org.apache.kafka.clients.producer.ProducerRecord
import play.api.libs.json.{JsObject, JsValue, Json}

import java.net.{HttpURLConnection, URI, URLEncoder}
import scala.collection.mutable.ListBuffer
import scala.io.Source

case class Controller @Inject()(var field: FieldInterface) extends ControllerInterface with Observable:
  val file: Injector = Guice.createInjector(new MinesweeperJson)
  private val undoManager = new UndoManager[FieldInterface]
  private val fileIO = file.getInstance(classOf[FileIOInterface])

  def createNewField(string: String): FieldInterface = {
    string match {
      case "1" | "2" | "3" =>
        field = createNewFieldApi(string)
        field
      case _ =>
        throw new IllegalArgumentException(s"Invalid difficulty level: $string")
    }
  }

  private def createNewFieldApi(difficulty: String): FieldInterface = {
    try {
      val encodedDifficulty = URLEncoder.encode(difficulty, "UTF-8")
      val uri = new URI(s"http://$modelServer/field/createNew?x=$encodedDifficulty")
      val url = uri.toURL

      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("POST")
      connection.setDoOutput(true)

      val inputStream = connection.getInputStream
      val result = Source.fromInputStream(inputStream).mkString
      inputStream.close()

      field = field.jsonToField(result)
      field
    } catch {
      case ex: Exception =>
        println(s"Failed to create new field: ${ex.getMessage}")
        field // Optionally, you might return a default or empty field here
    }
  }

  def calculateBombAmount(): Int =
    calculateBombAmountApi match {
      case Some(bombAmount) =>
        bombAmount
      case None => 0
    }

  private def calculateBombAmountApi: Option[Int] = {
    val uri = new URI(s"http://$modelServer/field/calculateBombs")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    val responseCode = connection.getResponseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
      val inputStream = connection.getInputStream
      val response = Source.fromInputStream(inputStream).getLines().mkString
      inputStream.close()
      val json = Json.parse(response)
      Some((json \ "bombAmount").as[Int])
    } else {
      println(s"Failed to get bomb amount from server: HTTP response code $responseCode")
      None
    }
  }

  def setBombs(bombAmount: Int): FieldInterface =
    setBombsApi(bombAmount) match {
      case Some(_) =>
        field = field.setBombs(bombAmount)
        notifyObservers(Event.Move)
        field
      case None => field
    }

  private def setBombsApi(bombAmount: Int): Option[String] = {
    val uri = new URI(s"http://$modelServer/field/setBombs")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")

    val requestBody = s"""{"bombAmount": $bombAmount}"""
    connection.getOutputStream.write(requestBody.getBytes("UTF-8"))

    val responseCode = connection.getResponseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
      val inputStream = connection.getInputStream
      val response = Source.fromInputStream(inputStream).getLines().mkString
      inputStream.close()
      Some(response)
    } else {
      println(s"Failed to set bombs on server: HTTP response code $responseCode")
      None
    }
  }

  def doAndPublish(coordinates: Coordinates): FieldInterface =
    field = revealValue(coordinates)

    val records = new ListBuffer[ProducerRecord[String, String]]()

    records += new ProducerRecord("field-topic", "rows", field.rows.toString)
    records += new ProducerRecord("field-topic", "cols", field.cols.toString)

    for (i <- 0 until field.rows)
      for (j <- 0 until field.cols)
        records += new ProducerRecord("field-topic", s"$i-$j", field.getCell(i, j).toString())

    records += new ProducerRecord("field-topic", "end", "end")

    Producer(records)
    field

    // notifyObservers(Event.Move)

  def doAndPublish(doThis: => FieldInterface): Unit =
    field = doThis
    // notifyObservers(Event.Move)

  def quit(): Unit = notifyObservers(Event.Quit)

  def revealValue(move: Coordinates): FieldInterface =
    revealValueApi(move) match {
      case Some(updatedFieldJson) =>
        field = field.jsonToField(updatedFieldJson)
        field
      case None =>
        field
    }

  private def revealValueApi(move: Coordinates): Option[String] = {
    val uri = new URI(s"http://$modelServer/field/revealValue")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")

    // Prepare the JSON payload with the move coordinates
    val jsonPayload = Json.obj("x" -> move.x, "y" -> move.y).toString

    // Send the POST request
    val outputStream = connection.getOutputStream
    outputStream.write(jsonPayload.getBytes("UTF-8"))
    outputStream.close()

    // Process the response
    val responseCode = connection.getResponseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
      val inputStream = connection.getInputStream
      val response = Source.fromInputStream(inputStream).getLines().mkString
      inputStream.close()
      Some(response)
    } else {
      println(s"Failed to reveal value on server: HTTP response code $responseCode")
      None
    }
  }

  def undo: FieldInterface =
    undoManager.undoStep(field)

  def redo: FieldInterface =
    undoManager.redoStep(field)

  def noStep(move: Coordinates): FieldInterface =
    undoManager.noStep(field, DoCommand(move))

  def setFlag(coordinates: Coordinates): FieldInterface =
    setFlagApi(coordinates) match {
      case Some(updatedFieldJson) =>
        field = field.jsonToField(updatedFieldJson)
        field
      case None =>
        field
    }

  private def setFlagApi(coordinates: Coordinates): Option[String] = {
    val uri = new URI(s"http://$modelServer/field/setFlag")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")

    // Prepare the JSON payload with the coordinates
    val jsonPayload = Json.obj("x" -> coordinates.x, "y" -> coordinates.y).toString

    // Send the POST request
    val outputStream = connection.getOutputStream
    outputStream.write(jsonPayload.getBytes("UTF-8"))
    outputStream.close()

    // Process the response
    val responseCode = connection.getResponseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
      val inputStream = connection.getInputStream
      val response = Source.fromInputStream(inputStream).getLines().mkString
      inputStream.close()
      Some(response)
    } else {
      println(s"Failed to set flag on server: HTTP response code $responseCode")
      None
    }
  }

  def save: FieldInterface =
    saveApi()
    field

  private def saveApi(): Unit = {
    // Prepare the JSON payload
    val jsonPayload = Json.obj("field" -> field.toJson).toString
    val uri = new URI(s"http://$persistenceServer/fileIo")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    try {
      // Set up the connection properties
      connection.setRequestMethod("POST")
      connection.setDoOutput(true)
      connection.setRequestProperty("Content-Type", "application/json; utf-8")
      connection.setRequestProperty("Accept", "application/json")

      // Write the JSON payload to the connection's output stream
      val outputStream = connection.getOutputStream
      outputStream.write(jsonPayload.getBytes("UTF-8"))
      outputStream.flush()
      outputStream.close()

      // Read and process the server's response
      val responseCode = connection.getResponseCode
      if (responseCode == HttpURLConnection.HTTP_OK) {
        println("Field saved successfully")
      } else {
        val errorStream = connection.getErrorStream
        if (errorStream != null) {
          val errorMessage = Source.fromInputStream(errorStream).mkString
          println(s"Failed to save field: $errorMessage")
        } else {
          println(s"Failed to save field to server: HTTP response code $responseCode")
        }
      }
    } catch {
      case e: Exception =>
        println(s"An error occurred while trying to save the field: ${e.getMessage}")
    } finally {
      connection.disconnect() // Ensure the connection is properly closed
    }
  }

  def load: FieldInterface =
    loadApi match {
      case Some(loadedField) => loadedField
      case None => field
    }

  private def loadApi: Option[FieldInterface] = {
    val uri = new URI(s"http://$persistenceServer/fileIo/load")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]

    try {
      // Set up the connection properties
      connection.setRequestMethod("GET")
      connection.setRequestProperty("Accept", "application/json, text/plain")

      // Get the response code and handle the response
      val responseCode = connection.getResponseCode
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // Check the Content-Type of the response
        val contentType = connection.getContentType

        // Read and handle the response based on Content-Type
        val inputStream = connection.getInputStream
        val response = Source.fromInputStream(inputStream).mkString
        inputStream.close()

        if (contentType.contains("application/json")) {
          // Parse JSON response
          field = field.jsonToField(response)
          Some(field)
        } else {
          // Handle text/plain or other content types
          println(s"Received non-JSON response: $response")
          None
        }
      } else {
        // Handle non-OK responses by reading the error stream if available
        val errorStream = connection.getErrorStream
        if (errorStream != null) {
          val errorMessage = Source.fromInputStream(errorStream).mkString
          println(s"Failed to load field: $errorMessage")
        } else {
          println(s"Failed to load field from server: HTTP response code $responseCode")
        }
        None
      }
    } catch {
      case e: Exception =>
        println(s"An error occurred while trying to load the field: ${e.getMessage}")
        None
    } finally {
      connection.disconnect() // Ensure the connection is properly closed
    }
  }


  def flagsLeft(): Int =
    flagsLeftApi match {
      case Some(flagsLeft) => flagsLeft
      case None => 0
    }

  private def flagsLeftApi: Option[Int] = {
    val uri = new URI(s"http://$modelServer/field/flagsLeft")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    val responseCode = connection.getResponseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
      val inputStream = connection.getInputStream
      val response = Source.fromInputStream(inputStream).getLines().mkString
      inputStream.close()
      val json = Json.parse(response)
      Some((json \ "flagsLeft").as[Int])
    } else {
      println(s"Failed to get flags left from server: HTTP response code $responseCode")
      None
    }
  }

  override def getCell(x: Int, y: Int): (Stone, Stone, Int) =
    getCellApi(x, y) match {
      case Some(cell) =>
        val json = Json.parse(cell)
        val first = (json \ "first").as[String]
        val second = (json \ "second").as[String]
        val third = (json \ "third").as[Int]
        (Stone.valueOf(first), Stone.valueOf(second), third)
      case None => (Stone.NotTracked, Stone.NotTracked, 0)
    }

  private def getCellApi(x: Int, y: Int): Option[String] = {
    val uri = new URI(s"http://$modelServer/field/getCell?x=$x&y=$y")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    val responseCode = connection.getResponseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
      val inputStream = connection.getInputStream
      val response = Source.fromInputStream(inputStream).getLines().mkString
      inputStream.close()
      Some(response)
    } else {
      println(s"Failed to get cell from server: HTTP response code $responseCode")
      None
    }
  }

  override def gameToJson: JsObject =
    val file = new FileIOJson
    file.gameToJson(this.field)

  override def toString: String = field.toString
