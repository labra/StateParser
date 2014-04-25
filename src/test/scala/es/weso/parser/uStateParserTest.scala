package es.weso.parser

import scala.util.parsing.combinator.RegexParsers
import util.parsing.input.CharSequenceReader.EofCh
import utest._
import utest.ExecutionContext.RunNow

object uStateParserTest extends TestSuite { 

val tests = TestSuite{
  "test2"-{
    val x = 2+2
	val y = 2*2
	assert(x == y)
  }
}

}