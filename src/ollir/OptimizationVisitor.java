import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import pt.up.fe.comp.jmm.analysis.table.Symbol;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;


class OptimizationVisitor extends AJmmVisitor<String, String> {

    private MySymbolTable symbolTable;
    private Map<String, String> types = new HashMap<>();
    private String className = "";
    private String actualMethodName = "";
    private String actualMethodType = "";
    private List<String> parameters = new ArrayList<String>();
    private Stack stack = new Stack();
    private List<String> imports = new ArrayList<String>();
    private String assignmentOllir = new String();
    private Integer auxNumber = 1;
    List<String> booleanArray = Arrays.asList("True", "False", "Less", "And", "Or", "Not");
    List<String> intArray = Arrays.asList("IntegerLiteral", "DotLength", "ArrayAccess");
    List<String> varArray = Arrays.asList("MethodCall", "RestIdentifier");
    List<String> newInstanceArray = Arrays.asList("NewInstance");
    List<String> typeReturner = Arrays.asList("True", "False", "Less", "And", "Or", "Not", "IntegerLiteral", "DotLength", "ArrayAccess", "MethodCall", "RestIdentifier", "NewInstance");
    List<String> nodeTypes = Arrays.asList("int", "boolean", "int[]");

    public OptimizationVisitor(MySymbolTable symbolTable) {
        fillTypesMap();
        this.symbolTable = symbolTable;

        addVisit("ClassDeclaration", this::handleClassDeclaration);
        addVisit("ImportDeclaration", this::handleImportDeclaration);
        addVisit("MethodDeclaration", this::handleMethodDeclaration);
        addVisit("MainDeclaration", this::handleMainDeclaration);
        addVisit("Assignment", this::handleAssignment);
        addVisit("MethodCall", this::handleMethodCall);
        addVisit("ReturnExpression", this::handleReturnExpression);

        setDefaultVisit(this::defaultVisit);
    }

    public String handleClassDeclaration(JmmNode node, String ollirCode) {
        this.className = firstChildName(node);
        types.put(firstChildName(node), firstChildName(node));
        String ret = "";
        if (node.getChildren().get(1).getKind().equals("Extends"))
            ret += this.className + " extends " + node.getChildren().get(1).get("extendedClass") + " {\n";
        else
            ret += this.className + " {\n";
        ret += "    .construct " + this.className + "().V {\n";
        ret += "        invokespecial(this, \"<init>\").V;\n";
        ret += "    }\n\n";
        return ret + defaultVisit(node, ollirCode) + "}";
    }

    public String handleImportDeclaration(JmmNode node, String ollirCode) {
        this.imports.add(node.get("name"));
        return "";
    }

    public String handleMethodDeclaration(JmmNode node, String ollirCode) {
        this.actualMethodName = node.get("name");
        this.actualMethodType = firstChildName(node);
        this.parameters = new ArrayList<String>();

        if (this.actualMethodType.equals("void")) {
            JmmNode nodeR = new JmmNodeImpl("ReturnExpression");
            node.add(nodeR);
        }

        String init = "    .method public " + this.actualMethodName;
        String methodParameters = "(" + methodParameters(node) + ")";
        String methodType = "." + getMethodType(node) + " {\n";

        return init + methodParameters + methodType + defaultVisit(node, ollirCode) + "    }\n\n";
    }

    public String handleMainDeclaration(JmmNode node, String ollirCode) {
        this.actualMethodName = "MainDeclaration";

        String init = "    .method public static main";
        String args = "(" + firstChildName(node) + ".array.String).V {\n";
        this.parameters = new ArrayList<String>();
        //JmmNode nodeR = new JmmNodeImpl("ReturnExpression");
        //node.add(nodeR);

        return init + args + defaultVisit(node, ollirCode) + "    }\n\n";
    }

    public String handleMethodCall(JmmNode node, String ollirCode) {
        String scope = getScope(node);
        //return getMethodCall(node, new String(), node.get("name"), 0, getTypeReturnedByNode(node, scope)); // TODO
        return "";
    }

