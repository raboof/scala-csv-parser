package zamblauskas.csv

package object parser:

  def column(name: String) = ColumnBuilder(name)

