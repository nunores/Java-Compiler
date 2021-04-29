
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.examples.ExamplePostorderVisitor;
import pt.up.fe.comp.jmm.ast.examples.ExamplePreorderVisitor;
import pt.up.fe.comp.jmm.ast.examples.ExamplePrintVariables;
import pt.up.fe.comp.jmm.ast.examples.ExampleVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

public class AnalysisStage implements JmmAnalysis {

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {

        if (parserResult.getReports().isEmpty() && parserResult.getRootNode() != null) {
			JmmNode node = parserResult.getRootNode().sanitize();
			
			var stGenerator = new SymbolTableGenerator(); // TO DO
			Boolean temp = stGenerator.visit(node);
			MySymbolTable st = stGenerator.getSymbolTable();

			st.print();

			ArrayList<Report> reports = new ArrayList<Report>();
			var semanticAnalysisVisitor = new SemanticAnalysisVisitor(st);
			semanticAnalysisVisitor.visit(node, reports);

			System.out.println(reports);
			
			return new JmmSemanticsResult(node, st, reports);
		}
		return null;

    }

}