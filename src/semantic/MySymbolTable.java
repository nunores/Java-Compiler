import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import pt.up.fe.comp.jmm.JmmNode;


public class MySymbolTable implements SymbolTable {

    Map<JmmNode, MySymbol> table = new LinkedHashMap();
    
    public MySymbolTable() {}

    public void add(JmmNode node, MySymbol symbol){
        // Check repeated vars, symbols, correct operation types, ...
        
        table.put(node, symbol);
    }

    public Map<JmmNode, MySymbol> getTable(){
        return this.table;
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

    @Override
    public String toString() {
        return super.toString();
    }

    public void print(){
        for (Map.Entry<JmmNode, MySymbol> entry : table.entrySet()) {
            JmmNode node = entry.getKey();
            MySymbol symbol = entry.getValue();

            System.out.println(symbol);
        }
    }



}