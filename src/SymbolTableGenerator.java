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

class SymbolTableGenerator extends PreorderJmmVisitor<List<Report>, Boolean> {

    private MySymbolTable symbolTable = new MySymbolTable();

    public MySymbolTable getSymbolTable(){
        return this.symbolTable;
    }

    public SymbolTableGenerator() {

        addVisit("ImportDeclaration", this::handleImportDeclaration);
        addVisit("ClassDeclaration", this::handleClassDeclaration);
        addVisit("VarDeclaration", this::handleVarDeclaration);
        addVisit("MethodDeclaration", this::handleMethodDeclaration);
        addVisit("MainDeclaration", this::handleMainDeclaration);
        addVisit("Parameter", this::handleParameter);
        addVisit("Extends", this::handleExtends);

        setDefaultVisit(this::defaultVisit);

    }

    public Boolean handleImportDeclaration(JmmNode node, List<Report> reports){
        
        MySymbol symbol = new MySymbol(new Type(node.getKind(), false), node.getKind(), node.get("qualifiedName"), "GLOBAL");
        symbolTable.add(node, symbol);        
        
        return defaultVisit(node, reports);
    }

    public Boolean handleClassDeclaration(JmmNode node, List<Report> reports){
        MySymbol symbol = new MySymbol(new Type(node.getKind(), false), node.getKind(), node.getChildren().get(0).get("name"), "GLOBAL");
        symbolTable.add(node, symbol);        
        
        return defaultVisit(node, reports);
    }

    public Boolean handleVarDeclaration(JmmNode node, List<Report> reports){
        if (node.getParent().getKind().equals("ClassDeclaration"))
        {
            MySymbol symbol = new MySymbol(new Type(node.getChildren().get(0).get("name"), false), node.getKind(), node.get("name"), "GLOBAL");
            symbolTable.add(node, symbol);
        }
        else if (node.getParent().getKind().equals("MainDeclaration"))
        {
            MySymbol symbol = new MySymbol(new Type(node.getChildren().get(0).get("name"), false), node.getKind(), node.get("name"), node.getParent().getKind());
            symbolTable.add(node, symbol);   
        }
        else
        {
            MySymbol symbol = new MySymbol(new Type(node.getChildren().get(0).get("name"), false), node.getKind(), node.get("name"), node.getParent().get("name"));
            symbolTable.add(node, symbol);        
        }        
        return defaultVisit(node, reports);
    }

    public Boolean handleMethodDeclaration(JmmNode node, List<Report> reports){
        MySymbol symbol = new MySymbol(new Type(node.getChildren().get(0).get("name"), false), node.getKind(), node.get("name"), "GLOBAL");
        symbolTable.add(node, symbol);      
        
        return defaultVisit(node, reports);
    }

    public Boolean handleMainDeclaration(JmmNode node, List<Report> reports){
        MySymbol symbol = new MySymbol(new Type(node.getKind(), false), node.getKind(), node.getKind(), "GLOBAL");
        symbolTable.add(node, symbol);        
        
        return defaultVisit(node, reports);
    }
    
    public Boolean handleParameter(JmmNode node, List<Report> reports){
        MySymbol symbol = new MySymbol(new Type(node.getChildren().get(0).get("name"), false), node.get("name"), node.get("name"), node.getParent().get("name"));

        this.symbolTable.getTable().get(node.getParent()).addAttribute("Parameter", symbol);  
        symbolTable.add(node, symbol); 
        
        return defaultVisit(node, reports);
    }

    public Boolean handleExtends(JmmNode node, List<Report> reports){
        MySymbol symbol = new MySymbol(new Type(node.getKind(), false), node.get("extendedClass"), node.getKind(), node.getParent().getKind());
        
        this.symbolTable.getTable().get(node.getParent()).addAttribute("Extends", symbol);

        return defaultVisit(node, reports);

    }

    private Boolean defaultVisit(JmmNode node, List<Report> reports) {
        return true;
    }


}