import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;


public class MySymbol extends Symbol {
    
    private String scope;
    
    public MySymbol(Type type, String name, String scope){
        super(type, name);
        this.scope = scope;
    }
}
