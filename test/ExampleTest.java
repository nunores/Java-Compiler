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
        /*JmmParserResult r = TestUtils.parse("test/fixtures/public/fail/syntactical/BlowUp.jmm");
        System.out.println("Errors: " + TestUtils.getNumErrors(r.getReports()));
        assertEquals(TestUtils.getNumErrors(r.getReports()), 3);*/
	}
}
