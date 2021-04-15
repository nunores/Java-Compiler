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
import pt.up.fe.comp.jmm.analysis.table.Type;

class SymbolTableGenerator extends PreorderJmmVisitor<List<Report>, Boolean> {

    private MySymbolTable symbolTable = new MySymbolTable();

    public MySymbolTable getSymbolTable(){
        return this.symbolTable;
    }

    public SymbolTableGenerator() {

        addVisit("ImportDeclaration", this::handleImportDeclaration);

        setDefaultVisit(this::defaultVisit);

    }

    public Boolean handleImportDeclaration(JmmNode node, List<Report> reports){
        
        MySymbol symbol = new MySymbol(new Type(node.getKind(), false), node.getKind(), null);
        symbolTable.add(node, symbol);        
        
        return defaultVisit(node, reports);
    }

    private Boolean defaultVisit(JmmNode node, List<Report> reports) {
        return true;
    }


}