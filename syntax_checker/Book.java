package syntax_checker;

public abstract class Book {

    SymbolTable myItems;

    public Book () {
        myItems  = new SymbolTable();
    }
    public boolean AddSymbol(Symbol x, Book y){
        if(myItems.get(x) == null){
            myItems.put(x, y);
            return true;
        }
        else
            return false;
    }


}
