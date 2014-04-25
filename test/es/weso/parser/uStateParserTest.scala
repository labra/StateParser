package es.weso.parser

import scala.util.parsing.combinator.RegexParsers
import util.parsing.input.CharSequenceReader.EofCh
import utest._
import utest.ExecutionContext.RunNow

object uStateParserTest extends TestSuite { 
val test = TestSuite{
  "test1"-{
    throw new Exception("test1")
  }
  "test2"-{
    1
  }
  "test3"-{
    val a = List[Byte](1, 2)
    a(10)
  }
}

}