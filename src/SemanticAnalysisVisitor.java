import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import java.util.List;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.JmmNode;
import java.util.Map;
import java.util.ArrayList;

class SemanticAnalysisVisitor extends PreorderJmmVisitor<ArrayList<Report>, Boolean> {

    private MySymbolTable symbolTable = new MySymbolTable();

    public SemanticAnalysisVisitor(MySymbolTable symbolTable) {

        this.symbolTable = symbolTable;

        addVisit("Assignment", this::handleAssignment);

        setDefaultVisit(this::defaultVisit);

    }

/*     public Boolean scopeIsCorrect(String currScope, JmmNode node) {
        if (node.getChildren().get(0).getKind().equals("Operation")) {
            if (!scopeIsCorrect(currScope, node.getChildren().get(0)))
                return false;
        } else {
            if (!inScope(node.getChildren().get(0), currScope))
            {
                return false;
            }
        }

        if (inScope(node.getChildren().get(1), currScope)) {
            return true;
        }

        return false;

    } */

    //funçao que ve os filhos todos e retorna true ou false dos scopes todo

    public Boolean scopeIsCorrect(String currScope, JmmNode node) {
        for (int i = 0; i < node.getChildren().size(); i++)
        {
            if (node.getChildren().get(i).getKind().equals("RestIdentifier"))
            {
                if (!inScope(node.getChildren().get(i), currScope))
                    return false;
            }
            else
            {
                if (!scopeIsCorrect(currScope, node.getChildren().get(i)))
                    return false;
            }
        }
        return true;
    }

    public Boolean inScope(JmmNode node, String scope) {
        if (node.getKind().equals("RestIdentifier"))
        {
            for (Map.Entry<JmmNode, MySymbol> entry : symbolTable.getTable().entrySet()) {
                MySymbol symbol = entry.getValue();

                if (symbol.getName().equals(node.get("name"))
                        && (symbol.getScope().equals("GLOBAL") || symbol.getScope().equals(scope))) {
                    return true;
                }
            }
            return false;
        }
        else
        {
            return true;
        }   
    }

    public Boolean handleAssignment(JmmNode node, ArrayList<Report> reports) {
        // node.getParent().getKind()
        String scope = new String();
        if (node.getAncestor("MethodDeclaration").isPresent()) {
            scope = node.getAncestor("MethodDeclaration").get().get("name");
        } else if (node.getAncestor("MainDeclaration").isPresent()) {
            scope = "MainDeclaration";
        } else if (node.getAncestor("ClassDeclaration").isPresent()) {
            scope = "GLOBAL";
        }

        // Scope of assignment: left side

        if (!this.inScope(node.getChildren().get(0), scope)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,
                    node.getChildren().get(0).get("name") + " is not in current scope."));
        }

        // Scope of assignment: right side


        if (node.getChildren().get(1).getKind().equals("Operation"))
        {
            if (!scopeIsCorrect(scope, node.getChildren().get(1)))
                System.out.println("Errooooooouuuuuuuu");
        }
        return defaultVisit(node, reports);
    }

    private Boolean defaultVisit(JmmNode node, ArrayList<Report> reports) {
        return true;
    }

}