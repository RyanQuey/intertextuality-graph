package com.ryanquey.intertextualitygraph.helpers.shapeless

import shapeless.{LabelledGeneric, Generic}
import shapeless.record.Record
import shapeless.syntax.std.maps._
import shapeless._
import labelled.{ FieldType, field }


/*
 * for converting maps to case classes
 * - I don't understand half the code, but it seems to work
 * - https://stackoverflow.com/a/55065901/6952495
 * - use like: 
 *   ```
 *     def valueMapToCaseClass(valueMap: java.util.Map[String, Any]) : TextVertex = {
 *        val typecastedMap = valueMap.asScala.toMap
 *        CaseClassFromMap[TextVertex](typecastedMap)
 *     }
 * 
 *   ```
 *
 * - if get error: 
 *   java.util.NoSuchElementException: None.get
 *
 *   It probably means that there is a field in the case class that has not been marked Option and was not passed in
 */
trait FromMap[L <: HList] {
  def apply(m: Map[String, Any]): Option[L]
}

trait LowPriorityFromMap {
  implicit def hconsFromMap1[K <: Symbol, V, T <: HList](implicit
    witness: Witness.Aux[K],
    typeable: Typeable[V],
    fromMapT: Lazy[FromMap[T]]
  ): FromMap[FieldType[K, V] :: T] = new FromMap[FieldType[K, V] :: T] {
    def apply(m: Map[String, Any]): Option[FieldType[K, V] :: T] = for {
      v <- m.get(witness.value.name)
      h <- typeable.cast(v)
      t <- fromMapT.value(m)
    } yield field[K](h) :: t
  }
}

object FromMap extends LowPriorityFromMap {
  implicit val hnilFromMap: FromMap[HNil] = new FromMap[HNil] {
    def apply(m: Map[String, Any]): Option[HNil] = Some(HNil)
  }

  implicit def hconsFromMap0[K <: Symbol, V, R <: HList, T <: HList](implicit
    witness: Witness.Aux[K],
    gen: LabelledGeneric.Aux[V, R],
    fromMapH: FromMap[R],
    fromMapT: FromMap[T]
    ): FromMap[FieldType[K, V] :: T] =
      new FromMap[FieldType[K, V] :: T] {
        def apply(m: Map[String, Any]): Option[FieldType[K, V] :: T] = for {
          v <- m.get(witness.value.name)
          r <- Typeable[Map[String, Any]].cast(v)
          h <- fromMapH(r)
          t <- fromMapT(m)
        } yield field[K](gen.from(h)) :: t
      }

  implicit def hconsFromMap0opt[K <: Symbol, V, R <: HList, T <: HList](implicit
    witness: Witness.Aux[K],
    gen: LabelledGeneric.Aux[V, R],
    fromMapH: FromMap[R],
    fromMapT: FromMap[T]
  ): FromMap[FieldType[K, Option[V]] :: T] =
    new FromMap[FieldType[K, Option[V]] :: T] {
      def apply (m: Map[String, Any]) : Option[FieldType[K, Option[V]] :: T] = (
        for {
          v <- m.get(witness.value.name)
          r <- Typeable[Map[String, Any]].cast(v)
          h <- fromMapH(r)
          t <- fromMapT(m)
        } yield field[K](Some(gen.from(h))) :: t
      ).orElse(
        for {
          v <- m.get(witness.value.name)
          r1 <- Typeable[Option[Map[String, Any]]].cast(v)
          opt = for {
            r <- r1
            h <- fromMapH(r)
          } yield gen.from(h)
          t <- fromMapT(m)
        } yield field[K](opt) :: t
      )
    }
}


trait CaseClassFromMap[P <: Product] {
  def apply(m: Map[String, Any]): Option[P]
}

/*
 * 
 * - I think the Product corresponds to case class, since all case classes are subclasses of Product (or implement them or something)
 */ 
object CaseClassFromMap {
  implicit def mk[P <: Product, R <: HList] (
    implicit 
      gen: LabelledGeneric.Aux[P, R],
      fromMap: FromMap[R]
  ): CaseClassFromMap[P] = new CaseClassFromMap[P] {
    def apply(m: Map[String, Any]): Option[P] = fromMap(m).map(gen.from)
  }

  def apply[P <: Product](mapWithFields: Map[String, Any])(implicit fromMap: CaseClassFromMap[P]): P = {
    println(s"making case class from map ${mapWithFields}");
    // I think this instantiates FromMap object, using one of the apply methods
    fromMap(mapWithFields).get
  }

}

