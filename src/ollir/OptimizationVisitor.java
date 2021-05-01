package ollir;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import semantic.MySymbolTable;


class OptimizationVisitor extends AJmmVisitor<String, String> {

    private MySymbolTable symbolTable = new MySymbolTable();
    private Map<String, String> types = new HashMap<>();
    private String className = "";
    private String actualMethodName = "";
    private String actualMethodType = "";

    public OptimizationVisitor(MySymbolTable symbolTable) {
        fillTypesMap();
        this.symbolTable = symbolTable;

        addVisit("ClassDeclaration", this::handleClassDeclaration);
        addVisit("MethodDeclaration", this::handleMethodDeclaration);
        addVisit("MainDeclaration", this::handleMainDeclaration);
        addVisit("Assignment", this::handleAssignment);
    
        // Not needed for CP2
        addVisit("ImportDeclaration", this::handleImportDeclaration);
        addVisit("VarDeclaration", this::handleVarDeclaration);
        addVisit("ReturnExpression", this::handleReturnExpression);
        addVisit("ifStatement", this::handleIf);
        addVisit("elseStatement", this::handleElse);

        setDefaultVisit(this::defaultVisit);
    }

    public String firstChildName(JmmNode node) {
        return node.getChildren().get(0).get("name");
    }

    public String getMethodType(JmmNode node) {
        return types.get(firstChildName(node));
    }

    public String handleClassDeclaration(JmmNode node, String ollirCode) { // DONE
        this.className = firstChildName(node);
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

    public String handleMethodDeclaration(JmmNode node, String ollirCode) {
        this.actualMethodName = node.get("name");
        this.actualMethodType = firstChildName(node);

        String init = "    .method public " + this.actualMethodName;
        String parameters = "(" + methodParameters(node) + ")";
        String methodType = "." + getMethodType(node) + " {\n";

        return init + parameters + methodType + defaultVisit(node, ollirCode) + "    }\n\n";
    }

    public String methodParameters(JmmNode node) {
        List<String> parameters = new ArrayList<String>();
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

    public String handleMainDeclaration(JmmNode node, String ollirCode) {
        String init = "    .method public static main";
        String args = "(" + firstChildName(node) + ".array.String).V {\n";

        return init + args + defaultVisit(node, ollirCode) + "    }\n\n";
    }

    public String handleReturnExpression(JmmNode node, String ollirCode) {
        String ret = "        ret." + types.get(this.actualMethodType) + " ";
        String var = firstChildName(node) + "." + types.get(this.actualMethodType);
        return ret + var + ";\n" + defaultVisit(node, ollirCode);
    }

    public String handleAssignment(JmmNode node, String ollirCode) {
        String var = node.getChildren().get(0).get("name");
        //String varType = symbolTable.getValue(var);

        return "        // " + var + ".\n" + defaultVisit(node, ollirCode);
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

    public void fillTypesMap() {
        types.put("int", "i32");
        types.put("boolean", "bool");
        types.put("String", "String");
        types.put("int[]", "array.i32");
        types.put("String[]", "array.String");
        types.put("void", "V");
    }

    public String handleImportDeclaration(JmmNode node, String ollirCode) {

        return "// Import: Not needed.\n\n" + defaultVisit(node, ollirCode);
    }

    public String handleVarDeclaration(JmmNode node, String ollirCode) {

        return "        // VarDeclaration: Not needed.\n" + defaultVisit(node, ollirCode);
    }

    public String handleIf(JmmNode node, String ollirCode) {

        return "        // If: Not needed.\n" + defaultVisit(node, ollirCode);
    }

    public String handleElse(JmmNode node, String ollirCode) {

        return "        // Else: Not needed.\n" + defaultVisit(node, ollirCode);
    }

    //public String getOllirCode() { return ollirCode; }
    /*
    public String getOllirCode() { return "Fac {" + 
        ".construct Fac().V {" + 
            "invokespecial(this, \"<init>\").V;" + 
        "}"
        +
        ".method public compFac(num.i32).i32 {" + 
            "if ($1.num.i32 >=.i32 1.i32) goto else;" + 
                "num_aux.i32 :=.i32 1.i32;" +
                "goto endif;" +
            "else:" + 
                "aux1.i32 :=.i32 $1.num.i32 -.i32 1.i32;" + 
                "aux2.i32 :=.i32 invokevirtual(this, \"compFac\", aux1.i32).i32;" + 
                "num_aux.i32 :=.i32 $1.num.i32 *.i32 aux2.i32;" + 
            "endif:" + 
                "ret.i32 num_aux.i32;" + 
        "}" + 
        
        ".method public static main(args.array.String).V {" + 
            "aux1.Fac :=.Fac new(Fac).Fac;" + 
            "invokespecial(aux1.Fac,\"<init>\").V;" +
            "aux2.i32 :=.i32 invokevirtual(aux1.Fac,\"compFac\",10.i32).i32;" + 
            "invokestatic(io, \"println\", aux2.i3).V;" + 
        "}" + 
    "}"; }*/
}