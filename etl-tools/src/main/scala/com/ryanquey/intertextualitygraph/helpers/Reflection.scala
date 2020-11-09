package com.ryanquey.intertextualitygraph.helpers

import scala.reflect._
import scala.reflect.runtime.universe._

// https://stackoverflow.com/a/24100624/6952495
object Reflection {
  /*
   * for type U, get all fields with that type for class T
   *
   */ 
  def getFieldsOfTypeForClass[T: TypeTag: ClassTag, U: TypeTag: ClassTag]() = {
    val rm = runtimeMirror(classTag[T].runtimeClass.getClassLoader)
    val classTest = typeOf[T].typeSymbol.asClass
    val classMirror = rm.reflectClass(classTest)
    val constructor = typeOf[T].decl(termNames.CONSTRUCTOR).asMethod

    val listFieldSymbols : List[Symbol] = constructor.paramLists.flatten.filter(_.typeSignature <:< typeOf[U])
    val notListFieldSymbols : List[Symbol] = constructor.paramLists.flatten.filterNot(_.typeSignature <:< typeOf[U])

    println(s"list field symbools: $listFieldSymbols")
    println(s"not list field symbools: $notListFieldSymbols")
    val listFields =  listFieldSymbols.map(_.name.toString)
    println(s"list fields in reflection: $listFields")

    listFields
  }

  def fromMap[T: TypeTag: ClassTag](m: Map[String,_]) = {
    val rm = runtimeMirror(classTag[T].runtimeClass.getClassLoader)
    val classTest = typeOf[T].typeSymbol.asClass
    val classMirror = rm.reflectClass(classTest)
    val constructor = typeOf[T].decl(termNames.CONSTRUCTOR).asMethod
    val constructorMirror = classMirror.reflectConstructor(constructor)

    val constructorArgs = constructor.paramLists.flatten.map( (param: Symbol) => {
      val paramName = param.name.toString
      println(s"paramName: $paramName")
      println(s"param.typeSignature: ${param.typeSignature}")
      if(param.typeSignature <:< typeOf[Option[Any]]) {
        // note that "get" will return Option (Some or None)
        m.get(paramName)
      } else
        // if not an option, throw error if not exists
        m.get(paramName).getOrElse(throw new IllegalArgumentException("Map is missing required parameter named " + paramName))
    })

    // takes all args, and send to case class's constructor
    println(s"constructorArgs: ${constructorArgs}")
    // TEST
    constructorMirror(constructorArgs:_*).asInstanceOf[T]
  }
}

