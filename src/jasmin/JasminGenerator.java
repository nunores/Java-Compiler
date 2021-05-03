import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.*;
import java.util.*;

//import javax.swing.text.AbstractDocument.ElementEdit;


public class JasminGenerator {
    private ClassUnit classUnit;
    
    public JasminGenerator(ClassUnit classUnit){
        this.classUnit = classUnit;
    }

    public String classToJasmin(){
        String jasminString = ".class " + "public " + classUnit.getClassName() + "\n.super java/lang/Object\n";
        return jasminString + methodToJasmin();
    }

    public String methodToJasmin(){
        String jasminString = "";
        for (Method method: classUnit.getMethods()){
            jasminString += "\n.method public ";
            if (method.isConstructMethod()){
                jasminString += "<init>()V\n\taload_0\n\tinvokespecial java/lang/Object/<init>()V\n\treturn \n";
            }
            else if(method.isStaticMethod()){
                jasminString += "static main([Ljava/lang/String;)V\n\t.limit stack 99\n\t.limit locals 99\n";
            }
            else{
                jasminString += method.getMethodName() + "(";
                for (Element element: method.getParams()){
                    jasminString += convertElementType(element.getType().getTypeOfElement());
                }
                jasminString += ")" + this.convertElementType(method.getReturnType().getTypeOfElement());
                jasminString += "\n\t.limit stack 99\n\t.limit locals 99\n";

                HashMap<String, Descriptor> varTable = method.getVarTable();

                for (Instruction instruction: method.getInstructions()){
                        jasminString += instructToJasmin(instruction, varTable, method.getLabels());
                } 
            }

            jasminString += ".end method\n";
        }
        return jasminString;
        
    }

    public String convertElementType(ElementType type){
        switch (type){
            case INT32:
                return "I";
            case BOOLEAN:
                return "Z";
            case ARRAYREF:
                return "{I";
            case OBJECTREF:
                return "OBJ";
            case VOID:
                return "V";
            default:
                return "ups - conversao element type"; 
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
                    return jasminString += "\t" +invokeToJasmin(instruction2, varTable);//"\tinvokevirtual " + classUnit.getClassName() + "\n";
                case invokespecial:
                    return jasminString += "\tinvokespecial " +classUnit.getClassName() +"/" + invokeToJasmin(instruction2, varTable);
                case invokestatic:
                    return jasminString += invokeToJasmin(instruction2, varTable);//"\tinvokestatic " + "\n";
                case NEW:
                case arraylength:
                default: 
            }
        }
    return "";
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
            jasminString+=convertElementType(e.getType().getTypeOfElement());
        }
        jasminString+=")";

        jasminString+=convertElementType(instruction.getReturnType().getTypeOfElement());
        return jasminString +"\n" ;
    }
}
