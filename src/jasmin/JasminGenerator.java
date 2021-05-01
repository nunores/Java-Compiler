package jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;

public class JasminGenerator {
    private ClassUnit classUnit;
    
    public JasminGenerator(ClassUnit classUnit){
        this.classUnit = classUnit;
    }

    public String classToJasmin(){
        String jasminString = "JASMIN: \n\n" ;
        String stringBuilder = ".class " + classUnit.getClassName() + "\n.super java/lang/Object\n";
        return jasminString + stringBuilder + methodToJasmin();
    }

    public String methodToJasmin(){
        String stringBuilder = "";
        for (Method method: classUnit.getMethods()){
            stringBuilder += "\n.method public ";
            if (method.isConstructMethod()){
                stringBuilder += "<init>()V\naload_0\ninvokespecial java/lang/Object.<init>()V\nreturn";
            }
            if(method.isStaticMethod()){
                stringBuilder += "static main([Ljava/lang/String;)V";
            }
            else{
                stringBuilder += method.getMethodName(); 
            }

            stringBuilder += "\n.end method\n";
        }
        return stringBuilder;
        
    }
}
