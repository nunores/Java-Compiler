import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.*;
import java.util.*;


public class JasminGenerator {
    private ClassUnit classUnit;
    
    public JasminGenerator(ClassUnit classUnit){
        this.classUnit = classUnit;
    }
    private String jasminAccessModifier(AccessModifiers accessModifiers){
        if(accessModifiers.name().equals("PRIVATE")) return "private";
        else return "public";
    }

    private String jasminIsStatic(boolean isStatic){
        if(isStatic) return "static";
        else return "";
    }
    public String classToJasmin(){
        String jasminString = ".class " + jasminAccessModifier(classUnit.getClassAccessModifier())+" " + classUnit.getClassName() + "\n.super " +((classUnit.getSuperClass() == null || classUnit.getSuperClass().equals(classUnit.getClassName())) ? "java/lang/Object" : classUnit.getSuperClass())+ "\n";
        return jasminString + methodToJasmin();
    }

    public String methodToJasmin(){
        String jasminString = "";
        for (Method method: classUnit.getMethods()){
            jasminString += "\n.method ";
            if (method.isConstructMethod()){
                jasminString += jasminAccessModifier(method.getMethodAccessModifier()) +
                jasminIsStatic(method.isStaticMethod()) + " "+
                "<init>"+"(";
                for (Element element: method.getParams()){
                    jasminString += convertElementType(element.getType());
                } 
                jasminString += ")"+convertElementType(method.getReturnType())+"\n";
            
            }
            else{
                jasminString += jasminAccessModifier(method.getMethodAccessModifier()) +" "+
                jasminIsStatic(method.isStaticMethod()) + " "+
                method.getMethodName()+"(";
                for (Element element: method.getParams()){
                    jasminString += convertElementType(element.getType());
                } 
                jasminString += ")"+convertElementType(method.getReturnType())+"\n";
            }

            // jasminString += instructionToJasmin(method.getInstructions(), method);
            jasminString += ".end method\n";
        }
        return jasminString;
        
    }

