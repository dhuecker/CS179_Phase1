package syntax_checker;
import syntaxtree.*;
import parser.*;
import java.util.List;
import java.util.ArrayList;

public class Typecheck {

    public static MiniJavaParser parser =  new MiniJavaParser(System.in);
    public static SymbolTable symbolTable;

    public static void typeChecking() {
        //setup everything below
        symbolTable = new SymbolTable();
        SymbolTableConstructor oneVisitor = new SymbolTableConstructor();
        CheckVisitor<String> twoVisitor = new CheckVisitor<>();

        parser.ReInit(System.in);

        //setup now done to accept input program now try to typecheck it below
        try {
            Goal root = parser.Goal();

            oneVisitor.root = root;
            oneVisitor.sTable = symbolTable;
            //now construct the symbol table
            root.accept(oneVisitor);
            Graph classGraph = new Graph();
            List<String> classes = symbolTable.hashtable.getAllElements();
            for(int a =0; a < classes.size(); a++){
                ClassBook cbook = (ClassBook) symbolTable.get(Symbol.symbol(classes.get(a)));
                classGraph.addEdges(cbook.parent, classes.get(a));
            }

            //check if the graph is acyclic
            if(!classGraph.acyclic()){
                oneVisitor.errorFound = true;
            }
            else{
                //set up the 2nd visitor now
                twoVisitor.root = root;
                twoVisitor.symbolTable = symbolTable;
                root.accept(twoVisitor);
            }
        }
        catch (Exception x){
            System.out.println("error:" + x );
            x.printStackTrace();
        }

        //now time to output results of input program to the user
        if(!oneVisitor.errorFound && !twoVisitor.errorFound){
            System.out.println("Program type checked successfully");
        }
        else{
            System.out.println("Type error");
        }
    }

    public static void main(String args[]){
        typeChecking(); // now type check inside of main
    }

}

//methods used in typechecking class defined below
class GraphNode {
    public boolean beenHere;
    public GraphNode another;
    public String value;

    public GraphNode (String value){
        this.value = value;
        beenHere = false;
    }

    public void print() {
        if(another != null){
            System.out.println(value + "-->" + another.value);
        }
        else{
            System.out.println(value);
        }
    }
}
class Graph {
    List<GraphNode> nodes;

    public Graph() {
        nodes = new ArrayList<>();
    }
    //contiue here

    //adding edges to graph
    public void addEdges(String key1, String key2){
        if(key1 != null){
            addNodes(key1).another= addNodes(key2);
        }
        else
            addNodes(key2);
    }

    GraphNode addNodes(String key){
        GraphNode x = find(key);
        if(x != null || key == null){
            return x;
        }
        GraphNode temp = new GraphNode(key);
        nodes.add(temp);
        return temp;
    }

    GraphNode find(String key){
        for(int a = 0; a < nodes.size(); a++){
            if(nodes.get(a).value.equals(key) && key != null){
                return nodes.get(a);
            }
        }
        return null;
    }
    public boolean acyclic(){
        GraphNode current = nodes.get(0); //first node
        while (current != null && !current.beenHere){
            if(current.another != null && current.another.beenHere){
                return false;
            }
            current.beenHere = true;
            current = current.another;
        }
        return true;
    }
    public void print(){
        GraphNode current = nodes.get(0);
        while(current != null && !current.beenHere){
            System.out.print(current.value+ "-->");
            current.beenHere = true;
            current = current.another;
        }
    }
}