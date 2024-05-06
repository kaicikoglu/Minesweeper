package controllerComponent.controllerBaseImpl

import FieldComponent.FieldBaseImpl.*
import FieldComponent.*
import FileIOComponent.FileIOInterface
import FileIOComponent.fileIoJsonImpl.FileIOJson
import com.google.inject.{Guice, Inject, Injector}
import controllerComponent.ControllerInterface
import controllerComponent.controllerBaseImpl.*
import lib.{Event, Observable, UndoManager}
import play.api.libs.json.{JsObject, JsValue, Json}

import java.net.{HttpURLConnection, URI, URLEncoder}
import scala.io.Source

case class Controller @Inject() (var field: FieldInterface) extends ControllerInterface with Observable:
  val file: Injector = Guice.createInjector(new MinesweeperJson)
  private val undoManager = new UndoManager[FieldInterface]
  private val fileIO = file.getInstance(classOf[FileIOInterface])

  def createNewField(string: String): FieldInterface =
    string match {
      case "1" | "2" | "3" =>
        field = createNewFieldApi(string)
        field
    }

  private def createNewFieldApi(difficulty: String): FieldInterface = {
    val encodedDifficulty = URLEncoder.encode(difficulty, "UTF-8")
    val uri = new URI(s"http://localhost:8080/field/createNew?x=$encodedDifficulty")
    val url = uri.toURL

    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)

    val inputStream = connection.getInputStream
    val result = Source.fromInputStream(inputStream).mkString
    inputStream.close()

    field = field.jsonToField(result)
    field
  }

  def calculateBombAmount(): Int =
    calculateBombAmountApi match {
      case Some(bombAmount) =>
        bombAmount
      case None => 0
    }

  private def calculateBombAmountApi: Option[Int] = {
    val uri = new URI("http://localhost:8080/field/calculateBombs")
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
    val uri = new URI("http://localhost:8080/field/setBombs")
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

  def doAndPublish(doThis: Coordinates => FieldInterface, coordinates: Coordinates): Unit =
    field = doThis(coordinates)
    notifyObservers(Event.Move)

  def doAndPublish(doThis: => FieldInterface): Unit =
    field = doThis
    notifyObservers(Event.Move)

  def quit(): Unit = notifyObservers(Event.Quit)

  def revealValue(move: Coordinates): FieldInterface =
    if field.getCell(move.x, move.y)._1 != Stone.NotTracked then undoManager.noStep(field, DoCommand(move))
    else undoManager.doStep(field, DoCommand(move))

  def undo: FieldInterface =
    undoManager.undoStep(field)

  def redo: FieldInterface =
    undoManager.redoStep(field)

  def noStep(move: Coordinates): FieldInterface =
    undoManager.noStep(field, DoCommand(move))

  def setFlag(coordinates: Coordinates): FieldInterface =
    undoManager.doFlag(field, DoCommand(coordinates))

  def save: FieldInterface =
    saveApi()
    field

  private def saveApi(): Unit =
    val jsonPayload = Json.obj("field" -> field.toJson).toString
    val uri = new URI("http://localhost:8081/fileIo/save")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")

    val outputStream = connection.getOutputStream
    outputStream.write(jsonPayload.getBytes("UTF-8"))
    outputStream.close()

    val responseCode = connection.getResponseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
      println("Field saved successfully")
    } else {
      println(s"Failed to save field to server: HTTP response code $responseCode")
    }

  def load: FieldInterface =
    loadApi match {
      case Some(loadedField) => loadedField
      case None => field
    }

  private def loadApi: Option[FieldInterface] = {
    val uri = new URI("http://localhost:8081/fileIo/load")
    val url = uri.toURL
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")

    val responseCode = connection.getResponseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
      val inputStream = connection.getInputStream
      val response = Source.fromInputStream(inputStream).getLines().mkString
      inputStream.close()
      field = field.jsonToField(response)
      Some(field)
    } else {
      println(s"Failed to load field from server: HTTP response code $responseCode")
      None
    }
  }

  def flagsLeft(): Int =
    flagsLeftApi match {
      case Some(flagsLeft) => flagsLeft
      case None => 0
    }

  private def flagsLeftApi: Option[Int] = {
    val uri = new URI(s"http://localhost:8080/field/flagsLeft")
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
    val uri = new URI(s"http://localhost:8080/field/getCell?x=$x&y=$y")
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