    private String ArrayTypeToJasmin(ElementType elementType){
        switch (elementType.name()){
            case "STRING": 
                return "[Ljava/lang/String;";   
            case "INT32": 
                return "[I"; 
            case "BOOLEAN": 
                return "[B"; 
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
                return ArrayTypeToJasmin(type.getTypeOfElement());
            case OBJECTREF:
                return "OBJ";
            case CLASS:
                return "C";
            case THIS:
                return "T";
            case STRING:
                return "S";
            case VOID:
                return "V";
            default:
                return "V"; 
        }
    }
    public String instructToJasmin(Instruction instruction, HashMap<String, Descriptor> varTable, HashMap<String, Instruction> methodLabels){
        String jasminString = "";
        for(Map.Entry<String, Instruction> entry: methodLabels.entrySet()){
            if(entry.getValue().equals(instruction)){
                jasminString += entry.getKey() + ":\n";
                break;
            }
        }

        if (instruction instanceof AssignInstruction){
            AssignInstruction instruction2 = (AssignInstruction)instruction;
            jasminString += instructToJasmin(instruction2.getRhs(), varTable, new HashMap<String, Instruction>());
            Operand operand = (Operand) instruction2.getDest();
    
            if(!operand.getType().getTypeOfElement().equals(ElementType.OBJECTREF)){
                jasminString += this.storeElement(operand, varTable);
            }
            return jasminString;
        }
        if(instruction instanceof SingleOpInstruction){
            SingleOpInstruction instruction2 = (SingleOpInstruction)instruction;
            jasminString += loadElement(instruction2.getSingleOperand(), varTable);
        }

        if (instruction instanceof BinaryOpInstruction){
            BinaryOpInstruction instruction2 = (BinaryOpInstruction)instruction;
            String left = loadElement(instruction2.getLeftOperand(), varTable);
            String right = loadElement(instruction2.getRightOperand(), varTable);
            OperationType operationType = instruction2.getUnaryOperation().getOpType();
            switch(operationType){
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
            if(!instruction2.hasReturnValue()) jasminString +="\treturn\n";
            switch(instruction2.getOperand().getType().getTypeOfElement()){
                case VOID:

                case INT32:
                    return "\tireturn\n";

                case BOOLEAN:
                    jasminString += loadElement(instruction2.getOperand(), varTable);
                    jasminString += "\tireturn\n";

                case ARRAYREF:

                case OBJECTREF:
                    jasminString += loadElement(instruction2.getOperand(), varTable);
                    jasminString += "\tareturn\n";

                default:
                    break;
            }
        }

        if(instruction instanceof CallInstruction){
            CallInstruction instruction2 = (CallInstruction) instruction;
            switch(instruction2.getInvocationType()){
                case invokevirtual:
                    return jasminString += "\taload_0\n\t" +invokeToJasmin(instruction2, varTable);//"\tinvokevirtual " + classUnit.getClassName() + "\n";
                case invokespecial:
                    return jasminString += "\tinvokespecial " +classUnit.getClassName() +"/" + invokeToJasmin(instruction2, varTable);
                case invokestatic:
                    return jasminString += invokeToJasmin(instruction2, varTable);//"\tinvokestatic " + "\n";
                case NEW:
                case arraylength:
                default: 
            }
        }

        if(instruction instanceof GotoInstruction){
            return "goto";
        }


    return "";
}

    public String instructionToJasmin(ArrayList<Instruction> instructions , Method method){
        HashMap<String, Descriptor> varTable = method.getVarTable();
        HashMap<String, Instruction> labels = method.getLabels();
        String body = "";
        body += "\t.limit stack 99\n";
        body += "\t.limit locals 99\n";
        for(Instruction instruction : instructions){
            body += "\t"+ instructToJasmin(instruction, varTable, labels) + "\n";
        }

        if(method.getReturnType().getTypeOfElement() == ElementType.VOID){
            body += "\treturn\n";
        }
        return body;
    }

    public String storeElement(Operand operand, HashMap<String, Descriptor> varTable){
        switch(operand.getType().getTypeOfElement()){
            case INT32:
            case BOOLEAN:
                return String.format("\tistore %s\n", varTable.get(operand.getName()).getVirtualReg());
            case ARRAYREF:
                return String.format("\tastore %s\n", varTable.get(operand.getName()).getVirtualReg());
            case OBJECTREF:
                return String.format("\tastore %s\n", varTable.get(operand.getName()).getVirtualReg());
            default:
                break;
        }
        return "nhe1";
    }

    public String loadElement(Element element, HashMap<String, Descriptor> varTable){
        String jasminString = "";
        if(element instanceof LiteralElement){
            String num = ((LiteralElement) element).getLiteral();
            if(Integer.parseInt(num) <= 5){
                return "\ticonst_" + num + "\n";
            }

            else{
                return "\tbipush " + num + "\n";
            }
        }
        else if(element instanceof ArrayOperand){
            ArrayOperand operand = (ArrayOperand) element;

            jasminString += String.format("\taload %s\n", varTable.get(operand.getName()).getVirtualReg());
            jasminString += loadElement(operand.getIndexOperands().get(0), varTable);

            return jasminString + "\tiaload\n";
        }

        else if(element instanceof Operand){
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()){
                case THIS:
                    return "\taload_0\n";
                case INT32:
                case BOOLEAN:
                    return String.format("\tiload %s\n", varTable.get(operand.getName()).getVirtualReg());
                case ARRAYREF:
                    return String.format("\taload %s\n", varTable.get(operand.getName()).getVirtualReg());
                case OBJECTREF:
                default:
                    break;
            }
        }
        return "nhe";
    }
    public String invokeToJasmin(CallInstruction instruction, HashMap<String, Descriptor> varTable){
        String jasminString = "";
        Element first = instruction.getFirstArg();

        if(first.toString().equals("this")){
            jasminString +=loadElement((Operand) first, varTable);
            Element second = instruction.getSecondArg();
            if(second.isLiteral()){
                jasminString += ((LiteralElement) second).getLiteral().replace("\"", "");           
            }
            else{
                Operand op = (Operand) second;
                jasminString += op.getName();
            }
        }
        else{
            jasminString+=instruction.getInvocationType()+" ";

            if(first.isLiteral()){
                jasminString+="\n"+ ((LiteralElement) first).getLiteral().replace("\"", "");
            }
            else{
                Operand op = (Operand) first;
                //jasminString += op.getName();
            }

            Element second = instruction.getSecondArg();
            if(second.isLiteral()){
                jasminString+=classUnit.getClassName() + "/" + ((LiteralElement) second).getLiteral().replace("\"", "");
            }
            else{
                Operand op = (Operand) second;
                jasminString += op.getName();
            }
        }

        jasminString+="(";

        for(Element e: instruction.getListOfOperands()){
            jasminString+=convertElementType(e.getType());
        }
        jasminString+=")";

        jasminString+=convertElementType(instruction.getReturnType());
        return jasminString +"\n" ;
    }

    private String jasminFields(){
        String jasmiString = "";
        for(Field field : classUnit.getFields())
        {
            jasmiString += ".field "+jasminIsStatic(field.isStaticField())+" "+field.getFieldName() + " "+convertElementType(field.getFieldType())+"\n";
        }
        return jasmiString;
    }
}
