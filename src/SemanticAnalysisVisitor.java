import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import java.util.List;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.JmmNode;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


class SemanticAnalysisVisitor extends PreorderJmmVisitor<ArrayList<Report>, Boolean> {

    private MySymbolTable symbolTable = new MySymbolTable();

    List<String> booleanArray = Arrays.asList("True", "False");
    List<String> intArray = Arrays.asList("IntegerLiteral", "DotLength");
    List<String> varArray = Arrays.asList("MethodCall", "RestIdentifier");
 
    public SemanticAnalysisVisitor(MySymbolTable symbolTable) {

        this.symbolTable = symbolTable;

        addVisit("Assignment", this::handleAssignment);

        setDefaultVisit(this::defaultVisit);

    }

    // Function that goes through every child and returns true if scope is correct, false otherwise
    public Boolean scopeIsCorrect(String currScope, JmmNode node) {
        if (node.getKind().equals("RestIdentifier"))
        {
            if (!inScope(node, currScope))
            {
                return false;
            }
        }
        for (int i = 0; i < node.getChildren().size(); i++)
        {
            if (node.getChildren().get(i).getKind().equals("RestIdentifier"))
            {
                if (!inScope(node.getChildren().get(i), currScope))
                {
                    return false;
                }
            }
            else
            {
                if (!scopeIsCorrect(currScope, node.getChildren().get(i)))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public Boolean inScope(JmmNode node, String scope) {
        if (node.getKind().equals("RestIdentifier") || node.getKind().equals("Var"))
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

    public String getType(JmmNode node, String scope){
        String tempType = new String();
        for (Map.Entry<JmmNode, MySymbol> entry : symbolTable.getTable().entrySet()) {
            MySymbol symbol = entry.getValue();

            if (symbol.getName().equals(node.get("name")) && (symbol.getScope().equals("GLOBAL") || symbol.getScope().equals(scope))) {
                if (symbol.getScope().equals("GLOBAL"))
                {
                    tempType = symbol.getType().getName();
                }
                else if(symbol.getScope().equals(scope))
                {
                    return symbol.getType().getName();
                }
                
            }
        }
        return tempType;
    }

    public Boolean checkTypes(JmmNode node, String type, String scope)
    {
        // TODO: treat current
        for (int i = 0; i < node.getChildren().size(); i++)
        {
            if (this.intArray.contains(node.getChildren().get(i).getKind()) && !type.equals("int"))
            {
                System.out.println("1");
                return false;
            }
            else if (this.booleanArray.contains(node.getChildren().get(i).getKind()) && !type.equals("boolean"))
            {
                System.out.println("2");
                return false;
            }
            else if (this.varArray.contains(node.getChildren().get(i).getKind()))
            {
                System.out.println("3");
                System.out.println(getType(node.getChildren().get(i), scope));
                System.out.println(node.getChildren().get(i).get("name"));
                if (!type.equals(getType(node.getChildren().get(i), scope)))
                {
                    System.out.println(getType(node.getChildren().get(i), scope)); // TODO: Report
                    return false;
                }
            }
            else
            {
                System.out.println("4");
                if (!checkTypes(node.getChildren().get(i), type, scope))
                    return false;
            }
        }
        return true;
    }
 
    public Boolean handleAssignment(JmmNode node, ArrayList<Report> reports) {
        String scope = new String();
        if (node.getAncestor("MethodDeclaration").isPresent()) {
            scope = node.getAncestor("MethodDeclaration").get().get("name");
        } else if (node.getAncestor("MainDeclaration").isPresent()) {
            scope = "MainDeclaration";
        } else if (node.getAncestor("ClassDeclaration").isPresent()) {
            scope = "GLOBAL";
        }

        if (!this.inScope(node.getChildren().get(0), scope)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,
                    node.getChildren().get(0).get("name") + " is not in current scope."));
        }

        if (!scopeIsCorrect(scope, node.getChildren().get(1))) //TODO: tratar report disto
            System.out.println("Errooooooouuuuuuuu");

        String type = getType(node.getChildren().get(0), scope);

        if (!checkTypes(node.getChildren().get(1), type, scope))
            System.out.println("Weleleleleelelelel");


        return defaultVisit(node, reports);
    }

    private Boolean defaultVisit(JmmNode node, ArrayList<Report> reports) {
        return true;
    }

}