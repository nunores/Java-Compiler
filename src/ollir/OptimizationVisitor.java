import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

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
    private Integer globalVarNumber = 0;
    private Integer prevInitialVarNumber = 0;
    List<String> booleanArray = Arrays.asList("True", "False", "Less", "And", "Or", "Not");
    List<String> intArray = Arrays.asList("IntegerLiteral", "DotLength", "ArrayAccess");
    List<String> varArray = Arrays.asList("MethodCall", "RestIdentifier");
    List<String> newInstanceArray = Arrays.asList("NewInstance");
    List<String> typeReturner = Arrays.asList("True", "False", "Less", "And", "Or", "Not", "IntegerLiteral", "DotLength", "ArrayAccess", "MethodCall", "RestIdentifier", "NewInstance");
    List<String> nodeTypes = Arrays.asList("int", "boolean", "int[]");

    public OptimizationVisitor(MySymbolTable symbolTable) {
        fillTypesMap();
        this.symbolTable = symbolTable;

        addVisit("ClassDeclaration", this::handleClassDeclaration); // DONE
        addVisit("ImportDeclaration", this::handleImportDeclaration); // DONE
        addVisit("MethodDeclaration", this::handleMethodDeclaration); // DONE
        addVisit("MainDeclaration", this::handleMainDeclaration); // DONE
        addVisit("Assignment", this::handleAssignment); // TO FINISH
        addVisit("MethodCall", this::handleMethodCall); // TO FINISH
        addVisit("NewInstance", this::handleNewInstance); // TO DO
        addVisit("Operation", this::handleOperation); // TO DO
        addVisit("ReturnExpression", this::handleReturnExpression); // DONE

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
        String init = "    .method public static main";
        String args = "(" + firstChildName(node) + ".array.String).V {\n";
        this.parameters = new ArrayList<String>();
        JmmNode nodeR = new JmmNodeImpl("ReturnExpression");
        node.add(nodeR);

        return init + args + defaultVisit(node, ollirCode) + "    }\n\n";
    }

    public String handleMethodCall(JmmNode node, String ollirCode) {
        String restIdent = node.getChildren().get(0).get("name");
        String methodCall = node.get("name");
        String ret = "";
        if (this.imports.contains(restIdent) && node.getChildren().get(1).getKind().equals("InsideFunction")) {
            ret = "        invokestatic(" + handleInsideFunction(node) + ").V;\n";
        }
        return ret + defaultVisit(node, ollirCode);
    }

    public String handleReturnExpression(JmmNode node, String ollirCode) {
        String ret = "        ret." + types.get(this.actualMethodType) + " ";
        if (node.getChildren().isEmpty()) {
            return "        ret.V;\n";
        }
        String var = firstChildName(node);
        int varIsArg = checkVarIsArg(var);
        if (varIsArg != 0) {
            var = "$" + String.valueOf(varIsArg) + var + "." + types.get(this.actualMethodType);
        }
        else {
            var = var + "." + types.get(this.actualMethodType);
        }
        return ret + var + defaultVisit(node, ollirCode) + ";\n";
    }

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

    public String handleAssignment(JmmNode node, String ollirCode) {
        // String var = node.getChildren().get(0).get("name"); // num_aux
        // String varType = types.get(varTypeST(var, this.actualMethodName)); // i32 
        // String varRet = var + "." + varType; // num_aux.i32
        // String ret = "";

        // int varIsArg = checkVarIsArg(var);
        // if (varIsArg != 0) {
        //     varRet = "$" + String.valueOf(varIsArg) + varRet; // $1.num.i32
        // }

        // JmmNode arg2 = node.getChildren().get(1);
        // if (arg2.getKind().equals("Operation")) {
        //     String op = arg2.get("name");
        //     String str = varRet + " .=." + varType + " \n";
        //     ret = str;
        // }
        // else {
        //     String var2 = node.getChildren().get(1).get("name");
        //     ret = varRet + " :=." + varType + " " + var2 + "." + varType + defaultVisit(node, ollirCode) + ";\n\n";
        // }

        // return "        // " + ret + defaultVisit(node, ollirCode);

        String scope = getScope(node);
        //System.out.println(getTypeReturnedByNode(node.getChildren().get(1), scope));

        return getAssignment(node.getChildren().get(1), new String(), node.getChildren().get(0).get("name"), 0, getTypeReturnedByNode(node.getChildren().get(1), scope)); // TODO

    }

    public String getAssignment(JmmNode node, String result, String var, int varNumber, String type)
    {

        int nextVarNumber = varNumber + 1;
        
        if (node.getKind().equals("Operation"))
        {
            result += getOperation(node, result, var, type); // TODO: Get type com knowledge (função OP)
        }
        else if (node.getKind().equals("MethodCall"))
        {
            result += getMethodCall(node, result, "aux", nextVarNumber, type);
        }
        else if (node.getKind().equals("Exp"))
        {
            result += getAssignment(node.getChildren().get(0), result, "aux", nextVarNumber, type);   
        }
        else if (node.getKind().equals("IntegerLiteral") || node.getKind().equals("RestIdentifier") || node.getKind().equals("True") || node.getKind().equals("False"))
        {
            int varIsArg = checkVarIsArg(var);
            if (varIsArg != 0) {
                var = "$" + String.valueOf(varIsArg) + "." + var;
            }
            
            return "\t" + var + "." + types.get(type) + " :=." + types.get(type) + " " + node.get("name") + "." + types.get(type) + ";\n";
        }
        else if (node.getKind().equals("NewInstance")) {
            return "\t" + var + "." + types.get(type) + " :=." + types.get(type) + " new(" + types.get(type) + ")." + types.get(type) + ";\n";
        }
        



        // if (node.getChildren().get(1).getKind().equals("Operation"))
        // {

        // }
        // else if (node.getChildren().get(1).getKind().equals("MethodCall"))
        // {

        // }
        // else if (node.getChildren().get(1).getKind().equals("Exp"))
        // {
        //     result += getAssignment(node.getChildren().get(1), result);   
        // }

        return result;
    }

    public String getOperation(JmmNode node, String result, String var, String type)
    {
        String firstChild = new String();
        String secondChild = new String();
        String newResult = result;
        Integer initialVarNumber = this.globalVarNumber;
        
        if (node.getChildren().get(0).getKind().equals("IntegerLiteral") || node.getChildren().get(0).getKind().equals("RestIdentifier"))
        {
            firstChild = node.getChildren().get(0).get("name");
        }
        else if(node.getChildren().get(0).getKind().equals("Operation"))
        {
            this.globalVarNumber++;
            newResult += getOperation(node.getChildren().get(0), result, "aux", type);
            firstChild = "aux" + String.valueOf(this.prevInitialVarNumber);
        }
        else if(node.getChildren().get(0).getKind().equals("MethodCall"))
        {
            /*this.globalVarNumber++;
            newResult += getMethodCall(node.getChildren().get(0), result, "aux", type);
            firstChild = "aux" + String.valueOf(this.prevInitialVarNumber);*/
        }
        else if(node.getChildren().get(0).getKind().equals("Exp"))
        {

        }

        if (node.getChildren().get(1).getKind().equals("IntegerLiteral") || node.getChildren().get(1).getKind().equals("RestIdentifier"))
        {
            int varIsArg = checkVarIsArg(var);
            if (varIsArg != 0) {
                var = "$" + String.valueOf(varIsArg) + "." + var;
            }

            secondChild = node.getChildren().get(1).get("name");
            int secondChildIsArg = checkVarIsArg(secondChild);
            if (secondChildIsArg != 0) {
                secondChild = "$" + String.valueOf(secondChildIsArg) + "." + secondChild;
            }

            if (initialVarNumber == 0)
            {
                newResult += "\t" + var + "." + types.get(type) + " :=." + types.get(type) + " " + firstChild + "." + types.get(type) + " " + node.get("name") + "." + types.get(type) + " " + secondChild + "." + types.get(type) + ";\n";
            }
            else
            {
                newResult += "\t" + var + String.valueOf(initialVarNumber) + "." + types.get(type) + " :=." + types.get(type) + " " + firstChild + "." + types.get(type) + " " + node.get("name") + "." + types.get(type) + " " + secondChild + "." + types.get(type) + ";\n";
            }
        }
        else if(node.getChildren().get(1).getKind().equals("Operation"))
        {
            this.globalVarNumber++;
            newResult += getOperation(node.getChildren().get(1), result, "aux", type);
            secondChild = "aux" + String.valueOf(this.prevInitialVarNumber);

            int varIsArg = checkVarIsArg(var);
            if (varIsArg != 0) {
                var = "$" + String.valueOf(varIsArg) + "." + var;
            }

            int secondChildIsArg = checkVarIsArg(secondChild);
            if (secondChildIsArg != 0) {
                secondChild = "$" + String.valueOf(secondChildIsArg) + "." + secondChild;
            }

            if (initialVarNumber == 0)
            {
                newResult += "\t" + var + "." + types.get(type) + " :=." + types.get(type) + " " + firstChild + "." + types.get(type) + " " + node.get("name") + "." + types.get(type) + " " + secondChild + "." + types.get(type) + ";\n";
            }
            else
            {
                newResult += "\t" + var + String.valueOf(initialVarNumber) + "." + types.get(type) + " :=." + types.get(type) + " " + firstChild + "." + types.get(type) + " " + node.get("name") + "." + types.get(type) + " " + secondChild + "." + types.get(type) + ";\n";
            }
        }
        else if(node.getChildren().get(1).getKind().equals("MethodCall"))
        {
            
        }
        else if(node.getChildren().get(1).getKind().equals("Exp"))
        {

        } 
        
        this.prevInitialVarNumber = initialVarNumber;
        return newResult;
    }

    public String getMethodCall(JmmNode node, String result, String var, int varNumber, String type)
    {
        /*String invokeType = new String();
        if (node.getChildren().get(0).getKind().equals("This"))
        {
            invokeType = "virtual";
        }
        else
        {
            invokeType = "static";
        }

        for (int i = 0; i < node.getChildren().get(1).size(); i++)
        {
            
        }*/
        
        return new String();
    }


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

    public String handleNewInstance(JmmNode node, String ollirCode) {
        return "        // newInstance\n";
    }

    public String handleOperation(JmmNode node, String ollirCode) {
        return "        // handleOperation\n";
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

    public String firstChildName(JmmNode node) {
        return node.getChildren().get(0).get("name");
    }

    public String getMethodType(JmmNode node) {
        String type = types.get(firstChildName(node));
        if (type.equals("null")) {
            type = firstChildName(node);
        }
        return type;
    }

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

    public int checkVarIsArg(String var) {
        for (int i = 0; i < this.parameters.size(); i++) {
            if (this.parameters.get(i).startsWith(var)) {
                return this.parameters.get(i).indexOf(var) + 1;
            }
        }
        return 0;
    }

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

    public void fillTypesMap() {
        types.put("int", "i32");
        types.put("boolean", "bool");
        types.put("String", "String");
        types.put("int[]", "array.i32");
        types.put("String[]", "array.String");
        types.put("void", "V");
    }
}