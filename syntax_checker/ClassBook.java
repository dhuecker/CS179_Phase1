package syntax_checker;

public class ClassBook extends Book {

    String classname;
    String parent;
    SymbolTable functions;

    public ClassBook(String name){
        classname = name;
        functions = new SymbolTable();
    }
}
