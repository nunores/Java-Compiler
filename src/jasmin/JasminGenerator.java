package jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.ElementType;
import org.specs.comp.ollir.Instruction;
import org.specs.comp.ollir.*;
import java.util.*;

import javax.swing.text.AbstractDocument.ElementEdit;;


public class JasminGenerator {
    private ClassUnit classUnit;
    
    public JasminGenerator(ClassUnit classUnit){
        this.classUnit = classUnit;
    }

    public String classToJasmin(){
        String jasminString = ";JASMIN: \n\n" ;
        jasminString += ".class " + classUnit.getClassName() + "\n.super java/lang/Object\n";
        return jasminString + methodToJasmin();
    }

    public String methodToJasmin(){
        String jasminString = "";
        for (Method method: classUnit.getMethods()){
            jasminString += "\n.method public ";
            if (method.isConstructMethod()){
                jasminString += "<init>()V\naload_0\ninvokespecial java/lang/Object.<init>()V\nreturn ";
            }
            if(method.isStaticMethod()){
                jasminString += "static main([Ljava/lang/String;)V";
            }
            else{
                jasminString += method.getMethodName() + "(";
                for (Element element: method.getParams()){
                    jasminString += convertElementType(element.getType().getTypeOfElement());
                }
                jasminString += ")" + this.convertElementType(method.getReturnType().getTypeOfElement());

                HashMap<String, Descriptor> varTable = method.getVarTable();

                for (Instruction instruction: method.getInstructions()){
                        jasminString += instructToJasmin(instruction, varTable, method.getLabels());
                } 
            }

            jasminString += "\n.end method\n";
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
            jasminString = instructToJasmin(instruction2.getRhs(), varTable, new HashMap<String, Instruction>());
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
                    jasminString += left + right + "iadd\n";
                case SUB:
                    jasminString += left + right + "isub\n";
                case MUL:
                    jasminString += left + right + "imul\n";
                case DIV:
                    jasminString += left + right + "idiv\n";
                default:
                    jasminString += "default - operations\n";
            }
        }
        return jasminString;
    }

    public String storeElement(Operand operand, HashMap<String, Descriptor> varTable){
        String jasminString = "";
        switch(operand.getType().getTypeOfElement()){
            case INT32:
            case BOOLEAN:
                jasminString += String.format("istore%s\n", operand.getName());
            case ARRAYREF:
                jasminString += String.format("astore%s\n", operand.getName());
            case OBJECTREF:
                jasminString += String.format("astore%s\n", operand.getName());
            default:
                jasminString += "";
                break;
        }
        return jasminString;
    }

    public String loadElement(Element element, HashMap<String, Descriptor> varTable){
        String jasminString = "";
        if(element instanceof LiteralElement){
            String num = ((LiteralElement) element).getLiteral();
            if(Integer.parseInt(num) <= 5){
                return "iconst" + num + "\n";
            }

            else{
                return "bipush" + num + "\n";
            }
        }
        else if(element instanceof ArrayOperand){
            ArrayOperand operand = (ArrayOperand) element;

            jasminString = String.format("aload%s\n", operand.getName());
            jasminString += loadElement(operand.getIndexOperands().get(0), varTable);

            return jasminString + "iaload\n";
        }

        else if(element instanceof Operand){
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()){
                case THIS:
                    jasminString += "aload_0\n";
                case INT32:
                    jasminString += "";
                case BOOLEAN:
                    jasminString += String.format("iload%s\n", operand.getName());
                case ARRAYREF:
                    jasminString += String.format("aload%s\n", operand.getName());
                case OBJECTREF:
                    jasminString += "";
                default:
                    break;
            }
        }
        return jasminString;
    }
}