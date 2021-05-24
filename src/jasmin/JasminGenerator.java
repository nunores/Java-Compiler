import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.*;
import java.util.*;


public class JasminGenerator {
    private ClassUnit classUnit;
    private Method method;
    private int stackCount = -1;
    private int maxstack = 0;
    private HashMap<String, Instruction> labels = new HashMap<>();
    
    public JasminGenerator(ClassUnit classUnit) {
        this.classUnit = classUnit;
    }
    
    private String jasminAccessModifier(AccessModifiers accessModifiers) {
        if (accessModifiers.name().equals("PRIVATE")) return "private";
        else return "public";
    }

    public String classToJasmin() {
        String jasminString = ".class " + jasminAccessModifier(classUnit.getClassAccessModifier()) + " " + classUnit.getClassName() + "\n";
        jasminString += ".super ";
        if (classUnit.getSuperClass() == null || classUnit.getSuperClass().equals(classUnit.getClassName())) {
            jasminString += "java/lang/Object\n";
        }
        else {
            jasminString += classUnit.getSuperClass() + "\n";
        }
        return jasminString + fieldsToJasmin() + methodToJasmin();
    }

    private String jasminIsStatic(boolean isStatic) {
        if (isStatic) return "static ";
        else return "";
    }

    public String methodToJasmin() {
        String jasminString = "";
        for (Method method: classUnit.getMethods()) {
            this.stackCount = 0;
            this.method = method;
            this.labels = method.getLabels();
            jasminString += "\n.method ";
            boolean hasReturnInstr = false;
            if (method.isConstructMethod()) {
                for (Instruction i: method.getInstructions()) {
                    if (i instanceof ReturnInstruction) {
                        hasReturnInstr = true;
                        break;
                    }
                }
                if (!hasReturnInstr) {
                    method.addInstr(new ReturnInstruction(new Element(new Type(ElementType.VOID))));
                }
                jasminString += jasminAccessModifier(method.getMethodAccessModifier()) + " " +
                    jasminIsStatic(method.isStaticMethod()) + "<init>(";
                for (Element element: method.getParams()) {
                    jasminString += convertElementType(element.getType());
                } 
                jasminString += ")" + convertElementType(method.getReturnType()) + "\n";
            
            }
            else {
                for (Instruction i: method.getInstructions()) {
                    if (i instanceof ReturnInstruction) {
                        hasReturnInstr = true;
                        break;
                    }
                }
                if (!hasReturnInstr) {
                    method.addInstr(new ReturnInstruction(new Element(new Type(ElementType.VOID))));
                }
                jasminString += jasminAccessModifier(method.getMethodAccessModifier()) + " " +
                    jasminIsStatic(method.isStaticMethod()) + method.getMethodName()+"(";
                for (Element element: method.getParams()) {
                    jasminString += convertElementType(element.getType());
                } 
                jasminString += ")" + convertElementType(method.getReturnType()) + "\n";
            }

            String toAddNext = instructionToJasmin(method);
            toAddNext += ".end method\n";

            if (!method.isConstructMethod()) {
                jasminString += "\t.limit stack 99" /*+ getStack()*/ + "\n";
                jasminString += "\t.limit locals " + String.valueOf(method.getVarTable().size() + 1) + "\n";
            }

            jasminString += toAddNext;
        }
        return jasminString;
    }

    private String ArrayTypeToJasmin(ArrayType type){
        switch (type.getTypeOfElements()){
            case STRING:
                return "Ljava/lang/String;";
            case INT32:
                return "I";
            case BOOLEAN:
                return "B";
            case CLASS:
                return classUnit.getClassName();
            default:    
                return "";
        }
    }

    public String convertElementType(Type type){
        switch (type.getTypeOfElement()){
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case ARRAYREF:
                return "[" + ArrayTypeToJasmin((ArrayType) type);
            case OBJECTREF:
                return "OBJ";
            case CLASS:
                return "C";
            case THIS:
                return "T";
            case STRING:
                return "Ljava/lang/String;";
            case VOID:
                return "V";
            default:
                return "V"; 
        }
    }

