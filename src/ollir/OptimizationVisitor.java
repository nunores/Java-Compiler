import java.util.List;
import java.util.ArrayList;

import java.util.stream.Collectors;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.specs.util.utilities.StringLines;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.analysis.table.Symbol;


class OptimizationVisitor extends PreorderJmmVisitor<List<Report>, String> {

    private MySymbolTable symbolTable = new MySymbolTable();
    private List<Report> reports = new ArrayList<Report>();
    private String ollirCode = new String();

    public OptimizationVisitor(List<Report> reports, MySymbolTable symbolTable) {
        this.reports = reports;
        this.symbolTable = symbolTable;

        addVisit("Program", this::handleProgram);
        addVisit("ClassDeclaration", this::handleClassDeclaration);
        addVisit("MethodDeclaration", this::handleMethodDeclaration);
        addVisit("MainDeclaration", this::handleMainDeclaration);
        /*addVisit("Assignment", this::handleAssignment);
        addVisit("Less", this::handleLess);
        addVisit("And", this::handleAnd);
        addVisit("Not", this::handleNot);
        addVisit("ifStatement", this::handleIfStatementWhileStatement);
        addVisit("whileStatement", this::handleIfStatementWhileStatement);
        addVisit("ArrayAccess", this::handleArrayAccess);
        addVisit("ReturnExpression", this::handleReturnExpression);
        addVisit("MethodCall", this::handleMethodCall);*/

        setDefaultVisit(this::defaultVisit);
    }

    public String handleProgram(JmmNode node, List<Report> reports) {
        
        return "PROGRAM\n" + defaultVisit(node, reports);
    }

    public String handleClassDeclaration(JmmNode node, List<Report> reports) {
        /*String ret = node.getKind() + "{\n";
        ret += "\t.contruct " + node.getKind() + ".V {\n";
        ret += "\t\tinvokespecial(this, \"<init>\").V;\n";
        ret += "\t}\n";
        return ret + defaultVisit(node, reports) + "}";*/
        return "ClassDeclaration\n" + defaultVisit(node, reports);
    }

    public String handleMethodDeclaration(JmmNode node, List<Report> reports) {
        return "MethodDeclaration\n" + defaultVisit(node, reports);
    }

    public String handleMainDeclaration(JmmNode node, List<Report> reports) {
        
        return "MainDeclaration\n" + defaultVisit(node, reports);
    }

    private String defaultVisit(JmmNode node, List<Report> reports) {
        return ollirCode;
    }
}