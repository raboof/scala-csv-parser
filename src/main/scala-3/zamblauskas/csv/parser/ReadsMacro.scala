package zamblauskas.csv.parser

import scala.quoted._

object ReadsMacro:
  inline def materializeColumnReads[T]: ColumnReads[T] = ${materializeColumnReadsImpl[T]}

  def materializeColumnReadsImpl[T: Type](using quotes: Quotes): Expr[ColumnReads[T]] =
    import quotes.reflect._
    val typeOfT = TypeRepr.of[T]
    println(s"Materializing columns of type $typeOfT")
    val fullNameOfT = typeOfT.typeSymbol.fullName

    def abort(cause: String): Nothing = {
      val name = classOf[ColumnReads[_]].getName
      val msg = cause + "\n" +
        s"Error while trying to generate $name[$fullNameOfT].\n" +
        s"Either fix the error or provide $name[$fullNameOfT]."
      abort(msg)
    }
    val fields = typeOfT.typeSymbol.primaryConstructor.paramSymss.headOption.getOrElse(
      abort(s"Couldn't find $fullNameOfT constructor.")
    )

    val fieldsColumnBuilders = fields.map { field =>
      val columnName = field.name
      val returnType = field.tree.asInstanceOf[ValDef].tpt.tpe
      println(s" field $columnName")
      println(s" type $returnType is a ${returnType.getClass}")

      val isOption = returnType <:< TypeRepr.of[Option[_]]

      if(isOption) {
        val innerType = returnType.typeArgs.headOption.getOrElse(
          abort("Option must have a concrete type argument.")
        )
        // TODO String -> innerType
        '{ ColumnBuilder(${Expr(columnName)}).asOpt[String] }
      } else {
        // TODO String -> returnType
        '{ ColumnBuilder(${Expr(columnName)}).as[String] }
      }
    }

    val functionalPkg = '{import zamblauskas.functional._}
    val newTypeOfT = New(TypeTree.of[T])
      .select(typeOfT.typeSymbol.primaryConstructor)
      //.appliedToArgs(fieldsColumnBuilders.map(_.asTerm))
      //.asExprOf[T]
    //val newTypeOfT = c.parse(s"new $typeOfT(${fields.map(_ => "_").mkString(",")})")

    val columnBuilderOfT = fieldsColumnBuilders match {
      case Nil =>
        abort(s"$fullNameOfT constructor must have at least one parameter.")
      case x :: Nil =>
        //Apply(
        //  Select.unique(x.asTerm, "map"),
        //  List(Expr(typeOfT.typeSymbol.primaryConstructor).asTerm)
        //)
        //TODO
        //q"""
        //  $functionalPkg
        //  $x.map($newTypeOfT)
        //  """
      case _ =>
        ???
        //val applicativeBuilder = fieldsColumnBuilders.reduceLeft { (acc, r) =>
        //  q"$acc.and($r)"
        //}
        //q"""
        //   $functionalPkg
        //   $applicativeBuilder.apply($newTypeOfT)
        // """
    }
    ???
