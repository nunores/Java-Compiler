import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import java.util.ArrayList;
import java.util.List;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import java.util.HashMap;
import java.util.Map;
import pt.up.fe.comp.jmm.JmmNode;


class MySymbolTable implements SymbolTable {

    HashMap<JmmNode, MySymbol> table = new HashMap<JmmNode, MySymbol>(); 
    
    public MySymbolTable(){}

    public void add(JmmNode node, MySymbol symbol){
        table.put(node, symbol);
    }

    

    @Override
    public List<String> getImports() {
        return new ArrayList<String>();
    }
    
    @Override
    public String getClassName(){
        return new String();
    }
    
    @Override
    public String getSuper(){
        return new String();
    }

    @Override
    public List<Symbol> getFields(){
        return new ArrayList<Symbol>();
    }
    
    @Override
    public List<String> getMethods(){
        return new ArrayList<String>();
    }
    
    @Override
    public Type getReturnType(String methodName){
        return new Type(new String(), true);
    }

    @Override
    public List<Symbol> getParameters(String methodName){
        return new ArrayList<Symbol>();
    }
    
    @Override
    public List<Symbol> getLocalVariables(String methodName){
        return new ArrayList<Symbol>();
    }

    public void print(){
        for (Map.Entry<JmmNode, MySymbol> entry : table.entrySet()) {
            JmmNode node = entry.getKey();
            MySymbol symbol = entry.getValue();

            System.out.println("SYMBOL: " + symbol + "\t NODE: " + node);
        }
    }
}