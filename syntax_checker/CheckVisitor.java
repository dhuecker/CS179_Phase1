package syntax_checker;
import visitor.GJNoArguVisitor;
import java.util.Enumeration;
import syntaxtree.*;

public class CheckVisitor<R> implements GJNoArguVisitor {
    public static String ArrayTypeStr = "ARRAY_TYPE";
    public static String BoolTypeStr = "BOOL_TYPE";
    public static String IntTypeStr = "INT_TYPE";

    public Goal root;
    public SymbolTable symbolTable;
    ClassBook currentClass;
    MethodsBook currentMethod;

    public boolean errorFound = false;

    public void RegTypeError(){
        errorFound = true;
    }

    //helper functions for minijava go below
    public String idName(Identifier id){
        return id.f0.toString();
    }

    public String classname(ClassDeclaration c){
        return c.f1.f0.toString();
    }

    public String classname(ClassExtendsDeclaration c){
        return c.f1.f0.toString();
    }

    public String methodname(MethodDeclaration m){
        return m.f2.f0.toString();
    }

    public boolean distinct(NodeOptional no){
        if(!no.present()){
            return true;
        }
        //continue here
    }


}
