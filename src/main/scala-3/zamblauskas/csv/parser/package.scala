package zamblauskas.csv

package object parser {

  def column(name: String): ColumnBuilder = ColumnBuilder(name)

  implicit inline def materializeColumnReads[T]: ColumnReads[T] = ${ReadsMacro.materializeColumnReadsImpl[T]}

}
