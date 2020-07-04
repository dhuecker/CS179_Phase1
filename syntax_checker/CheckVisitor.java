package syntax_checker;
import visitor.GJNoArguVisitor;

import java.text.Normalizer;
import java.util.Enumeration;
import syntaxtree.*;

import javax.swing.plaf.BorderUIResource;

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

    public boolean distinct(NodeOptional no) {
        if (!no.present()) {
            return true;
        }
        //continue here
        FormalParameterList param = (FormalParameterList) no.node;
        int s = param.f1.size();
        if (s == 0) //one param
            return true;
        else {
            FormalParameter p_one;
            FormalParameter p_two;
            for (int a = -1; a < s; a++) {
                for (int b = -1; b < s; b++) {
                    if (a == -1)
                        p_one = param.f0;
                    else
                        p_one = ((FormalParameterRest) param.f1.elementAt(a)).f1;
                    if (b == -1)
                        p_two = param.f0;
                    else
                        p_two = ((FormalParameterRest) param.f1.elementAt(b)).f1;

                    if (p_one.f1.f0.toString().equals(p_two.f1.f0.toString()) && a != b) {
                        return false;
                    }
                }
            }

        }
            return true;
    }

    public NodeChoice findClass(String classname){
        for(int a = 0; a< root.f1.size(); a++){
            TypeDeclaration typeD = (TypeDeclaration) root.f1.elementAt(a);

            String currentName;
            if(typeD.f0.which == 0){
                currentName = classname((ClassDeclaration)typeD.f0.choice);
            }
            else{
                currentName = classname((ClassExtendsDeclaration)typeD.f0.choice);
            }

            if(classname.equals(currentName)){
                return typeD.f0;
            }
        }

        return null; //error when reachs here
    }

    public NodeListOptional fields(ClassDeclaration x){
         return x.f3;
    }

    public NodeListOptional fields(ClassExtendsDeclaration x){

        NodeChoice superC = findClass(x.f3.f0.toString());
        NodeListOptional superF;

        if(superC.which == 0){
            superF  = (NodeListOptional)fields((ClassDeclaration)superC.choice);
        }
        else{
            superF = (NodeListOptional)fields((ClassExtendsDeclaration)superC.choice);
        }

        NodeListOptional typeE = new NodeListOptional();
        //Add class elements to list
        for(int a = 0; a < x.f5.size(); a++){
            typeE.addNode(x.f5.elementAt(a));
        }

        //add superclass elements to list
        for(int b = 0; b < superF.size(); b++){
            typeE.addNode(superF.elementAt(b));
        }
        return typeE;
    }

    public MethodType methodType(String id, String id_m){
        NodeChoice targetC = findClass(id);

        if(targetC.which == 0){
            //regular class
            ClassDeclaration x = (ClassDeclaration)targetC.choice;
            for(int a =0; a< x.f4.size(); a++){
                MethodDeclaration current = (MethodDeclaration)x.f4.elementAt(a);
                if(methodname(current).equals(id_m)){
                    return new MethodType(current.f1, current.f4);
                }
            }
        }
        else{
            //extend class
            ClassExtendsDeclaration x = (ClassExtendsDeclaration)targetC.choice;

            for(int b = 0; b < x.f6.size(); b++){
                MethodDeclaration current = (MethodDeclaration)x.f6.elementAt(b);

                if(methodname(current).equals(id_m)){
                    return new MethodType(current.f1, current.f4);
                }
            }
            return methodType(x.f3.f0.toString(), id_m);
        }
        return null;
    }

    public boolean noOverloading(String x, String superx, String id){
        MethodType one = methodType(x, id);
        MethodType two = methodType(superx, id);
        if(methodType(superx, id) != null && one.equals(two)){
            return true;
        }
        return false;
    }

    public boolean isSubType(String target, String id){
        ClassBook current = (ClassBook) symbolTable.get(Symbol.symbol(id));
        if(current == null){
            return false;
        }
        if(current.parent != null && current.parent.equals(target)){
            return true;
        }

        while(current.parent != null){
            if(current.parent.equals(target)){
                return true;
            }
            current = ((ClassBook) symbolTable.get(Symbol.symbol(current.parent)));
        }
        return false;
    }

    //auto class Visitors below dont need to be overridden

    public R visit(NodeList x){
        R temp = null;
        int count = 0;
        for(Enumeration<Node> e = x.elements(); e.hasMoreElements();){
            e.nextElement().accept(this);
            count++;
        }
        return temp;
    }

    public R visit(NodeListOptional x){
        if(x.present()){
            R temp = null;
            int count = 0;
            for(Enumeration<Node> i  = x.elements(); i.hasMoreElements();){
                i.nextElement().accept(this);
                count++;
            }
            return temp;
        }
        else
            return null;
    }

    public R visit(NodeOptional x){
        if(x.present()){
            return x.node.accept(this);
        }
        else
            return null;
    }
    public R visit(NodeSequence x){
            R temp = null;
            int count = 0;
             for(Enumeration<Node> i = x.elements(); i.hasMoreElements();){
                 i.nextElement().accept(this);
                 count++;
             }
             return temp;
    }

    public R visit(NodeToken x){
        return null;
    }

    //our classes below

    //BracketExpression vist
    //f0 -> (
    //f1 -> Expression()
    //f2 -> )

    public R visit(BracketExpression x){
        R temp = null;
        x.f0.accept(this);
        temp = x.f1.accept(this);
        x.f2.accept(this);
        return temp;
    }
}
