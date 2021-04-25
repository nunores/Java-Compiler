import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import java.util.List;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.JmmNode;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;


class SemanticAnalysisVisitor extends PreorderJmmVisitor<ArrayList<Report>, Boolean> {

    private MySymbolTable symbolTable = new MySymbolTable();

    List<String> booleanArray = Arrays.asList("True", "False", "Less", "And", "Or", "Not");
    List<String> intArray = Arrays.asList("IntegerLiteral", "DotLength");
    List<String> varArray = Arrays.asList("MethodCall", "RestIdentifier");
    List<String> newInstanceArray = Arrays.asList("NewInstance");
    List<String> typeReturner = Arrays.asList("True", "False", "Less", "And", "Or", "Not", "IntegerLiteral", "DotLength", "MethodCall", "RestIdentifier", "NewInstance");
 
    public SemanticAnalysisVisitor(MySymbolTable symbolTable) {

        this.symbolTable = symbolTable;

        addVisit("Assignment", this::handleAssignment);
        addVisit("Less", this::handleLess);
        addVisit("And", this::handleAnd);
        addVisit("Not", this::handleNot);
        addVisit("ifStatement", this::handleIfStatementWhileStatement);
        addVisit("whileStatement", this::handleIfStatementWhileStatement);

        setDefaultVisit(this::defaultVisit);

    }

