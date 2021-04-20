import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class MySymbol extends Symbol {
    private boolean init = false;
    private String scope;
    private String name;
    private Map<String, ArrayList<MySymbol>> attributes = new HashMap<String, ArrayList<MySymbol>>();
    
    public MySymbol(Type type, String nodeName, String name, String scope){
        super(type, nodeName);
        this.scope = scope;
        this.name = name;
        
    }

    public void addAttribute(String key, MySymbol value)
    {
        if(attributes.containsKey(key))
        {
            this.attributes.get(key).add(value);
        }
        else
        {
            ArrayList<MySymbol> toAdd = new ArrayList<MySymbol>();
            toAdd.add(value);       
            this.attributes.put(key, toAdd);
        }
    }

    public boolean isImport() { return true; }
    public boolean isClass() { return true; }
    public boolean isField() { return true; }
    public boolean isMethod() { return true; }
    public boolean isParameter() { return true; }
    public boolean isVariable() { return true; }

    public void setInit() { init = true; }
    public boolean getInit() { return init; }

    public String getScope(){
        return this.scope;
    }

    @Override
    public String toString() {
        String temp = "";
        for (Map.Entry<String, ArrayList<MySymbol>> entry : attributes.entrySet()) {
            String attribute = entry.getKey();
            ArrayList<MySymbol> symbols = entry.getValue();

            temp += " " + attribute + ": ";
            for (int i = 0; i < symbols.size(); i++)
            {
                temp += symbols.get(i).getType().getName() + " " + symbols.get(i).getName() + "; ";
            }
        }
        return "Symbol [type=" + super.getType() + ", name=" + super.getName() + "]" + " Name: " + this.name + " Scope: " + this.scope + " " + temp;
    }

}
