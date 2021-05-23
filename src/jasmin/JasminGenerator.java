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
    int stackCount = -1;
    
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
        if (isStatic) return "static";
        else return "";
    }

    public String methodToJasmin() {
        String jasminString = "";
        for (Method method: classUnit.getMethods()) {
            this.method = method;
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
                    jasminIsStatic(method.isStaticMethod()) + " " + "<init>(";
                for (Element element: method.getParams()) {
                    jasminString += convertElementType(element.getType());
                } 
                jasminString += ")" + convertElementType(method.getReturnType()) + "\n";
            
            }
            else {
                jasminString += jasminAccessModifier(method.getMethodAccessModifier()) + " " +
                    jasminIsStatic(method.isStaticMethod()) + " " + method.getMethodName()+"(";
                for (Element element: method.getParams()) {
                    jasminString += convertElementType(element.getType());
                } 
                jasminString += ")" + convertElementType(method.getReturnType()) + "\n";
                jasminString += "\t.limit stack " + getStack() + "\n"; // TODO: calculate it
                jasminString += "\t.limit locals " + getLocals(method.getVarTable()) + "\n"; // TODO: calculate it
            }

            jasminString += instructionToJasmin(method);
            jasminString += ".end method\n";
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
            switch (instruction2.getInvocationType()) {
                case invokevirtual:
                    return jasminString + invokeToJasmin(instruction2, varTable);
                case invokespecial:
                    return jasminString + invokeToJasmin(instruction2, varTable);
                case invokestatic:
                    return  invokeToJasmin(instruction2, varTable);
                case NEW:
                    Operand op = (Operand)instruction2.getFirstArg();
                    jasminString += "\tnew " + op.getName() + "\n";
                    return jasminString;
                case arraylength:
                    
                default:
            }
        }

        if (instruction instanceof GotoInstruction) {
            return "goto";
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
                return String.format("\tistore %s\n", varTable.get(operand.getName()).getVirtualReg());
            case ARRAYREF: case OBJECTREF:
                return String.format("\tastore %s\n", varTable.get(operand.getName()).getVirtualReg());
            default:
                return "";
        }
    }

    public String loadElement(Element element, HashMap<String, Descriptor> varTable){
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
            return jasminString + "\tiaload\n";
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
                    jasminString+="java/lang/Object" + "/" + ((LiteralElement) second).getLiteral().replace("\"", "");
                }
                else {
                    if (first.getType().getTypeOfElement().equals(ElementType.CLASS)) {
                        jasminString+=((Operand) first).getName() + "/" + ((LiteralElement) second).getLiteral().replace("\"", "");
                    }
                    else { // OBJECTREF
                        jasminString+= classUnit.getClassName() + "/" + ((LiteralElement) second).getLiteral().replace("\"", "");
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

    public String getLocals(HashMap<String, Descriptor> varTable){
        int local = 0;
        for(Map.Entry<String, Descriptor> entry : varTable.entrySet()){
            Descriptor d1 = entry.getValue();
            if(d1.getScope().toString() == "LOCAL") local +=1;
        }
        return String.valueOf(local);
    }

    public String getStack(){
        return String.valueOf(countStack());
    }

    public int countStack(){
        stackCount +=1;
        return stackCount;
    }
}
