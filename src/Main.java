import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.io.StringReader;
import java.io.*;
import java.nio.file.*;

public class Main implements JmmParser {


	public JmmParserResult parse(String jmmCode) {
		
		try {
			Parser parser = new Parser(new StringReader(jmmCode));
    		SimpleNode root = parser.Program(); // returns reference to root node
            	
    		root.dump(""); // prints the tree on the screen
			//System.out.println(root.toJson());

    		return new JmmParserResult(root, parser.getReports());
		} catch(ParseException e) {
			throw new RuntimeException("Error while parsing", e);
		}
	}

    public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: java Calculator input.jmm");
			return;
		}

		Main main = new Main();
	
		main.parse(main.readFile(args[0]));
	}

	public String readFile(String filePath) {
		try {
			Path path = Path.of(filePath);
			return Files.readString(path);	
		} catch (IOException e) {
			return null;
		}
	}
}
