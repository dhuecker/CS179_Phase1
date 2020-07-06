package syntax_checker;
import visitor.Visitor;
import java.util.*;
import syntaxtree.*;

public class SymbolTableConstructor implements Visitor {
    public Goal root;
    public SymbolTable sTable;

    ClassBook currentClass = null;
    MethodsBook currentMethod = null;

    public boolean errorFound = false;

    public void RegTypeError() {
        errorFound = true;
    }
    //Helper functions below

    public String classname(MainClass mc) {
        return mc.f1.f0.toString();
    }
    public String classname(ClassExtendsDeclaration ced){
        return ced.f1.f0.toString();
    }

    public String idName(Identifier id) {
        return id.f0.toString();
    }

    public String classname(ClassDeclaration c) {
        return c.f1.f0.toString();
    }

    public String methodname(MethodDeclaration md) {
        return md.f2.f0.toString();
    }

    public boolean distinct(NodeOptional no) {
        if (!no.present()) {
            return true;
        }
        FormalParameterList fpl = (FormalParameterList) no.node;
        int tempNum = fpl.f1.size(); // if f1 is empty than there's only one parameter
        if (tempNum == 0) {
            return true;
        } else {
            FormalParameter param_x;
            FormalParameter param_y;

            for (int a = -1; a < tempNum; a++) {
                for (int b = -1; b < tempNum; b++) {
                    if (a == -1) {
                        param_x = fpl.f0;
                    } else {
                        param_x = ((FormalParameterRest) fpl.f1.elementAt(a)).f1;
                    }
                    if (b == -1) {
                        param_y = fpl.f0;
                    } else {
                        param_y = ((FormalParameterRest) fpl.f1.elementAt(b)).f1;
                    }
                    if (param_x.f1.f0.toString().equals(param_y.f1.f0.toString()) && a != b) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public NodeChoice findClass(String classname) {
        for (int a = 0; a < root.f1.size(); a++) {
            TypeDeclaration tdec = (TypeDeclaration) root.f1.elementAt(a);
            String currentName;
            if (tdec.f0.which == 0) {
                currentName = classname((ClassDeclaration) tdec.f0.choice);
            } else {
                currentName = classname((ClassExtendsDeclaration) tdec.f0.choice);
            }
            if (classname.equals(currentName)) {
                return tdec.f0;
            }
        }
        //error if it get's this far look into fixing later
        return null;
    }

    public NodeListOptional fields(ClassDeclaration cd) {
        return cd.f3;
    }

    public NodeListOptional fields(ClassExtendsDeclaration c) {
        //Now finding superclass
        NodeChoice superclass = findClass(c.f3.f0.toString());
        NodeListOptional SCfields;
        if (superclass.which == 0) {
            SCfields = (NodeListOptional) fields((ClassDeclaration) superclass.choice);
        } else {
            SCfields = (NodeListOptional) fields((ClassExtendsDeclaration) superclass.choice);
        }
        //List contains typeEnv of
        NodeListOptional typeE = new NodeListOptional();
        //adding to list
        for (int a = 0; a < c.f5.size(); a++) {
            typeE.addNode(c.f5.elementAt(a));
        }
        for (int a = 0; a < SCfields.size(); a++) {
            typeE.addNode(SCfields.elementAt(a));
        }
        return typeE;
    }

    public MethodType methodtype(String id, String id_m) {
        NodeChoice targetC = findClass(id);
        if (targetC.which == 0) { //regular class
            ClassDeclaration cd = (ClassDeclaration) targetC.choice;
            for (int a = 0; a < cd.f4.size(); a++) {
                MethodDeclaration current = (MethodDeclaration) cd.f4.elementAt(a);
                if (methodname(current).equals(id_m)) {
                    return new MethodType(current.f1, current.f4);
                }
            }
        } else { //extend class
            ClassExtendsDeclaration ced = (ClassExtendsDeclaration) targetC.choice;
            for (int a = 0; a < ced.f6.size(); a++) {
                MethodDeclaration current = (MethodDeclaration) ced.f6.elementAt(a);
                if (methodname(current).equals(id_m)) {
                    return new MethodType(current.f1, current.f4);
                }
            }
            return methodtype(ced.f3.f0.toString(), id_m);
        }
        return null;
    }

    public boolean noOverloading(String c, String sc, String id_m) {
        MethodType x = methodtype(c, id_m);
        MethodType y = methodtype(sc, id_m);
        if (methodtype(sc, id_m) != null && x.equals(y)) {
            return true;
        }
        return false;
    }

    //auto visitor classes below that might not have to be redone

    public void visit(NodeList n) {
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); ) {
            e.nextElement().accept(this);
        }
    }

    public void visit(NodeListOptional n) {
        if (n.present())
            for (Enumeration<Node> e = n.elements(); e.hasMoreElements(); )
                e.nextElement().accept(this);
    }

    public void visit(NodeOptional n){
        if( n.present())
            n.node.accept(this);
    }

    public void visit(NodeSequence n){
        for (Enumeration<Node> e = n.elements(); e.hasMoreElements();)
            e.nextElement().accept(this);
    }

    public void visit(NodeToken n){ }

    //Our methods made for visitor are below

    //Goal
    //f0 -> MainClass
    //f1 -> ( TypeDeclaration())
    //f2 -> end

    public void visit(Goal x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    } // end of Goal

    //MainClass
    //f0 -> "class"
    //f1 -> Identifier()
    //f2 => {
    //f3 -> public
    //f4 -> static
    //f5 -> void
    //f6 -> main
    //f7 ->(
    //f8 -> String
    //f9 -> [
    //f10 -> ]
    //f11 -> Identifier()
    //f12 -> )
    //f13 -> {
    //f14 -> ( VarDecclaration() ) *
    //f15 -> ( Statement () )*
    //f16 -> }
    //f17 -> }

    public void visit(MainClass mc){
        ClassBook tempb = new ClassBook(classname(mc));
        currentClass = tempb; //need to fix this

        mc.f0.accept(this);
        mc.f1.accept(this);
        mc.f2.accept(this);
        mc.f3.accept(this);
        mc.f4.accept(this);
        mc.f5.accept(this);
        mc.f6.accept(this);
        mc.f7.accept(this);
        mc.f8.accept(this);
        mc.f9.accept(this);
        mc.f10.accept(this);
        mc.f11.accept(this);
        mc.f12.accept(this);
        mc.f13.accept(this);
        mc.f14.accept(this);
        mc.f15.accept(this);
        mc.f16.accept(this);
        mc.f17.accept(this);

        sTable.put(Symbol.symbol(classname(mc)), tempb);
        currentMethod = null;

    }//end mainClass

    //TypeDeclaration
    //f0 -> classDeclaration() | ClassExtendsDeclaration ()

    public void visit(TypeDeclaration x){
        x.f0.accept(this);
    } // end TypeDeclaration

    //ClassDeclaration
    //f0 -> class
    //f1 -> Identifier()
    //f2 -> {
    //f3 -> ( VarDeclaration () )*
    //f4 -> (MethodDeclaration() )*
    //f5 -> }

    public  void visit(ClassDeclaration x){
        ClassBook tempB = new ClassBook(classname(x));
        currentClass = tempB;

        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);

        sTable.put(Symbol.symbol(classname(x)), tempB);
        currentMethod = null;
    } // end ClassDeclaration

    //ClassExtendsDeclaration
    //f0 -> class
    //f1 -> Identifier()
    //f2 -> extends
    //f3 -> Identifier()
    //f4 -> {
    //f5 -> (VarDeclaration())*
    //f6 -> (MethodDeclaration())
    //f7 -> }

    public void visit(ClassExtendsDeclaration x){
        ClassBook tempB = new ClassBook(classname(x));
        currentClass = tempB;

        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);
        x.f6.accept(this);
        x.f7.accept(this);

        tempB.parent = x.f3.f0.toString();
        sTable.put(Symbol.symbol(classname(x)), tempB);
        currentMethod = null;
    } //end ClassExtendsDeclaration

