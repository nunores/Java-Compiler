import java.util.List;
import java.util.stream.Collectors;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.specs.util.utilities.StringLines;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;


class SymbolTableGenerator extends PreorderJmmVisitor<String, SymbolTable> {

    public SymbolTableGenerator() {

        setDefaultVisit(this::defaultVisit);

    }

    private SymbolTable defaultVisit(JmmNode node, String space) {
/*         String content = space + node.getKind();
        String attrs = node.getAttributes()
                .stream()
                .filter(a -> !a.equals("line"))
                .map(a -> a + "=" + node.get(a))
                .collect(Collectors.joining(", ", "[", "]"));

        content += ((attrs.length() > 2) ? attrs : "");

        return content;  */
        return new MySymbolTable();
    }


}