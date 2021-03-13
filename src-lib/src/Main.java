import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.Arrays;
import java.util.ArrayList;
import java.io.StringReader;
import java.io.*;
import java.nio.file.*;

public class Main implements JmmParser {


	public JmmParserResult parse(String jmmCode) {
		
		try {
		    Calculator myCalc = new Calculator(new StringReader(jmmCode));
    		SimpleNode root = myCalc.Program(); // returns reference to root node
            	
    		root.dump(""); // prints the tree on the screen
    	
    		return new JmmParserResult(root, new ArrayList<Report>());
		} catch(ParseException e) {
			throw new RuntimeException("Error while parsing", e);
		}
	}

    public static void main(String[] args) {
        //System.out.println("Executing with args: " + Arrays.toString(args));
        if (args[0].contains("fail")) {
            throw new RuntimeException("It's supposed to fail");
        }

		if (args.length != 1) {
			System.out.println("Usage: java Calculator input.jmm");
			return;
		}

		Main main = new Main();
		
		FileInputStream file;
		try {
			file = new FileInputStream(args[0]);
		} catch (Exception e) {
			System.err.println("File " + args[0] + " not found");
			return;
		}
	
		//System.out.println("If you see this, probably its working!");

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
