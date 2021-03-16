import static org.junit.Assert.*;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;
import java.io.StringReader;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;

public class ExampleTest {

    @Test
    public void testExpression() {
    assertTrue(true);		
	}

	@Test
    public void testing() {
      Main main = new Main();
      JmmParserResult r = main.parse(main.readFile("inputs/input.jmm"));
      System.out.println(r.getRootNode().toJson());
    }
}
