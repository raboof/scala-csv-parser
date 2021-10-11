package zamblauskas.csv.parser

import zamblauskas.functional._
import zamblauskas.csv.parser.ReadResult._

case class ColumnBuilder(name: String):

  def as[T](implicit r: Reads[T]) = new ColumnReads[T]:
    override def isHeaderValid(names: Seq[String]): Boolean = names.contains(name)
    override def read(line: Seq[Column]): ReadResult[T] =
      line.find(_.name == name)
        .map(r.read)
        .getOrElse(ReadFailure(s"Column '$name' does not exist."))

  def asOpt[T](implicit r: Reads[T]) = new ColumnReads[Option[T]]:
    override def isHeaderValid(names: Seq[String]): Boolean = true
    override def read(line: Seq[Column]): ReadResult[Option[T]] =
      line.find(column => column.name == name && column.value.nonEmpty)
        .map(r.read(_).map(Some(_)))
        .getOrElse(ReadSuccess(None))


