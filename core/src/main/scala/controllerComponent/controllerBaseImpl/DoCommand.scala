package controllerComponent.controllerBaseImpl

import FieldComponent.*
import FieldComponent.FieldBaseImpl.*
import FieldComponent.FieldInterface
import lib.Command

class DoCommand(coordinates: Coordinates) extends Command[FieldInterface] {

  private var fieldUndo: Option[FieldInterface] = None
  private var fieldRedo: Option[FieldInterface] = None

  override def noStep(field: FieldInterface): FieldInterface = field

  override def doStep(field: FieldInterface): FieldInterface =
    fieldUndo = Some(field)
    field.revealValue(coordinates.x, coordinates.y)

  override def doFlag(field: FieldInterface): FieldInterface =
    fieldUndo = Some(field)
    field.setFlag(coordinates.x, coordinates.y)

  override def redoStep(field: FieldInterface): FieldInterface =
    fieldUndo = Some(field)
    fieldRedo.get

  override def undoStep(field: FieldInterface): FieldInterface =
    fieldRedo = Some(field)
    fieldUndo.get
}
