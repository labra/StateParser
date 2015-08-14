package es.weso.parser
import scala.util.parsing.combinator._
import scala.collection.mutable.ListBuffer
import scala.annotation.tailrec

/**
 * StateParser contains an extension of Scala Parser combinators
 * that can pass a state along the parsers
 */
trait StateParser extends Parsers {

  /**
   * A State Parser is a Parser that modifies a state
   * It could (and should) be defined as a StateT
   */
  type StateParser[S, A] = S => Parser[(A, S)]

  def seqRepState[S, A, B](
    first: StateParser[S, A],
    rep: StateParser[S, B]): StateParser[S, (A,List[B])] = { s =>
    for {
      (x, s1) <- first(s)
      (ys, s2) <- repState(s1, rep)
    } yield ((x, ys), s2)
  }

  /**
   * Should be private
   * @deprecated
   */
  def repState[T, S](s: S,
                     p: StateParser[S, T]): Parser[(List[T], S)] =
    repS(p)(s)

  /**
   * repS takes a parser a repeats it until it fails
   * threading the state along the repetitions *
   */
  def repS[T, S](p: StateParser[S, T]): StateParser[S, List[T]] = { s =>
    rep1State(s, p) | success((List(), s))
  }

  def rep1sepOptState[T, S](s: S,
                            p: StateParser[S, T],
                            q: => Parser[Any]): Parser[(List[T], S)] =
    p(s) >> { s1 =>
      repState(s1._2, arrowOptState(p, q)) ^^ {
        case (ls, s2) => (s1._1 :: ls.flatten, s2)
      }
    }

  def arrowOptState[T, S](
    p: StateParser[S, T],
    q: Parser[Any]): StateParser[S, Option[T]] = { s =>
    {
      q ~> opt(p(s)) ^^ {
        case None          => (None, s)
        case Some((t, s1)) => (Some(t), s1)
      }
    }
  }

  def rep1sepState[T, S](s: S,
                         p: StateParser[S, T],
                         q: => Parser[Any]): Parser[(List[T], S)] =
    p(s) >> { s1 =>
      repState(s1._2, arrowState(p, q)) ^^ {
        case (ls, s2) => (s1._1 :: ls, s2)
      }
    }

  def chainl1State[T, S](p: StateParser[S, T],
                         q: StateParser[S, (T, T) => T]): StateParser[S, T] = { s =>
    {
      chainl1State(p, p, q)(s)
    }
  }

  def chainl1State[T, U, S](
    first: StateParser[S, T],
    p: StateParser[S, U],
    q: StateParser[S, ((T, U) => T)]): StateParser[S, T] = { s =>
    {
      seqState(first, repS(seqState(q, p)))(s) ^^
        {
          // x's type annotation is needed to deal with changed type inference due to SI-5189
          case ((x ~ xs, s1)) => (xs.foldLeft(x: T) { case (a, f ~ b) => f(a, b) }, s1)
        }
    }
  }

  def optState[T, S](
    p: StateParser[S, T]): StateParser[S, Option[T]] = { s =>
    {
      (p(s) ^^ (x => (Some(x._1), x._2))
        | success((None, s)))
    }
  }

  def seqState[T, U, S](
    p: StateParser[S, T],
    q: StateParser[S, U]): StateParser[S, T ~ U] = { s =>
    {
      p(s) >> { s1 => q(s1._2) ^^ { case (u, s2) => (new ~(s1._1, u), s2) } }
    }
  }

  def arrowState[T, S](
    p: StateParser[S, T],
    q: Parser[Any]): StateParser[S, T] = { s =>
    {
      q ~> p(s)
    }
  }

  /**
   * Repeat a parser and collect the results
   */
  def rep1State[T, S](
      p: StateParser[S,T]): StateParser[S,List[T]] = { s => {
    rep1State(s, p, p)
  }
  }
    

  /**
   * @deprecated 
   */
  def rep1State[T, S](s: S, p: StateParser[S,T]): Parser[(List[T], S)] =
    rep1State(s, p, p)

  def rep1State[T, S](s: S,
                      first: StateParser[S,T],
                      p0: StateParser[S,T]): Parser[(List[T], S)] =
    Parser { in =>
      lazy val p = p0 // lazy argument
      val elems: ListBuffer[T] = new ListBuffer[T]

      def continue(s: S)(in: Input): ParseResult[(List[T], S)] = {
        val p0 = p // avoid repeatedly re-evaluating by-name parser

        @tailrec
        def applyp(s0: S)(in0: Input): ParseResult[(List[T], S)] = p0(s0)(in0) match {
          case Success(x, rest) =>
            elems += x._1; applyp(x._2)(rest)
          case e @ Error(_, _) => e // still have to propagate error
          case _               => Success((elems.toList, s0), in0)
        }

        applyp(s)(in)
      }

      first(s)(in) match {
        case Success(x, rest) =>
          elems += x._1; continue(x._2)(rest)
        case ns: NoSuccess => ns
      }
    }

}