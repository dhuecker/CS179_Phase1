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
        R _ret = null;
        x.f0.accept(this);
        _ret = x.f1.accept(this);
        x.f2.accept(this);
        return _ret;
    } //end BracketExpression

    //NotExpression
    //f0 -> !
    //f1 -> Expression

    public R visit(NotExpression x){
        R _ret = null;
        x.f0.accept(this);
        _ret = x.f1.accept(this);
        if (!_ret.equals(BoolTypeStr)){
            RegTypeError();
        }
        return _ret;
    }//end NotExpression

    //AllocationExpression
    //f0 -> new
    //f1 -> Identifier()
    //f2 -> (
    //f3 -> )

    public R visit(AllocationExpression x){
        R _ret =null;
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);

        //creating new class instance makes a new symbol table
        ClassBook newC = (ClassBook) symbolTable.get(Symbol.symbol(x.f1.f0.toString()));
        if(newC == null){
            RegTypeError();
            return null;
        }
        _ret = (R)newC.classname;
        return _ret;
    } //end AllocationExpression

    //ArrayAllocationExpression
    //f0 -> new
    //f1 -> int
    //f2 -> [
    //f3 -> Expression()
    //f4 -> ]

    public R visit(ArrayAllocationExpression x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);

        _ret = (R)ArrayTypeStr;
        return _ret;
    }//end ArrayAllocationExpression

    //ThisExpression
    //f0 -> this

    public R visit(ThisExpression x){
        R _ret = null;
        x.f0.accept(this);

        String currentName = currentClass.classname;
        //check the class exists in symbol table
        if(symbolTable.get(Symbol.symbol(currentName)) == null){
            RegTypeError();
        }

        _ret = (R)currentName;
        return _ret;
    }//end ThisExpression

    //Indentifier
    //f0 -> <INDENTIFIER>

    public R visit(Identifier x){
        R _ret = null;
        x.f0.accept(this);

        Book id = null;
        //curent method takes precende over class
        if(currentMethod != null){
            Book tbook = currentMethod.myItems.get(Symbol.symbol(x.f0.toString()));
            if(tbook != null)
                id = tbook;
        }

        if(currentClass !=null && id == null){
            Book tbook = currentClass.myItems.get(Symbol.symbol(x.f0.toString()));
            if(tbook != null)
                id = tbook;
        }

        if(id instanceof IntBook){
            _ret = (R)IntTypeStr;
        }

        if (id instanceof ArrayBook){
            _ret = (R)ArrayTypeStr;
        }

        if (id instanceof BoolBook){
            _ret= (R)BoolTypeStr;
        }

        if(id instanceof ClassBook){
            _ret = (R)((ClassBook) id).classname;
        }

        if(id instanceof ClassTypeBook){
            _ret = (R)((ClassTypeBook) id).classname;
        }
        return _ret;
    }//end Identifier

    //FalseLiteral
    //f0 -> false

    public R visit(FalseLiteral x){
        R _ret = null;
        x.f0.accept(this);
        _ret = (R)BoolTypeStr;
        return _ret;
    }// end FalseLiteral

    //TrueLiteral
    //fo -> true

    public R visit(TrueLiteral x){
        R _ret = null;
        x.f0.accept(this);
        _ret = (R)BoolTypeStr;
        return _ret;
    }//end TrueLiteral

    //IntegerLiteral
    //f0 -> <INTEGER_LITERAL>

    public R visit(IntegerLiteral x){
        R _ret = null;
        x.f0.accept(this);
        _ret = (R)IntTypeStr;
        return _ret;
    } //end IntergerLiteral

    //PrimaryExpression
    //f0 -> BracketExpression() | NotExpression()| ArrayAllocationExpression() | AllocationExpression() | ThisExpreession() | TrueLiteral() | FalseLiteral() | IntegerLiteral() | Identifier()

    public R visit(PrimaryExpression x){
        R _ret = null;
        _ret = x.f0.accept(this);
        return _ret;
    } //end PrimaryExpression

    //ExpressionRest
    // f0 -> ,
    // f1 -> Expression()

    public R visit(ExpressionRest x){
        R _ret =null;
        x.f0.accept(this);
        R temp = x.f1.accept(this);
        _ret = temp;
        return _ret;
    } //end ExpressionRest

    //ExpressionList
    //f0 -> Expression()
    //f1 -> ( ExpressionRest())*

    public R visit(ExpressionList x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        return _ret;
    } //end ExpressionList

    //MessageSend
    //f0 -> PrimaryExpression()
    //f1 -> .
    //f2 -> Identifier()
    //f3 -> (
    //f4 -> (ExpressionList())?
    //f5 -> )

    public R visit(MessageSend x){
        R _ret = null;
        String check = (String)x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);
        //if check is null throw an error
        if(check == null){
            RegTypeError();
            return null;
        }

        //Does method exist in class
        ClassBook cbook = (ClassBook)symbolTable.get(Symbol.symbol(check));
        MethodsBook mbook = (MethodsBook)cbook.functions.get(Symbol.symbol(x.f2.f0.toString()));
        //Now checking superclass for method
        if(mbook ==null){
            ClassBook tempBook = cbook;
            while( tempBook != null){
                MethodsBook tempM = (MethodsBook) tempBook.functions.get(Symbol.symbol(x.f2.f0.toString()));

                if(tempM != null){
                    mbook = tempM;
                    break; //get out of while
                }
                tempBook = (ClassBook)symbolTable.get(Symbol.symbol(tempBook.parent));
            }

            if(tempBook == null){ //if tempBook is still null then throw an error
                RegTypeError();
                return null;
            }
        }
        if(mbook == null){ //same as tempBook throw error if still null and return null
            RegTypeError();
            return null;
        }

        if(x.f4.present()){
            //check expression list has correct length
            if(mbook.paramNum != ((ExpressionList)x.f4.node).f1.size()){
                RegTypeError();
            }
            //are the variables the expected types
            if(!((ExpressionList)x.f4.node).f0.accept(this).equals(mbook.ptypes.get(0)) && mbook.paramNum != 0){
                RegTypeError();
            }

            for(int a = 0; a < ((ExpressionList)x.f4.node).f1.size(); a++){
                String currentType = (String)((ExpressionList)x.f4.node).f1.elementAt(a).accept(this);
                if(!currentType.equals(mbook.ptypes.get(a+1)) && !isSubType(mbook.ptypes.get(a+1), currentType)){
                    RegTypeError();
                }
            }
        }

        //Now setup _ret with correct Type String
        if(mbook.typeb instanceof ArrayBook){
            _ret = (R) ArrayTypeStr;
        }

        if(mbook.typeb instanceof IntBook){
            _ret = (R) IntTypeStr;
        }

        if(mbook.typeb instanceof BoolBook){
            _ret = (R) BoolTypeStr;
        }

        if(mbook.typeb instanceof ClassTypeBook){
            _ret = (R)((ClassTypeBook) mbook.typeb).classname;
        }

        return _ret;
    }// end MessageSend

    //ArrayLength
    //f0 -> PrimaryExpression()
    //f1 -> .
    //f2 -> length

    public R visit(ArrayLength x){
        R _ret =null;
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        _ret = (R)IntTypeStr;
        return _ret;
    }//end ArrayLength

    //ArrayLookup
    //f0 -> PrimaryExpression()
    //f1 -> [
    //f2 -> PrimaryExpression()
    //f3 -> ]

    public R visit(ArrayLookup x){
        R _ret = null;
        R arr_ex = x.f0.accept(this);
        x.f1.accept(this);
        R index = x.f2.accept(this);
        x.f3.accept(this);

        if(!arr_ex.equals(ArrayTypeStr) || !index.equals(IntTypeStr)){
            RegTypeError();
        }

        _ret = (R)IntTypeStr;
        return _ret;
    }//end ArrayLookup

    //TimesExpression
    //f0 -> PrimaryExpression()
    //f1 -> *
    //f2 -> PrimaryExpression()

    public R visit(TimesExpression x){
        R _ret = null;
        R RHS = x.f0.accept(this);
        x.f1.accept(this);
        R LHS = x.f2.accept(this);
        if (!RHS.equals(LHS) || !RHS.equals(IntTypeStr) || !LHS.equals(IntTypeStr)){
            RegTypeError();
        }
        _ret = RHS;
        return _ret;
    } //end TimesExpression

    //MinusExpression
    //f0 -> PrimaryExpression()
    //f1 -> -
    //f2 -> PrimaryExpression()

    public R visit(MinusExpression x){
        R _ret = null;
        R RHS = x.f0.accept(this);
        x.f1.accept(this);
        R LHS = x.f2.accept(this);
        if (!RHS.equals(LHS) || !RHS.equals(IntTypeStr) || !LHS.equals(IntTypeStr)){
            RegTypeError();
        }
        _ret = RHS;
        return _ret;
    } //end MinusExpression

    //PlusExpression
    //f0 -> PrimaryExpression()
    //f1 -> +
    //f2 -> PrimaryExpression()

    public R visit(PlusExpression x){
        R _ret = null;
        R RHS = x.f0.accept(this);
        x.f1.accept(this);
        R LHS = x.f2.accept(this);
        if (!RHS.equals(LHS) || !RHS.equals(IntTypeStr) || !LHS.equals(IntTypeStr)){
            RegTypeError();
        }
        _ret = RHS;
        return _ret;
    } //end PlusExpression

    //CompareExpression
    //f0 -> PrimaryExpression()
    //f1 -> <
    //f2 -> PrimaryExpression()

    public R visit(TimesExpression x){
        R _ret = null;
        R RHS = x.f0.accept(this);
        x.f1.accept(this);
        R LHS = x.f2.accept(this);
        if (!RHS.equals(LHS) || !RHS.equals(IntTypeStr) || !LHS.equals(IntTypeStr)){
            RegTypeError();
        }
        _ret = (R) BoolTypeStr;
        return _ret;
    } //end CompareExpression

    //AndExpression
    //f0 -> PrimaryExpression()
    //f1 -> &&
    //f2 -> PrimaryExpression()

    public R visit(AndExpression x){
        R _ret = null;
        R RHS = x.f0.accept(this);
        x.f1.accept(this);
        R LHS = x.f2.accept(this);
        if (!RHS.equals(LHS) || !RHS.equals(IntTypeStr) || !LHS.equals(IntTypeStr)){
            RegTypeError();
        }
        _ret = RHS;
        return _ret;
    } //end AndExpression

    //Expression
    //f0 -> PrimaryExpression() | PlusExpression() | MinusExpression() | TimesExpression() | CompareExpression() | AndExpression() | ArrayLength() | ArrayLookup() | MessageSend()

    public R visit(Expression x){
        R _ret = null;
        _ret = x.f0.accept(this);
        return _ret;
    }//end Expression

    //PrintStatement
    //f0 -> System.out.println
    //f1 -> (
    //f2 -> Expression()
    //f3 -> )
    //f4 -> ;

    public R visit(PrintStatement x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        R expr = x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);

        if(expr == null || !expr.equals(IntTypeStr)){ //check for error here
            RegTypeError();
        }

        return _ret;
    }//end PrintStatement

    //WhileStatement
    //f0 -> while
    //f1 -> (
    //f2 -> Expression()
    //f3 -> )
    //f4 -> Statement()

    public R visit(WhileStatement x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        R expr = x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        //check for error
        if (!expr.equals(BoolTypeStr)){
            RegTypeError();
        }

        return _ret;
    }//end WhileStatement

    //IfStatement
    //f0 -> if
    //f1 -> (
    //f2 -> Expression()
    //f3 -> )
    //f4 -> Statement()
    //f5 -> else
    //f6 -> Statement()

    public R visit(IfStatement x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        R expr = x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);
        x.f6.accept(this);
        //check for error
        if (!expr.equals(BoolTypeStr)){
            RegTypeError();
        }

        return _ret;
    }//end IfStatement

    //ArrayAssignmentStatement
    //f0 -> Identifier()
    //f1 -> [
    //f2 -> Expression()
    //f3 -> ]
    //f4 -> =
    //f5 -> Expression()
    //f6 -> ;

    public R visit(ArrayAssignmentStatement x){
        R _ret = null;
        R ident = x.f0.accept(this);
        x.f1.accept(this);
        R expr = x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        R expr2 = x.f5.accept(this);
        x.f6.accept(this);

        //Now check for any errors
        if (!ident.equals(ArrayTypeStr)){
            RegTypeError();
        }

        if (!expr.equals(IntTypeStr)){
            RegTypeError();
        }

        if (!expr2.equals(IntTypeStr)){
            RegTypeError();
        }

        return _ret;
    }//end ArrayAssignmentStatement

    //AssignmentStatement
    //f0 -> Identifier()
    //f1 -> =
    //f2 -> Expression()
    //f3 -> ;

    public R visit(AssignmentStatement x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        String ExType = (String) x.f2.accept(this);
        x.f3.accept(this);


        //continue here
        Book tempMid = null;
        //Does f0 ident exist in symbol table already
        if (currentMethod != null){
            tempMid = currentMethod.myItems.get(Symbol.symbol(x.f0.f0.toString()));
        }
        Book tempCid = currentClass.myItems.get(Symbol.symbol(x.f0.f0.toString()));
        //Error check
        if (tempMid == null && tempCid == null){
            RegTypeError();
        }

        //Do both sides of the assignment type check
        Book tempbook = (tempMid != null) ? tempMid : tempCid;
        String id = "";
        if(tempbook instanceof ArrayBook){
            id = ArrayTypeStr;
        }
        if (tempbook instanceof BoolBook){
            id = BoolTypeStr;
        }
        if (tempbook instanceof IntBook){
            id = IntTypeStr;
        }
        if (tempbook instanceof ClassBook){
            id = ((ClassBook)tempbook).classname;
        }
        //Another error check below
        if(ExType == null || !ExType.equals(id) && !isSubType(id, ExType)){
            RegTypeError();
        }

        return _ret;

    }//end AssignmentStatement

    //Block
    //f0 -> {
    //f1 -> (Statement())*
    //f2 -> }

    public R visit(Block x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        return _ret;
    }//end Block

    //Statement
    //f0 -> IfStatement() | WhileStatement() | AssignmentStatement() | ArrayAssignmentStatement() | PrintStatement() | Block ()

    public R visit(Statement x){
        R _ret = null;
        x.f0.accept(this);
        return _ret;
    } //end Statement

    //IntegerType
    //f0 -> int

    public R visit(IntegerType x){
        R _ret = null;
        x.f0.accept(this);
        return _ret;
    }//end IntegerType

    //BooleanType
    //f0 -> boolean

    public R visit(BooleanType x){
        R _ret = null;
        x.f0.accept(this);
        return _ret;
    }//end BooleanType

    //ArrayType
    //f0 -> int
    //f1 -> [
    //f2 -> ]

    public R visit(ArrayType x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        return _ret;
    }//end ArrayType

    //Type
    //f0 -> Identifier() | ArrayType() | BooleanType() | IntegerType()
    public R visit(Type x){
        R _ret = null;
        x.f0.accept(this);
        return _ret;
    }//end Type

    //FormalParameterRest
    //f0 -> ,
    //f1 -> FormalParameter()

    public R visit(FormalParameterRest x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        return _ret;
    } //end FormalParameterRest

    //FormalParameter
    //f0 -> Type()
    //f1 -> Identifier()

    public R visit(FormalParameter x){
        R _ret = null;
        x.f0.accept(this);
        _ret = x.f1.accept(this);
        return _ret;
    } //end FormalParameter

    //FormalParameterList
    //f0 -> FormalParameter()
    //f1 -> (FormalParameterRest())*

    public R visit(FormalParameterList x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        return _ret;
    } //end FormalParameterList


    //MethodDeclaration
    //f0 -> public
    //f1 -> Type()
    //f2 -> Identifier()
    //f3 -> (
    //f4 -> (FormalParameterList())?
    //f5 -> )
    //f6 -> {
    //f7 -> (VarDeclaration())*
    //f8 -> (Statement())*
    //f9 -> return
    //f10 -> Expression()
    //f11 -> ;
    //f12 -> }

    public R visit(MethodDeclaration x){
        R _ret = null;
        currentMethod = (MethodsBook) currentClass.functions.get(Symbol.symbol(methodname(x)));
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
        String checkType = (String)x.f10.accept(this);
        x.f11.accept(this);
        x.f12.accept(this);

        //now check return type
        if (x.f1.f0.choice instanceof Identifier){
            _ret = (R) ((ClassTypeBook)currentMethod.typeb).classname;
        }
        if (x.f1.f0.choice instanceof ArrayType){
            _ret = (R) ArrayTypeStr;
        }
        if (x.f1.f0.choice instanceof BooleanType){
            _ret = (R) BoolTypeStr;
        }
        if (x.f1.f0.choice instanceof IntegerType){
            _ret = (R) IntTypeStr;
        }

        //more error checking before return
        if (!((R)checkType).equals(_ret)){
            RegTypeError();
        }
        if(!distinct(x.f4)){
            RegTypeError();
        }

        return _ret;
    }//end MethodDeclaration

    //VarDeclaration
    //f0 -> Type()
    //f1 -> Identifier()
    //f2 -> ;

    public R visit(VarDeclaration x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        return _ret;
    }//end VarDeclaration

    //ClassExtendsDeclaration
    //f0 -> class
    //f1 -> Identifier()
    //f2 -> extends
    //f3 -> Identifier()
    //f4 -> {
    //f5 -> (VarDeclaration())*
    //f6 -> (MethodDeclaration())*
    //f7 -> }

    public R visit(ClassExtendsDeclaration x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);
        x.f6.accept(this);
        x.f7.accept(this);
        return _ret;
    } //end ClassExtendsDeclaration

    //ClassDeclaration
    //f0 -> class
    //f1 -> Identifier()
    //f2 -> {
    //f3 -> (VarDeclaration())*
    //f4 -> (MethodDeclaration())*
    //f5 -> }

    public R visit(ClassDeclaration x){
        R _ret = null;
        currentClass = (ClassBook)symbolTable.get(Symbol.symbol(classname(x)));
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        x.f3.accept(this);
        x.f4.accept(this);
        x.f5.accept(this);
        return _ret;
    } //end ClassDeclaration

    //TypeDeclaration
    //f0 -> ClassDeclaration() | ClassExtendsDeclaration()

    public R visit(TypeDeclaration x){
        R _ret = null;
        x.f0.accept(this);
        return _ret;
    }//end TypeDeclaration

    //Mainclass
    //f0 -> class
    //f1 -> Identifier()
    //f2 -> {
    //f3 -> public
    //f4 -> static
    //f5 -> void
    //f6 -> main
    //f7 -> (
    //f8 -> String
    //f9 -> [
    //f10 -> ]
    //f11 -> Identifier()
    //f12 -> )
    //f13 -> {
    //f14 -> (VarDeclaration())*
    //f15 -> (Statement())*
    //f16 -> }
    //f17 -> }

    public R visit(MainClass x){
        R _ret = null;
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
        x.f13.accept(this);
        x.f14.accept(this);
        x.f15.accept(this);
        x.f16.accept(this);
        x.f17.accept(this);
        return _ret;
    } //end MainClass

    //Goal
    //f0 -> MainClass()
    //f1 -> (TypeDeclaration())*
    //f2 -> <EOF>
    public R visit(Goal x){
        R _ret = null;
        x.f0.accept(this);
        x.f1.accept(this);
        x.f2.accept(this);
        return _ret;
    }//end Goal


}
