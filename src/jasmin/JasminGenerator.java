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
                jasminString += ".limit stack 99\n";
                jasminString += ".limit locals 99\n";
            }

            jasminString += instructionToJasmin(method);
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
            return loadElement(instruction2.getSingleOperand(), varTable);
        }

        if (instruction instanceof BinaryOpInstruction){
            BinaryOpInstruction instruction2 = (BinaryOpInstruction)instruction;
            String left = loadElement(instruction2.getLeftOperand(), varTable);
            String right = loadElement(instruction2.getRightOperand(), varTable);
            OperationType operationType = instruction2.getUnaryOperation().getOpType();
            switch(operationType){
                case ADD:
                    return left + right + "iadd\n";
                case SUB:
                    return left + right + "isub\n";
                case MUL:
                    return left + right + "imul\n";
                case DIV:
                    return left + right + "idiv\n";
                default:
                    return "default - operations\n";
            }
        }

        if(instruction instanceof ReturnInstruction){
            ReturnInstruction instruction2 = (ReturnInstruction)instruction;
            if(!instruction2.hasReturnValue()) return "return\n";
            switch(instruction2.getOperand().getType().getTypeOfElement()){
                case VOID:
                    jasminString += "return\n";

                case INT32:
                    return "ireturn\n";

                case BOOLEAN:
                    jasminString += loadElement(instruction2.getOperand(), varTable);
                    jasminString += "ireturn\n";
                    return jasminString;

                case ARRAYREF:
                    jasminString += "return\n";

                case OBJECTREF:
                    jasminString += loadElement(instruction2.getOperand(), varTable);
                    jasminString += "areturn\n";
                    return jasminString;

                default:
                    jasminString += "return\n";
            }
            
        }

        if(instruction instanceof CallInstruction){
            CallInstruction instruction2 = (CallInstruction) instruction;
            switch(instruction2.getInvocationType()){
                case invokevirtual:
                    return  "aload_0\n" +invokeToJasmin(instruction2, varTable);
                case invokespecial:
                    return  "aload_0\n" + invokeToJasmin(instruction2, varTable);
                case invokestatic:
                    return  invokeToJasmin(instruction2, varTable);
                case NEW:
                case arraylength:
                default: 
            }
        }

        if(instruction instanceof GotoInstruction){
            return "goto";
        }


    return jasminString;
}

    public String instructionToJasmin(Method method){
        HashMap<String, Descriptor> varTable = method.getVarTable();
        HashMap<String, Instruction> labels = method.getLabels();
        String jasminString = "";
        for(Instruction instruction : method.getInstructions()){
            jasminString += ""+ instructToJasmin(instruction, varTable, labels);
        }

        return jasminString;
    }

    public String storeElement(Operand operand, HashMap<String, Descriptor> varTable){
        switch(operand.getType().getTypeOfElement()){
            case INT32:
            case BOOLEAN:
                return String.format("istore %s\n", varTable.get(operand.getName()).getVirtualReg());
            case ARRAYREF:
                return String.format("astore %s\n", varTable.get(operand.getName()).getVirtualReg());
            case OBJECTREF:
                return String.format("astore %s\n", varTable.get(operand.getName()).getVirtualReg());
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
                return "iconst_" + num + "\n";
            }

            else{
                return "bipush " + num + "\n";
            }
        }
        else if(element instanceof ArrayOperand){
            ArrayOperand operand = (ArrayOperand) element;

            jasminString += String.format("aload %s\n", varTable.get(operand.getName()).getVirtualReg());
            jasminString += loadElement(operand.getIndexOperands().get(0), varTable);

            return jasminString + "iaload\n";
        }

        else if(element instanceof Operand){
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()){
                case THIS:
                    return "aload_0\n";
                case INT32:
                case BOOLEAN:
                    return String.format("iload %s\n", varTable.get(operand.getName()).getVirtualReg());
                case ARRAYREF:
                    return String.format("aload %s\n", varTable.get(operand.getName()).getVirtualReg());
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

    private String fieldsToJasmin(){
        String jasmiString = "";
        for(Field field : classUnit.getFields())
        {
            jasmiString += ".field "+jasminIsStatic(field.isStaticField())+" "+field.getFieldName() + " "+convertElementType(field.getFieldType())+"\n";
        }
        return jasmiString;
    }
}
