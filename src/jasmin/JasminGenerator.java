package jasmin;

import org.specs.comp.ollir.ClassUnit;

public class JasminGenerator {
    private ClassUnit classUnit;
    
    public JasminGenerator(ClassUnit classUnit){
        this.classUnit = classUnit;
    }

    public String classToJasmin(){
        String jasminString = "JASMIN: \n\n" ;
        String stringBuilder = ".class " + classUnit.getClassName() + "\n.super java/lang/Object\n";
        return jasminString + stringBuilder;
    }
}