    public String instructToJasmin(Instruction instruction, HashMap<String, Descriptor> varTable, HashMap<String, Instruction> methodLabels){
        String jasminString = "";
        for (Map.Entry<String, Instruction> entry: methodLabels.entrySet()) {
            if (entry.getValue().equals(instruction)) {
                jasminString += entry.getKey() + ":\n";
                break;
            }
        }

        if (instruction instanceof AssignInstruction) {
            AssignInstruction instruction2 = (AssignInstruction)instruction;
            jasminString += instructToJasmin(instruction2.getRhs(), varTable, methodLabels);
            Operand operand = (Operand)instruction2.getDest();

            jasminString += this.storeElement(operand, varTable);

            return jasminString;
        }
        
        if (instruction instanceof SingleOpInstruction) {
            SingleOpInstruction instruction2 = (SingleOpInstruction)instruction;
            return loadElement(instruction2.getSingleOperand(), varTable);
        }

        if (instruction instanceof BinaryOpInstruction) {
            stackCount -=1; maxStack();
            BinaryOpInstruction instruction2 = (BinaryOpInstruction)instruction;
            String left = loadElement(instruction2.getLeftOperand(), varTable);
            String right = loadElement(instruction2.getRightOperand(), varTable);
            OperationType operationType = instruction2.getUnaryOperation().getOpType();
            switch(operationType) {
                case ADD:
                    return left + right + "\tiadd\n";
                case SUB:
                    return left + right + "\tisub\n";
                case MUL:
                    return left + right + "\timul\n";
                case DIV:
                    return left + right + "\tidiv\n";
                default:
                    return "default - operations\n";
            }
        }

        if(instruction instanceof ReturnInstruction){
            ReturnInstruction instruction2 = (ReturnInstruction)instruction;

            if (!instruction2.hasReturnValue()) {
                return "\treturn\n";

            }
            switch(instruction2.getOperand().getType().getTypeOfElement()){
                case VOID:
                    return "\treturn\n";

                case INT32: case BOOLEAN:
                    jasminString += loadElement(instruction2.getOperand(), varTable);
                    jasminString += "\tireturn\n";
                    return jasminString;

                case ARRAYREF:

                case OBJECTREF:
                    jasminString += loadElement(instruction2.getOperand(), varTable);
                    jasminString += "\tareturn\n";
                    return jasminString;
                default:
                    break;
            }
        }

        if (instruction instanceof CallInstruction) {
            CallInstruction instruction2 = (CallInstruction) instruction;
            Operand op = (Operand)instruction2.getFirstArg();
            switch (instruction2.getInvocationType()) {
                case invokevirtual:
                    return jasminString + invokeToJasmin(instruction2, varTable);
                case invokespecial:
                    return jasminString + invokeToJasmin(instruction2, varTable);
                case invokestatic:
                    return jasminString + invokeToJasmin(instruction2, varTable);
                case NEW:
                    jasminString += "\tnew " + op.getName() + "\n";
                    return jasminString;
                case arraylength:
                    jasminString += loadElement(instruction2.getFirstArg(), varTable);
                    jasminString += "\tarraylength\n";
                default:
            }
        }

        if (instruction instanceof GotoInstruction) {
            GotoInstruction instruction2 = (GotoInstruction) instruction;
            String goToLabel = instruction2.getLabel();
            return "\tgoto " + goToLabel + "\n";
        }

        if (instruction instanceof CondBranchInstruction){
            CondBranchInstruction instruction2 = (CondBranchInstruction) instruction;
            String left = loadElement(instruction2.getLeftOperand(), varTable);
            String right = loadElement(instruction2.getRightOperand(), varTable);
            String goToLabel = instruction2.getLabel();
            String ret = left + right;

            Element e = instruction2.getRightOperand();
            if (e.isLiteral() && ((LiteralElement)e).getLiteral().equals("0")) {
                switch(instruction2.getCondOperation().getOpType()) {
                    case LTH: ret += "\tiflt "; break;
                    case GTH: ret += "\tifgt "; break;
                    case EQ:  ret += "\tifeq "; break;
                    case NEQ: ret += "\tifne "; break;
                    case LTE: ret += "\tifle "; break;
                    case GTE: ret += "\tifge "; break;
                    default:  ret += "\tif_not_dealed"; break;
                }
            }
            else {
                switch(instruction2.getCondOperation().getOpType()){
                    case LTH: ret += "\tif_icmplt "; break;
                    case GTH: ret += "\tif_icmpgt "; break;
                    case EQ:  ret += "\tif_icmpeq "; break;
                    case NEQ: ret += "\tif_icmpne "; break;
                    case LTE: ret += "\tif_icmple "; break;
                    case GTE: ret += "\tif_icmpge "; break;
                    default:  ret += "\tif_not_dealed"; break;
                }
            }
            return ret + goToLabel + "\n";
        }

        if(instruction instanceof GetFieldInstruction){
            GetFieldInstruction instruction2 = (GetFieldInstruction) instruction;
            Element first = instruction2.getFirstOperand();
            Element second = instruction2.getSecondOperand();
            jasminString += loadElement(first, varTable);
            jasminString += "getfield " + "alguma coisa aqui";
            jasminString += storeElement((Operand)instruction2.getSecondOperand(), varTable);
            return jasminString;
            
            /*EXEMPLO DE GETFIELD 
            package xyz;     
            class Point {         
                public int xCoord, yCoord;     
            };
            
            int x = p.xCoord;

            Jasmin da linha a cima:
            
            aload_1 ; push object in local varable 1 (i.e. p) onto the stack     
            getfield xyz/Point/xCoord I  ; get the value of p.xCoord, which is an int     
            istore_2                     ; store the int value in local variable 2 (x)             
            */ 


        }
        if(instruction instanceof PutFieldInstruction){
            PutFieldInstruction instruction2 = (PutFieldInstruction) instruction;
            Element first = instruction2.getFirstOperand();
            Element second = instruction2.getSecondOperand();
            Element third = instruction2.getThirdOperand();
            jasminString += loadElement(first, varTable); 
            jasminString += loadElement(second, varTable);
            jasminString += "putfield " + third.getType();
            return jasminString;


            /*EXEMPLO DE PUTFIELD 
            package xyz;     
            class Point {         
                public int xCoord, yCoord;     
            };
            
            p.xCoord = 10;

            Jasmin da linha a cima:

            aload_1 ; push object in local varable 1 (i.e. p) onto the stack     
            bipush 10  ; push the integer 10 onto the stack     
            putfield xyz/Point/xCoord I  ; set the value of the integer field p.xCoord to 10 */ 
        }

    return jasminString;
}

