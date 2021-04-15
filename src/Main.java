import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.MainAnalysis;

import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;
import java.io.*;
import java.nio.file.*;

public class Main implements JmmParser {

	public String readFile(String filePath) {
		try {
			Path path = Path.of(filePath);
			return Files.readString(path);	
		} catch (IOException e) {
			return null;
		}
	}

	public JmmParserResult parse(String jmmCode) {
		Parser parser = new Parser(new StringReader(jmmCode));

		try {
    		SimpleNode root = parser.Program(); // returns reference to root node
            	
    		//root.dump(""); // prints the tree on the screen
			//System.out.println(root.toJson());
			System.out.println(parser.getReports());

    		return new JmmParserResult(root, parser.getReports());
		} catch(ParseException e) {
			Report r = new Report(ReportType.ERROR, Stage.SYNTATIC, 0, e.getMessage());
			parser.getReports().add(r);
			System.out.println(parser.getReports());

			return new JmmParserResult(null, parser.getReports());
		}
	}

	public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
		if (parserResult.getReports().isEmpty() && parserResult.getRootNode() != null) {
			JmmNode node = parserResult.getRootNode().sanitize();
			
			var stGenerator = new SymbolTableGenerator(); // TO DO
			SymbolTable st = stGenerator.visit(node, null);


			/*var semanticAnalysisVisitor = new SemanticAnalysisVisitor(); // TO DO: class SemanticAnalysisVisitor extends PreOrderVisitor<SymbolTable, List<Reports>
			List<Report> reports = semanticAnalysisVisitor.visit(node, st);*/
			
			return new JmmSemanticsResult(node, st, new ArrayList<Report>());
		}
		return null;
	}

    public static void main(String[] args) {
		if (args.length != 1) {
			return;
		}

		Main compiler = new Main();
		String filename = compiler.readFile(args[0]);

		JmmParserResult parserResult = compiler.parse(filename);

		MainAnalysis analysis = new MainAnalysis();

        //analysis.semanticAnalysis(parserResult);

		JmmSemanticsResult semanticsResult = compiler.semanticAnalysis(parserResult); 	// CP2: Symbol table generation and semantic analysis

		//OllirResult ollirResult = compiler.toOllir(semanticsResult); 					// CP2: Convert AST to OLLIR format
		//JasminResult jasminResult = compiler.toJasmin(ollirResult, filename);			// CP2: Convert OLLIR to Jasmin Bytecode (only for code structures defined in the project)
		//compiler.compile(jasminResult, filename);										// CP2: this should generate the .class File
	}
}
