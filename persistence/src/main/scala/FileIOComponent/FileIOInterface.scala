package FileIOComponent

import FieldComponent.*

trait FileIOInterface:
  def load: FieldInterface
  def save(grid: FieldInterface): Unit
