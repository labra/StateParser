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
   * <p> TODO: It could be defined as a State transformer monad
   */
  type StateParser[S, A] = S => Parser[(A, S)]
  
  def seqRepState[S, A, B](
    first: StateParser[S, A],
    rep: StateParser[S, B]): StateParser[S, (A, List[B])] = { s =>
    for {
      (x, s1) <- first(s)
      (ys, s2) <- repS(rep)(s1)
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
   * threading the state along the repetitions 
   * @tparam T type of values
   * @tparam S type of state
   * @param p parser to repeat
   * @return parser of a list of values
   */
  def repS[T, S](p: StateParser[S, T]): StateParser[S, List[T]] = { s =>
    rep1State(s, p) | success((List(), s))
  }


  /**
   * repeat a parser followed by an optional separator parser whose result is ignored several times
   * @tparam T type returned by parser
   * @tparam S type of state
   * @param p parser
   *  
   * 
   */
  def rep1sepOptState[T, S](p: StateParser[S, T],
                            q: => Parser[Any]): StateParser[S,List[T]] = { s =>
    p(s) >> { s1 =>
      repState(s1._2, arrowOptState(p, q)) ^^ {
        case (ls, s2) => (s1._1 :: ls.flatten, s2)
      }
    }
  }

  /**
   *   
   */
  def arrowOptState[T, S](
      p: StateParser[S, T],
      q: Parser[Any]): StateParser[S, Option[T]] = { s =>
    q ~> opt(p(s)) ^^ {
        case None          => (None, s)
        case Some((t, s1)) => (Some(t), s1)
    }
  }

  def rep1sepState[T, S](p: StateParser[S, T],
                         q: => Parser[Any]): StateParser[S, List[T]] = { s =>
    p(s) >> { s1 =>
      repState(s1._2, arrowState(p, q)) ^^ {
        case (ls, s2) => (s1._1 :: ls, s2)
      }
    }
  }

  /*
   * @deprecated
   */
  /* def rep1sepState[T, S](s: S,
                         p: StateParser[S, T],
                         q: => Parser[Any]): Parser[(List[T], S)] =
    rep1sepState(p, q)(s) */

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
    for {
      (x,s1) <- p(s)
      (y,s2) <- q(s1)
    } yield (new ~(x,y),s2)
  }
  
  def seq3State[A,B,C,S](
      p1: StateParser[S,A],
      p2: StateParser[S,B],
      p3: StateParser[S,C]): StateParser[S, (A, B, C)] = { s => 
    for {
      (x,s1) <- p1(s)
      (y,s2) <- p2(s1)
      (z,s3) <- p3(s2)
    } yield ((x,y,z), s3)
  }
    
  def seq4State[A,B,C,D,S](
      p1: StateParser[S,A],
      p2: StateParser[S,B],
      p3: StateParser[S,C],
      p4: StateParser[S,D]): StateParser[S, (A, B, C, D)] = { s =>
    for {
      (x1,s1) <- p1(s)
      (x2,s2) <- p2(s1)
      (x3,s3) <- p3(s2)
      (x4,s4) <- p4(s3)
    } yield ((x1,x2,x3,x4), s4)
  }
  
  def seq5State[A,B,C,D,E,S](
      p1: StateParser[S,A],
      p2: StateParser[S,B],
      p3: StateParser[S,C],
      p4: StateParser[S,D],
      p5: StateParser[S,E]): StateParser[S, (A, B, C, D, E)] = { s =>
    for {
      (x1,s1) <- p1(s)
      (x2,s2) <- p2(s1)
      (x3,s3) <- p3(s2)
      (x4,s4) <- p4(s3)
      (x5,s5) <- p5(s4)
    } yield ((x1,x2,x3,x4,x5), s5)
  }

  def seq6State[A,B,C,D,E,F,S](
      p1: StateParser[S,A],
      p2: StateParser[S,B],
      p3: StateParser[S,C],
      p4: StateParser[S,D],
      p5: StateParser[S,E],
      p6: StateParser[S,F]): StateParser[S, (A, B, C, D, E, F)] = { s =>
    for {
      (x1,s1) <- p1(s)
      (x2,s2) <- p2(s1)
      (x3,s3) <- p3(s2)
      (x4,s4) <- p4(s3)
      (x5,s5) <- p5(s4)
      (x6,s6) <- p6(s5)
    } yield ((x1,x2,x3,x4,x5,x6), s6)
  }
   
  /**
   * Parses p and then any (ignoring parsed value of any)
   * @deprecated: Should be arrowRight
   */
  def arrowState[T, S](
    p: StateParser[S, T],
    any: Parser[Any]): StateParser[S, T] = arrowRightState(p,any)

  /**
   * Parses any and then p (ignoring parsed value of any)
   */
  def arrowRightState[T, S](
    p: StateParser[S, T],
    any: Parser[Any]): StateParser[S, T] = { s =>
    {
      any ~> p(s)
    }
  }
  
 /**
   * Parses p and then any (ignoring parsed value of any)
   */
  def arrowLeftState[T, S](
    p: StateParser[S, T],
    any: Parser[Any]): StateParser[S, T] = { s =>
    {
      p(s) <~ any
    }
  }

  /**
   * Repeat a parser and collect the results
   */
  def rep1State[T, S](
    p: StateParser[S, T]): StateParser[S, List[T]] = { s =>
    {
      rep1State(s, p, p)
    }
  }

  /**
   * @deprecated
   */
  def rep1State[T, S](s: S, p: StateParser[S, T]): Parser[(List[T], S)] =
    rep1State(s, p, p)

  def rep1State[T, S](s: S,
                      first: StateParser[S, T],
                      p0: StateParser[S, T]): Parser[(List[T], S)] =
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