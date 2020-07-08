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
            Graph classGraph = Graph();
            List<String> classes = symbolTable.hashtable.getAllElements();
            for(int a =0; a < classes.size(); a++){
                ClassBook cbook = (ClassBook) symbolTable.get(Symbol.symbol(classes.get(a)));
                ClassGraph.addEdge(cbook.parent, classes.get(a));
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
}