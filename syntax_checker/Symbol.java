package syntax_checker;
import java.util.Dictionary;
import java.util.Hashtable;

public class Symbol {
    private String name;

    private Symbol(String x) {
        name = x;
    }

    private static Dictionary d = new Hashtable();

    public String NameGet() {
        return name;
    }

    public static Symbol symbol(String x) {
        String y = x.intern();
        Symbol z = (Symbol) d.get(y);
        if (z == null) { //check if Symbol exists already
            z = new Symbol(y);
            d.put(y, z);

        }
        return z;
    }

    public boolean lte(Symbol LHS) { //set hashcode for Less Than or Equal to
        return this.lt(LHS) || this.eq(LHS);
    }

    public boolean gte(Symbol LHS) { //set hashcode for Greater Than or Equal to
        return this.gt(LHS) || this.eq(LHS);
    }

    public boolean lt(Symbol LHS) { //set hashcode for Less Than
        return this.hashCode() < LHS.hashCode();
    }

    public boolean gt(Symbol LHS) {  //set hashcode for Greater Than
        return this.hashCode() > LHS.hashCode();
    }

    public boolean eq(Symbol LHS) { //set hashcode for Equal
        return this.hashCode() == LHS.hashCode();
    }


} //end of symbol class