    public String handleReturnExpression(JmmNode node, String ollirCode) {
        String ret = "        ret." + types.get(this.actualMethodType) + " ";
        if (node.getChildren().isEmpty()) {
            return "        ret.V;\n";
        }
        String var = firstChildName(node);
        int varIsArg = checkVarIsArg(var);
        if (varIsArg != 0) {
            var = "$" + String.valueOf(varIsArg) + "." + var + "." + types.get(this.actualMethodType);
        }
        else {
            var = var + "." + types.get(this.actualMethodType);
        }
        return ret + var + defaultVisit(node, ollirCode) + ";\n";
    }

    /*-------------------------------------------------------------------------------------------------*/

    public String getType(JmmNode node, String scope){
        String tempType = new String();
        String nodeName = new String();
        if (node.getKind().equals("ArrayAccess"))
        {
            nodeName = node.getChildren().get(0).get("name");
        }
        else
        {
            nodeName = node.get("name");
        }

        if (booleanArray.contains(node.getKind()))
        {
            return "boolean";
        }
        else if (intArray.contains(node.getKind()))
        {
            return "int";
        }
        else if (newInstanceArray.contains(node.getKind()))
        {
            return node.get("name");
        }

        for (Map.Entry<JmmNode, MySymbol> entry : symbolTable.getTable().entrySet()) {
            MySymbol symbol = entry.getValue();

            if (symbol.getName().equals(nodeName) && (symbol.getScope().equals("GLOBAL") || symbol.getScope().equals(scope))) {
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

    /**
     * 
     * @param node Node to evaluate scope of 
     * @return Returns the scope of the node given
     */
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

    public String handleAssignment(JmmNode node, String ollirCode) { //TODO: tratar types
        String scope = getScope(node);
        String type = getTypeToOllir(getTypeReturnedByNode(node, scope));
        String line = "        ";
        switch (node.getChildren().get(0).getKind()) {
            case "Var":
                line = line + node.getChildren().get(0).get("name") + "." + type + " :=." + type + " "; 
                break;
        
            default:
                System.out.println("Unexpected behaviour: handleAssignment");
                break;
        }
        
        switch (node.getChildren().get(1).getKind()) {
            case "IntegerLiteral": case "RestIdentifier": case "True": case "False": case "NewInstance":
                line = line + terminalNode(node.getChildren().get(1)) + ";";
                break;
            case "Operation": case "And": case "Less":
                line = line + operationNode(node.getChildren().get(1)) + ";";  //TODO: Caso de não precisar de variáveis auxiliares
                break;
            case "MethodCall":
                line = line + methodCallNode(node.getChildren().get(1)) + ";";
                break;
            case "Exp":
                line = line + expNode(node.getChildren().get(1)) + ";";
                break;
            case "Not":
                line = line + notNode(node.getChildren().get(1)) + ";";
                break;
            // case "New": case "NewArray":
            //     line = line + newNode(node.getChildren().get(1)) + ";";
            //     break;
            default:
                System.out.println("Unexpected behaviour: handleAssignment");
                break;
        }
        this.assignmentOllir += line + defaultVisit(node, ollirCode) + "\n";
        String toReturn = this.assignmentOllir;
        this.assignmentOllir = new String();
        return toReturn;
    }

    private String notNode(JmmNode node) {
        String operation = new String();
        if (node.getKind().equals("And")) {
            operation = "&&";
        }
        else if (node.getKind().equals("Less")) {
            operation = "<";
        }
        else {
            operation = node.get("name");
        }
        String scope = getScope(node);
        String type = getTypeToOllir(getTypeReturnedByNode(node, scope));
        String toReturn = "aux" + this.auxNumber + "." + type;
        String line = "        aux" + this.auxNumber + "." + type + " :=." + type + " ";
        this.auxNumber++;

        switch (node.getChildren().get(0).getKind()) {
            case "IntegerLiteral": case "RestIdentifier": 
                line = line + operation + "." + type + " " + terminalNode(node.getChildren().get(0)) + ";\n";
                break;
            case "Operation": case "And":
                line = line + operation + "." + type + " " + operationNode(node.getChildren().get(0)) + ";\n";
                break;
            case "MethodCall":
                line = line + operation + "." + type + " " + methodCallNode(node.getChildren().get(0)) + ";\n";
                break;
            case "Exp":
                line = line + operation + "." + type + " " + expNode(node.getChildren().get(0)) + ";\n";
                break;
            case "True": case "False":
                line = line + operation + "." + type + " " + terminalNode(node.getChildren().get(0)) + ";\n";
                break;
            default:
                System.out.println("Unexpected behaviour: notNode");
                break;
        }

        this.assignmentOllir += line;
        
        return toReturn;
    }

    private String newNode(JmmNode node) { //TODO: types
        String operation = new String();
        if (node.getKind().equals("And")) {
            operation = "&&";
        }
        else if (node.getKind().equals("Less")) {
            operation = "<";
        }
        else {
            operation = node.get("name");
        }
        String scope = getScope(node);
        String type = getTypeToOllir(getTypeReturnedByNode(node, scope));
        String toReturn = "aux" + this.auxNumber + "." + type;
        String line = "        aux" + this.auxNumber + "." + type + " :=." + type + " ";
        this.auxNumber++;

        switch (node.getChildren().get(0).getKind()) {
            case "IntegerLiteral": case "RestIdentifier": 
                line = line + terminalNode(node.getChildren().get(0)) + " " + operation + "." + type + " ";
                break;
            case "Operation":
                line = line + operationNode(node.getChildren().get(0)) + " " + operation + "." + type + " ";
                break;
            case "MethodCall":
                line = line + methodCallNode(node.getChildren().get(0)) + " " + operation + "." + type + " ";
                break;
            case "Exp":
                line = line + expNode(node.getChildren().get(0)) + " " + operation + "." + type + " ";
                break;
            case "True": case "False":
                line = line + terminalNode(node.getChildren().get(0)) + " " + operation + ".bool ";
                break;
            default:
                System.out.println("Unexpected behaviour: operationNode");
                break;
        }

        switch (node.getChildren().get(1).getKind()) {
            case "IntegerLiteral": case "RestIdentifier": case "True": case "False":
                line = line + terminalNode(node.getChildren().get(1)) + ";\n";
                break;
            case "Operation":
                line = line + operationNode(node.getChildren().get(1)) + ";\n";
                break;
            case "MethodCall":
                line = line + methodCallNode(node.getChildren().get(1)) + ";\n";
                break;
            case "Exp":
                line = line + expNode(node.getChildren().get(1)) + ";\n";
                break;
            default:
                System.out.println("Unexpected behaviour: operationNode");
                break;
        }

        this.assignmentOllir += line;
        
        return toReturn;
    }

    private String expNode(JmmNode node) {
        String toReturn = new String();      
        
        switch (node.getChildren().get(0).getKind()) {
            case "Exp":
                toReturn = expNode(node.getChildren().get(0));
                break;
            case "Operation": case "And": case "Less":
                toReturn = operationNode(node.getChildren().get(0));
                break;
            case "MethodCall":
                toReturn = methodCallNode(node.getChildren().get(0));
                break;
            case "IntegerLiteral": case "RestIdentifier": case "True": case "False":
                toReturn = terminalNode(node.getChildren().get(0));
                break;
            case "Not":
                toReturn = notNode(node.getChildren().get(0));
                break;
            default:
                System.out.println("Unexpected behaviour: expNode");
                break;
        }
        
        
        return toReturn;
    }

    private String methodCallNode(JmmNode node) {  //TODO: types
        String scope = getScope(node);
        String type = getTypeToOllir(getTypeReturnedByNode(node, scope));
        String toReturn = "aux" + this.auxNumber + "." + type;
        String line = "aux" + this.auxNumber + "." + type + " :=." + type + " ";
        this.auxNumber++;
        
        switch (node.getChildren().get(0).getKind()) {
            case "This":
                line = line + "invokevirtual(this, " + "\"" + node.get("name") + "\"";
                break;
            case "RestIdentifier":
                if (varInImports(node.getChildren().get(0))) 
                {
                    line = line + "invokestatic(" + node.getChildren().get(0).get("name") + ", \"" + node.get("name") + "\"";
                }
                else
                {
                    line = line + "invokevirtual(" + node.getChildren().get(0).get("name") + "." + getClassName(node) + ", \"" + node.get("name") + "\"";
                }
                break;
            case "Exp":
                line = line + expNode(node.getChildren().get(0));
                break;
            default:
                System.out.println("Unexpected behaviour: methodCallNode");
                break;
        }

        for (int i = 0; i < node.getChildren().get(1).getChildren().size(); i++)
        {
            switch (node.getChildren().get(1).getChildren().get(i).getKind()) {
                case "IntegerLiteral": case "RestIdentifier": case "True": case "False":
                    line = line + ", " + terminalNode(node.getChildren().get(1).getChildren().get(i));
                    break;
                case "Operation":
                    line = line + ", " + operationNode(node.getChildren().get(1).getChildren().get(i));
                    break;
                case "Exp":
                    line = line + ", " + expNode(node.getChildren().get(1).getChildren().get(i));
                    break;
                default:
                    System.out.println("Unexpected behaviour: methodCallNode");
                    break;
            }
        }

        line = line + ")." + type + ";\n";
        this.assignmentOllir += line;
        return toReturn;
    }

    private String operationNode(JmmNode node) { //TODO: types
        String operation = new String();
        if (node.getKind().equals("And")) {
            operation = "&&";
        }
        else if (node.getKind().equals("Less")) {
            operation = "<";
        }
        else {
            operation = node.get("name");
        }
        String scope = getScope(node);
        String type = getTypeToOllir(getTypeReturnedByNode(node, scope));
        String toReturn = "aux" + this.auxNumber + "." + type;
        String line = "        aux" + this.auxNumber + "." + type + " :=." + type + " ";
        this.auxNumber++;

        switch (node.getChildren().get(0).getKind()) {
            case "IntegerLiteral": case "RestIdentifier": 
                line = line + terminalNode(node.getChildren().get(0)) + " " + operation + "." + type + " ";
                break;
            case "Operation":
                line = line + operationNode(node.getChildren().get(0)) + " " + operation + "." + type + " ";
                break;
            case "MethodCall":
                line = line + methodCallNode(node.getChildren().get(0)) + " " + operation + "." + type + " ";
                break;
            case "Exp":
                line = line + expNode(node.getChildren().get(0)) + " " + operation + "." + type + " ";
                break;
            case "True": case "False":
                line = line + terminalNode(node.getChildren().get(0)) + " " + operation + ".bool ";
                break;
            default:
                System.out.println("Unexpected behaviour: operationNode");
                break;
        }

        switch (node.getChildren().get(1).getKind()) {
            case "IntegerLiteral": case "RestIdentifier": case "True": case "False":
                line = line + terminalNode(node.getChildren().get(1)) + ";\n";
                break;
            case "Operation":
                line = line + operationNode(node.getChildren().get(1)) + ";\n";
                break;
            case "MethodCall":
                line = line + methodCallNode(node.getChildren().get(1)) + ";\n";
                break;
            case "Exp":
                line = line + expNode(node.getChildren().get(1)) + ";\n";
                break;
            default:
                System.out.println("Unexpected behaviour: operationNode");
                break;
        }

        this.assignmentOllir += line;
        
        return toReturn;
    }

    public String terminalNode(JmmNode node) //TODO: tratar types
    {
        String toReturn = new String();
        switch (node.getKind()) {
            case "IntegerLiteral": case "RestIdentifier":
                toReturn = node.get("name") + ".i32";
                break;
            case "True":
                toReturn = "1.bool";
                break;
            case "False":
                toReturn = "0.bool";
                break;
            case "NewInstance":
                toReturn = "new(" + node.get("name") + ")." + node.get("name");
                break;
            default:
                System.out.println("Unexpected behaviour: terminalNode");
                break;
        }
        return toReturn;
    }

    public Boolean varInImports(JmmNode node){
        for (Map.Entry<JmmNode, MySymbol> entry : symbolTable.getTable().entrySet()) {
            JmmNode tempNode = entry.getKey();
            MySymbol symbol = entry.getValue();

            if (tempNode.getKind().equals("ImportDeclaration") && symbol.getName().equals(node.get("name"))){
                return true;
            }
        }
        return false;
    }

    public String getClassName(JmmNode node)
    {
        return node.getAncestor("ClassDeclaration").get().getChildren().get(0).get("name");
    }

    /*-------------------------------------------------------------------------------------------------*/


    public String handleInsideFunction(JmmNode node) {
        List<String> a = new ArrayList<String>();
        String methodCall = node.get("name");
        String restIdent = node.getChildren().get(0).get("name");
        a.add(restIdent);
        a.add("\"" + methodCall + "\"");
        for (JmmNode child : node.getChildren().get(1).getChildren()) {
            a.add(child.get("name") + "." + types.get(varTypeST(child.get("name"), this.actualMethodName)));
        }
        return String.join(", ", a);
    }

    public String handleInsideFunctionVirtual(JmmNode node) {
        List<String> a = new ArrayList<String>();
        String methodCall = node.get("name");
        String restIdent = node.getChildren().get(0).get("name") + "." + types.get(varTypeST("s", this.actualMethodName));
        a.add(restIdent);
        a.add("\"" + methodCall + "\"");
        for (JmmNode child : node.getChildren().get(1).getChildren()) {
            a.add(child.get("name") + "." + types.get(varTypeST(child.get("name"), this.actualMethodName)));
        }
        return String.join(", ", a);
    }

    private String defaultVisit(JmmNode node, String ollirCode) {
        String ret = "";
        if (!(node.getChildren().isEmpty())) {
            for (JmmNode child : node.getChildren()) {
                ret += visit(child, ollirCode);
            }
        }
        return ret;
    }

    /**
     * 
     * @param node Node to be evaluated
     * @return Name of the first child's name
     */
    public String firstChildName(JmmNode node) {
        return node.getChildren().get(0).get("name");
    }

    /**
     * 
     * @param node Node of the method
     * @return Type of method
     */
    public String getMethodType(JmmNode node) {
        String type = types.get(firstChildName(node));
        if (type.equals("null")) {
            type = firstChildName(node);
        }
        return type;
    }

    /**
     * @param node Node of the method
     * @return String containing the method parameters
     */
    public String methodParameters(JmmNode node) {
        int i = 1;
        List<JmmNode> children = node.getChildren();

        while (i < children.size()) {
            JmmNode child = children.get(i);

            if (child.getKind().equals("Parameter")) {
                String paramType = types.get(firstChildName(child));
                parameters.add(child.get("name") + "." + paramType);
            }
            else break;
            i++;
        }
        return String.join(", ", parameters);
    }

    /**
     * 
     * @param var Variable to check
     * @return Return index of parameter on array or 0
    */
    public int checkVarIsArg(String var) {
        for (int i = 0; i < this.parameters.size(); i++) {
            if (this.parameters.get(i).startsWith(var)) {
                return this.parameters.get(i).indexOf(var) + 1;
            }
        }
        return 0;
    }

    /**
     * 
     * @param var Variable to check
     * @param methodName Method where variable is at
     * @return Type of variable or "" if not found
    */
    public String varTypeST(String var, String methodName) {
        for (Map.Entry<JmmNode, MySymbol> entry : symbolTable.getTable().entrySet()) {
            MySymbol symbol = entry.getValue();

            if (symbol.getName().equals(var) &&
                    (symbol.getScope().equals(methodName) || symbol.getScope().equals("GLOBAL"))) {
                return symbol.getType().getName();
            }
        }
        return "";
    }

    /**
     * Fills types variables with corresponding OLLIR types
    */
    public void fillTypesMap() {
        types.put("int", "i32");
        types.put("boolean", "bool");
        types.put("String", "String");
        types.put("int[]", "array.i32");
        types.put("String[]", "array.String");
        types.put("void", "V");
        
    }
    

    public String getTypeToOllir(String type)
    {
        switch (type) {
            case "int":
                return "i32";
            case "boolean":
                return "bool";
            case "int[]":
                return "array.i32";
            case "String[]":
                return "array.String";
            case "void":
                return "V";
            default:
                return type;
        }
    
    }
}