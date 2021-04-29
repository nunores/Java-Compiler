import java.util.List;
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

class OptimizationVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {

    private MySymbolTable symbolTable = new MySymbolTable();
    private List<Report> reports = null;

    public MySymbolTable getSymbolTable(){
        return this.symbolTable;
    }

    public OptimizationVisitor(List<Report> reports) {
        this.reports = reports;
    }

    private Boolean defaultVisit(JmmNode node, List<Report> reports) {
        return true;
    }
}