import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import java.util.List;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.JmmNode;
import java.util.ArrayList;


class SemanticAnalysisVisitor extends PreorderJmmVisitor<ArrayList<Report>, Boolean>  {

    private MySymbolTable symbolTable = new MySymbolTable();

    public SemanticAnalysisVisitor(MySymbolTable symbolTable, ArrayList<Report> reports) {

        this.symbolTable = symbolTable;

        addVisit("Operation", this::handleOperation);

        setDefaultVisit(this::defaultVisit);

    }

    public Boolean handleOperation(JmmNode node, ArrayList<Report> reports){
        //node.getParent().getKind()
        
        return defaultVisit(node, reports); 
    }


    private Boolean defaultVisit(JmmNode node, ArrayList<Report> reports) {
        //return reports.isEmpty();
        return true;
    }
    
}