    public String getTypeReturnedByNode(JmmNode node, String scope)
    {
        
        String currentType = "";
        if (typeReturner.contains(node.getKind()))
        {
            if (booleanArray.contains(node.getKind()))
            {
                return "boolean";
            }
            else if (intArray.contains(node.getKind()))
            {
                return "int";
            }
            else if (varArray.contains(node.getKind()))
            {
                return getType(node, scope);
            }
            else if (newInstanceArray.contains(node.getKind()))
            {
                return node.get("name");
            }
        }
        for (int i = 0; i < node.getChildren().size(); i++)
        {
            JmmNode current = node.getChildren().get(i);
            String typeReturned = getTypeReturnedByNode(current, scope);
            if (currentType.equals(""))
            {
                currentType = typeReturned;
            }
            else if (!typeReturned.equals(currentType))
            {
                return "NULL";
            }
        }
        return currentType;
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

    // Checks if the scope in an assignment is the current or global
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

    // Returns the type of a variable/method
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
        if (this.intArray.contains(node.getKind()) || this.booleanArray.contains(node.getKind()) || this.varArray.contains(node.getKind()) || this.newInstanceArray.contains(node.getKind()))
        {
            if (this.intArray.contains(node.getKind()))
            {
                if (!type.equals("int"))
                {
                    System.out.println("1");
                    return false;
                }
            }
            else if (this.booleanArray.contains(node.getKind()))
            {
                if(!type.equals("boolean")){                    
                    System.out.println("2");
                    return false;
                }
            }
            else if(this.newInstanceArray.contains(node.getKind())){
                if(!type.equals(node.get("name")))
                {
                    System.out.println("3");
                    return false;
                }
            }
            else if (this.varArray.contains(node.getKind()))
            {
                if (!type.equals(getType(node, scope)))
                {
                    System.out.println(getType(node, scope)); // TODO: Report
                    return false;
                }
            }
            return true;
        }
        for (int i = 0; i < node.getChildren().size(); i++)
        {
            System.out.println(node.getChildren().get(i).getKind());
            if (this.intArray.contains(node.getChildren().get(i).getKind()))
            {
                if(!type.equals("int")){                  
                    System.out.println("1");
                    return false;
                }
            }
            else if (this.booleanArray.contains(node.getChildren().get(i).getKind()))
            {
                if (!type.equals("boolean"))
                {
                    System.out.println("2");
                    return false;
                }
            }
            else if (this.newInstanceArray.contains(node.getChildren().get(i).getKind()))
            {
                if (!type.equals(node.get("name")))
                {
                    System.out.println("3");
                    return false;
                }
            }
            else if (this.varArray.contains(node.getChildren().get(i).getKind()))
            {
                if (!type.equals(getType(node.getChildren().get(i), scope)))
                {
                    System.out.println(node.get("name"));
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

    public Boolean handleIfStatementWhileStatement(JmmNode node, ArrayList<Report> reports) {
        String scope = getScope(node);
        if (!scopeIsCorrect(scope, node.getChildren().get(0)))
        {
            System.out.println("Bad scope");
        }

        if (!getTypeReturnedByNode(node.getChildren().get(0), scope).equals("boolean"))
        {
            System.out.println("IfStatement / WhileStatement nao tem um boolean na condiçao"); // TODO: Report
        }
        
        return defaultVisit(node, reports);
    }

    public Boolean handleNot(JmmNode node, ArrayList<Report> reports) {
        String scope = getScope(node);
        if (!scopeIsCorrect(scope, node.getChildren().get(0)))
        {
            System.out.println("Bad scope");
        }

        if (!getTypeReturnedByNode(node.getChildren().get(0), scope).equals("boolean"))
        {
            System.out.println("Not nao tem um boolean"); // TODO: Report
        }
    
        return defaultVisit(node, reports);
    }

    public Boolean handleAnd(JmmNode node, ArrayList<Report> reports) {
        String scope = getScope(node);
        if (!scopeIsCorrect(scope, node.getChildren().get(0)))
        {
            System.out.println("Bad scope");
        }
        if (!scopeIsCorrect(scope, node.getChildren().get(1)))
        {
            System.out.println("Bad scope");
        }

        if (!getTypeReturnedByNode(node.getChildren().get(0), scope).equals("boolean") || !getTypeReturnedByNode(node.getChildren().get(1), scope).equals("boolean"))
        {
            System.out.println("And nao tem boolean num dos dois lados"); // TODO: Report
        }

        return defaultVisit(node, reports);
    }

    public Boolean handleLess(JmmNode node, ArrayList<Report> reports) {
        String scope = getScope(node);
        if (!scopeIsCorrect(scope, node.getChildren().get(0)))
        {
            System.out.println("Bad scope");
        }
        if (!scopeIsCorrect(scope, node.getChildren().get(1)))
        {
            System.out.println("Bad scope");
        }

        if (!getTypeReturnedByNode(node.getChildren().get(0), scope).equals("int") || !getTypeReturnedByNode(node.getChildren().get(1), scope).equals("int"))
        {
            System.out.println("Less nao tem int num dos dois lados"); // TODO: Report
            
        }

        return defaultVisit(node, reports);
        

    }

    public String getScope(JmmNode node){
        String scope = new String();
        if (node.getAncestor("MethodDeclaration").isPresent()) {
            scope = node.getAncestor("MethodDeclaration").get().get("name");
        } else if (node.getAncestor("MainDeclaration").isPresent()) {
            scope = "MainDeclaration";
        } else if (node.getAncestor("ClassDeclaration").isPresent()) {
            scope = "GLOBAL";
        }
        return scope;
    }


    public Boolean handleAssignment(JmmNode node, ArrayList<Report> reports) {
        String scope = getScope(node);

        if (!this.inScope(node.getChildren().get(0), scope)) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, 0,
                    node.getChildren().get(0).get("name") + " is not in current scope."));
        }

        if (!scopeIsCorrect(scope, node.getChildren().get(1))) //TODO: tratar report disto
            System.out.println("Errooooooouuuuuuuu");

        String type = getType(node.getChildren().get(0), scope);

        if (!checkTypes(node.getChildren().get(1), type, scope)) //TODO: tratar report disto
            System.out.println("Weleleleleelelelel");

        return defaultVisit(node, reports);
    }

    private Boolean defaultVisit(JmmNode node, ArrayList<Report> reports) {
        return true;
    }

}