    public String instructionToJasmin(Method method) {
        HashMap<String, Descriptor> varTable = method.getVarTable();
        HashMap<String, Instruction> labels = method.getLabels();
        String jasminString = "";
        for(Instruction instruction : method.getInstructions()){
            jasminString += "" + instructToJasmin(instruction, varTable, labels);
        }
        return jasminString;
    }

    public String storeElement(Operand operand, HashMap<String, Descriptor> varTable){

        switch(operand.getType().getTypeOfElement()){
            case INT32:
            case BOOLEAN:
                this.stackCount -= 1; maxStack();
                return String.format("\tistore %s\n", varTable.get(operand.getName()).getVirtualReg());
            case ARRAYREF: case OBJECTREF:
                this.stackCount -= 1; maxStack();
                return String.format("\tastore %s\n", varTable.get(operand.getName()).getVirtualReg());
            default:
                return "";
        }
    }

    public String loadElement(Element element, HashMap<String, Descriptor> varTable){
        this.stackCount += 1; maxStack();
        String jasminString = "";
        if (element instanceof LiteralElement) {
            String num = ((LiteralElement) element).getLiteral();
            int num_parsed = Integer.parseInt(num);
            if (num_parsed < 6){
                return "\ticonst_" + num + "\n";
            }
            else if (num_parsed < 128){
                return "\tbipush " + num + "\n";
            }
            else if (num_parsed < 32768){
                return "\tsipush " + num + "\n";
            }
            else {
                return "\tldc " + num + "\n";
            }
        }
        else if (element instanceof ArrayOperand) {
            ArrayOperand operand = (ArrayOperand) element;
            jasminString += String.format("\taload %s\n", varTable.get(operand.getName()).getVirtualReg());
            jasminString += loadElement(operand.getIndexOperands().get(0), varTable);
            return jasminString;
        }

        else if(element instanceof Operand){
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()) {
                case THIS:
                    return "\taload_0\n";
                case INT32:
                case BOOLEAN:
                    return String.format("\tiload %s\n", varTable.get(operand.getName()).getVirtualReg());
                case ARRAYREF:
                    return String.format("\taload %s\n", varTable.get(operand.getName()).getVirtualReg());
                case OBJECTREF:
                    return String.format("\taload %s\n", varTable.get(operand.getName()).getVirtualReg());
                //case CLASS:
                    //return String.format("\taload %s\n", varTable.get(operand.getName()).getVirtualReg());
                default:
                    break;
            }
        }
        return "";
    }

    public String invokeToJasmin(CallInstruction instruction, HashMap<String, Descriptor> varTable){
        String jasminString = "";

        Element first = instruction.getFirstArg();

        jasminString += loadElement((Operand) first, varTable);
        for (Element e: instruction.getListOfOperands()) {
            jasminString += loadElement((Operand)e, varTable);
        }

        if (first.toString().equals("this")) {
            Element second = instruction.getSecondArg();

            if (second.isLiteral()) {
                jasminString += ((LiteralElement) second).getLiteral().replace("\"", "");
            }
            else {
                Operand op = (Operand) second;
                jasminString += op.getName();
            }
        }
        else {
            jasminString += "\t" + instruction.getInvocationType() + " ";

            Element second = instruction.getSecondArg();
            if(second.isLiteral()) {
                if (this.method.isConstructMethod()) {
                    jasminString+="java/lang/Object" + "." + ((LiteralElement) second).getLiteral().replace("\"", "");
                }
                else {
                    if (first.getType().getTypeOfElement().equals(ElementType.CLASS)) {
                        jasminString+=((Operand) first).getName() + "." + ((LiteralElement) second).getLiteral().replace("\"", "");
                    }
                    else { // OBJECTREF
                        jasminString+= classUnit.getClassName() + "." + ((LiteralElement) second).getLiteral().replace("\"", "");
                    }
                }
            }
            else{
                Operand op = (Operand) second;
                jasminString += op.getName();
            }
        }


        jasminString+="(";
        for (Element e: instruction.getListOfOperands()) {
            jasminString += convertElementType(e.getType());
        }
        jasminString+=")";
        jasminString += convertElementType(instruction.getReturnType());
        
        return jasminString + "\n";
    }

    private String fieldsToJasmin(){
        String jasmiString = "";
        for(Field field : classUnit.getFields())
        {
            jasmiString += ".field "+ jasminIsStatic(field.isStaticField()) + " " + field.getFieldName() + " "+convertElementType(field.getFieldType())+"\n";
        }
        return jasmiString;
    }

    public String getStack(){
        return String.valueOf(this.maxstack);
    }

    public int maxStack(){
        if(maxstack < stackCount) maxstack=stackCount;
        return maxstack;
    }
}
