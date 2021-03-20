/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 6.1 */
/* JavaCCOptions:MULTI=false,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
import pt.up.fe.comp.jmm.JmmNode;

import java.lang.RuntimeException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;


public
class SimpleNode implements Node, JmmNode {

  protected HashMap<String, String> attributes = new HashMap<>();

  protected Node parent;
  protected Node[] children;
  protected int id;
  protected Object value;
  protected Parser parser;

    // added
    public int val;
    public Operator op = null;
    public String packageName;

  public SimpleNode(int i) {
    id = i;
  }

  public SimpleNode(Parser p, int i) {
    this(i);
    parser = p;
  }


  public String getKind() {
	  return toString();
  }
  
  public List<String> getAttributes() {	return new ArrayList<>(this.attributes.keySet()); }

  public void put(String attribute, String value) {	this.attributes.put(attribute, value); }

  public String get(String attribute) {	return this.attributes.get(attribute); }
  
  public List<JmmNode> getChildren() {
    return JmmNode.convertChildren(children);
  }
  
  public int getNumChildren() {
    return jjtGetNumChildren();
  }
  
  public void add(JmmNode child, int index) {
    if(!(child instanceof Node)) {
  	throw new RuntimeException("Node not supported: " + child.getClass());  
    }
	  
	jjtAddChild((Node) child, index);
  }


  public void jjtOpen() {
  }

  public void jjtClose() {
  }

  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  public void jjtSetValue(Object value) { this.value = value; }
  public Object jjtGetValue() { return value; }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() {
    return ParserTreeConstants.jjtNodeName[id];
  }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix) {

    if (this.attributes.size() != 0) {
      System.out.print(toString(prefix) + " [ ");
      for (String key : this.attributes.keySet()) {
        System.out.print(key + ": " + this.attributes.get(key) + "; ");
      } 
      System.out.println("]");
    }
    
    else {
      System.out.println(toString(prefix));
    }
    
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode)children[i];
        if (n != null) {
          n.dump(prefix + "    ");
        }
      }
    }
  }

  public int getId() {
    return id;
  }
}

/* JavaCC - OriginalChecksum=d33fdb2b8063d5de3474649324d5d160 (do not edit this line) */