    //VarDeclaration
    //f0 -> Type()
    //f1 -> Identifier()
    //f2 -> ;

    public void visit(VarDeclaration x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);

        if(currentMethod == null) {
            if (currentClass.myItems.alreadyEx(Symbol.symbol(idName(x.f1))))
                RegTypeError();

            if (x.f0.f0.choice instanceof ArrayType)
                currentClass.myItems.put(Symbol.symbol(idName(x.f1)), new ArrayBook());
            if (x.f0.f0.choice instanceof BooleanType)
                currentClass.myItems.put(Symbol.symbol(idName(x.f1)), new BoolBook());
            if (x.f0.f0.choice instanceof IntegerType)
                currentClass.myItems.put(Symbol.symbol(idName(x.f1)), new IntBook());
            if (x.f0.f0.choice instanceof Identifier)
                currentClass.myItems.put(Symbol.symbol(idName(x.f1)), new ClassBook(((Identifier) x.f0.f0.choice).f0.toString()));
        }
        else{
            if(currentMethod.myItems.alreadyEx(Symbol.symbol(idName(x.f1))))
                RegTypeError();

            if(x.f0.f0.choice instanceof ArrayType)
                currentMethod.myItems.put(Symbol.symbol(idName(x.f1)), new ArrayBook());
            if (x.f0.f0.choice instanceof BooleanType)
                currentMethod.myItems.put(Symbol.symbol(idName(x.f1)), new BoolBook());
            if (x.f0.f0.choice instanceof IntegerType)
                currentMethod.myItems.put(Symbol.symbol(idName(x.f1)), new IntBook());
            if (x.f0.f0.choice instanceof Identifier)
                currentMethod.myItems.put(Symbol.symbol(idName(x.f1)), new ClassBook(((Identifier) x.f0.f0.choice).f0.toString()));
        }
    } //end VarDeclaration

    //MethodDeclaration
    //f0 -> public
    //f1 -> Type()
    //f2 -> Identifier()
    //f3 -> (
    //f4 -> (FormalParameterList())
    //f5 -> )
    //f6 -> {
    //f7 -> (VarDeclaration())*
    //f8 -> (Statement ())*
    //f9 -> return
    //f10 -> Expression()
    //f11 -> ;
    //f12 -> }

    public void visit(MethodDeclaration x){
        MethodsBook tempB = new MethodsBook();
        currentMethod = tempB;

        if(x.f1.f0.choice instanceof ArrayType)
            tempB.typeb = new ArrayBook();
        if(x.f1.f0.choice instanceof BooleanType)
            tempB.typeb = new BoolBook();
        if (x.f1.f0.choice instanceof IntegerType)
            tempB.typeb = new IntBook();
        if (x.f1.f0.choice instanceof Identifier) {
            tempB.typeb = new ClassTypeBook();
            ((ClassTypeBook) tempB.typeb).classname = idName((Identifier) x.f1.f0.choice);
        }

        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);
        x.f6.accept(this);
        x.f7.accept(this);
        x.f8.accept(this);
        x.f9.accept(this);
        x.f10.accept(this);
        x.f11.accept(this);
        x.f12.accept(this);

        if(x.f4.present()){
            tempB.paramNum = ((FormalParameterList)(x.f4).node).f1.size();
        }
        else{
            tempB.paramNum = 0;
        }
        currentClass.functions.put(Symbol.symbol(methodname(x)), tempB);
    } //end MethodDeclaration

    //FormalParameterList
    //f0 -> FormalParameter
    //f1 -> ( FormalParameterRest() )*

    public void visit(FormalParameterList x){
        x.f0.accept(this);
        x.f1.accept(this);
    } //end FormalParameterList

    //FormalParameter
    //f0 -> Type()
    //f1 -> Identifier()

    public void visit(FormalParameter x){
        x.f0.accept(this);
        x.f1.accept(this);

        if (currentMethod == null){
            if(currentClass.myItems.alreadyEx(Symbol.symbol(idName(x.f1))))
                RegTypeError();

            if(x.f0.f0.choice instanceof ArrayType)
                currentClass.myItems.put(Symbol.symbol(idName(x.f1)), new ArrayBook());
            if(x.f0.f0.choice instanceof BooleanType)
                currentClass.myItems.put(Symbol.symbol(idName(x.f1)), new BoolBook());
            if(x.f0.f0.choice instanceof IntegerType)
                currentClass.myItems.put(Symbol.symbol(idName(x.f1)), new IntBook());
            if(x.f0.f0.choice instanceof Identifier)
                currentClass.myItems.put(Symbol.symbol(idName(x.f1)), new ClassBook(((Identifier) x.f0.f0.choice).f0.toString()));
        }
        else{
            if(currentMethod.myItems.alreadyEx(Symbol.symbol(idName(x.f1))))
                RegTypeError();
            if(x.f0.f0.choice instanceof ArrayType){
                currentMethod.myItems.put(Symbol.symbol(idName(x.f1)), new ArrayBook());
                currentMethod.ptypes.add(CheckVisitor.ArrayTypeStr);
            }
            if(x.f0.f0.choice instanceof BooleanType){
                currentMethod.myItems.put(Symbol.symbol(idName(x.f1)), new BoolBook());
                currentMethod.ptypes.add(CheckVisitor.BoolTypeStr);
            }
            if(x.f0.f0.choice instanceof IntegerType){
                currentMethod.myItems.put(Symbol.symbol(idName(x.f1)), new IntBook());
                currentMethod.ptypes.add(CheckVisitor.IntTypeStr);
            }
            if(x.f0.f0.choice instanceof Identifier){
                currentMethod.myItems.put(Symbol.symbol(idName(x.f1)), new ClassBook(((Identifier) x.f0.f0.choice).f0.toString()));
                currentMethod.ptypes.add(((Identifier) x.f0.f0.choice).f0.toString());
            }
        }
    } //end FormalParameter

    //FormalParameterRest
    //f0 -> ,
    //f1 -> FormalParameter()

    public void visit(FormalParameterRest x){
        x.f0.accept(this);
        x.f1.accept(this);
    } //end FormalParameterRest

    //Type
    //f0 -> ArrayType() | BooleanType() | IntegerType() | Identifier()

    public void visit(Type x){
        x.f0.accept(this);
    } //end Type

    //ArrayType
    //f0 -> int
    //f1-> [
    //f2 -> ]

    public void visit(ArrayType x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    } //end ArrayType

    //BooleanType
    //f0 -> boolean

    public void visit(BooleanType x){
        x.f0.accept(this);
    } //end BooleanType

    //IntegerType
    //f0 -> int

    public void visit(IntegerType x){
        x.f0.accept(this);
    } //end IntegerType

    //Statement
    //f0 -> Block()| PrintStatement() | IfStatement() | WhileStatement() | ArrayAssignmentStatement() | AssignmentStatement ()

    public void visit (Statement x){
        x.f0.accept(this);
    } //end Statement

    //Block
    //f0 -> {
    //f1 -> ( Statement() )*
    //f2 -> }

    public void visit(Block x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    } //end Block

    //AssignmentStatement
    //f0 -> Identifier
    //f1 -> =
    //f2 -> Expression()
    //f3 -> ;

    public void visit(AssignmentStatement x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
    } //end AssignmentStatement

    //ArrayAssignmentStatement
    //f0 -> Identifier()
    //f1 -> [
    //f2 -> Expression()
    //f3 -> ]
    //f4 -> =
    //f5 -> Expression()
    //f6 -> ;

    public void visit(ArrayAssignmentStatement x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);
        x.f6.accept(this);
    } //end ArrayAssignmentStatement

    //IfStatement
    //f0 -> if
    //f1 -> (
    //f2 -> Expression()
    //f3 -> )
    //f4 -> Statement()
    //f5 -> else
    //f6 -> Statement()

    public void visit(IfStatement x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);
        x.f6.accept(this);
    } //end IfStatement

    //WhileStatement
    //f0 -> while
    //f1 -> (
    //f2 -> Expression()
    //f3 -> )
    //f4 -> Statement()

    public void visit(WhileStatement x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
    } //end WhileStatement

    //PrintStatement
    //f0 -> System.out.println
    //f1 -> (
    //f2 -> Expression()
    //f3 -> )
    //f4 -> ;

    public void visit(PrintStatement x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
    } //end PrintStatement

    //Expression
    //f0 -> PrimaryExpression() | CompareExpression() | PlusExpression() | MinusExpression() | TimesExpression() | AndExpression() | ArrayLookup() | ArrayLength() | MessageSend()

    public void visit (Expression x){
        x.f0.accept(this);
    } //end Expression

    //AndExpression
    //f0 -> PrimaryExpression()
    //f1 -> &&
    //f2 -> PrimaryExpression()

    public void visit(AndExpression x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    } //end AndExpression

    //CompareExpression
    //f0 -> PrimaryExpression()
    //f1 -> <
    //f2 -> PrimaryExpression()

    public void visit(CompareExpression x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    } // end CompareExpression

    //PlusExpression
    //f0 -> PrimaryExpression()
    //f1 -> +
    //f2 -> PrimaryExpression()

    public void visit(PlusExpression x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    }//end PlusExpression

    //MinusExpression
    //f0 -> PrimaryExpression()
    //f1 -> -
    //f2 -> PrimaryExpression()
    public void visit(MinusExpression x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    } //end MinusExpression

    //TimesExpression
    //f0 -> PrimaryExpression()
    //f1 -> *
    //f2 -> PrimaryExpression()

    public void visit(TimesExpression x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    } //end TimesExpression

    //ArrayLookup
    //f0 -> PrimaryExpression()
    //f1 -> [
    //f2 -> PrimaryExpression()
    //f3 -> ]

    public void visit(ArrayLookup x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
    } //end ArrayLookup

    //ArrayLength
    //f0 -> PrimaryExpression()
    //f1 -> .
    //f2 -> length

    public void visit(ArrayLength x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    } //end ArrayLength

    //MessageSend
    //f0 -> PrimaryExpression()
    //f1 -> .
    //f2 -> Identifier()
    //f3 -> (
    //f4 -> ExpressionList() )?
    //f5 -> )

    public void visit(MessageSend x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);
    } //end MessageSend

    //ExpressionList
    //f0 -> Expression()
    //f1 -> ( ExpressionRest())*

    public void visit(ExpressionList x){
        x.f0.accept(this);
        x.f1.accept(this);
    }//end ExpressionList

    //ExpressionRest
    //f0 -> ,
    //f1 -> Expression()

    public void visit(ExpressionRest x){
        x.f0.accept(this);
        x.f1.accept(this);
    } //end ExpressionRest

    //PrimaryExpression
    //f0 -> BracketExpression() | ArrayAllocationExpression() | AllocationExpression() | ThisExpression() | NotExpression() | Identifier() | TrueLiteral() | FalseLiteral() | IntegerLiteral()

    public void visit(PrimaryExpression x){
        x.f0.accept(this);
    }//end PrimaryExpression

    //IntegerLiteral
    //f0 -> <INTERGER_LITERAL>

    public void visit(IntegerLiteral x){
        x.f0.accept(this);
    }//end IntegerLiteral

    //FalseLiteral
    //f0 -> false
    public void visit(FalseLiteral x){
        x.f0.accept(this);
    }//end FalseLiteral

    //TrueLiteral
    //f0 -> true
    public void visit(TrueLiteral x){
        x.f0.accept(this);
    }//end TrueLiteral

    //Identifier
    //f0 -> <IDENTIFIER>

    public void visit(Identifier x){
        x.f0.accept(this);
    }//end Identifier

    //ThisExpression
    //f0 -> ths

    public void visit(ThisExpression x){
        x.f0.accept(this);
    } //end ThisExpression

    //ArrayAllocationExpression
    //f0 -> new
    //f1 -> int
    //f2 -> [
    //f3 -> Expression()
    //f4 -> ]

    public void visit(ArrayAllocationExpression x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
    } //end ArrayAllocationExpression

    //AllocationExpression
    //f0 -> new
    //f1 -> Identifier()
    //f2 -> (
    //f3 -> )

    public void visit(AllocationExpression x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
    } //end AllocationExpression

    //BracketExpression
    //f0 -> (
    //f1 -> Expression()
    //f2 -> )

    public void visit(BracketExpression x){
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
    }//end BracketExpression

    //NotExpression
    //f0 -> !
    //f1 -> Expression()

    public void visit(NotExpression x){
        x.f0.accept(this);
        x.f1.accept(this);
    }//end NotExpression

}//end SymbolTableConstructor Class
