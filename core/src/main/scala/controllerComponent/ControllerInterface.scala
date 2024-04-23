package controllerComponent

import FieldComponent.*
import FieldComponent.FieldBaseImpl.{Coordinates, Stone}
import lib.Observable
import play.api.libs.json.JsObject

trait ControllerInterface extends Observable:
  def doAndPublish(doThis: Coordinates => FieldInterface, coordinates: Coordinates): Unit
  def doAndPublish(doThis: => FieldInterface): Unit
  def quit(): Unit
  def calculateBombAmount(): Int
  def revealValue(move: Coordinates): FieldInterface
  def undo: FieldInterface
  def redo: FieldInterface
  def noStep(move: Coordinates): FieldInterface
  def setBombs(bombAmount: Int): FieldInterface
  def setFlag(coordinates: Coordinates): FieldInterface
  def field: FieldInterface
  def save: FieldInterface
  def load: FieldInterface
  def createNewField(string: String): FieldInterface
  def flagsLeft(): Int
  def gameToJson: JsObject
  def getCell(x: Int, y: Int): (Stone, Stone, Int)
