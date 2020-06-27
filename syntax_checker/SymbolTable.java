package syntax_checker;
import java.util.List;
import java.util.ArrayList;

import static javax.swing.UIManager.get;

class BucketHash {
    Book chain;
    Symbol key;
    BucketHash another;

    BucketHash(Book x, BucketHash y, Symbol z){
        chain = x;
        key  = z;
        another = y;
    }
}
class HashTab {
    BucketHash temp [] = new BucketHash [256];

    private  int hash (Symbol x){
        String stemp = x.NameGet();
        int i = 0;
        for (int a = 0; a < stemp.length(); a++){
            i = i *66598 + stemp.charAt(a);
        }
        return i;
    }

    void insert(Symbol x, Book y){
        int i = hash(x)%256;
        i = (i < 0) ? i*-1 : i;
        temp[i] = new BucketHash(y, temp[i], x);
    }

    Book lookup(Symbol x){
        int i = hash(x)%256;
        i = (i < 0) ? i*-1 : i;
        for (BucketHash h = temp[i]; h != null; h = h.another){
            if(x.eq(h.key)){
                return h.chain;
            }
        }
        return null;

    }
    public void print(){
        for (int a = 0; a < temp.length; a++){
            BucketHash current = temp[a];
            while (current != null){
                System.out.println(current.key.toString());
            }
        }
    }

    void pop (Symbol x){
        int i = hash(x)%256;
        temp[i] = temp[i].another;
    }

    public List<String> getAllElements(){
        List<String> elements = new ArrayList<>();
        for (int a = 0; a < temp.length; a++){
            BucketHash current = temp[a];
            while (current != null){
                elements.add(current.key.toString());
                current = current.another;
            }
        }
        return elements;
    }
}
public class SymbolTable {
    HashTab hashtable;

    public SymbolTable() {
        hashtable = new HashTab();
    }

    public void put(Symbol key, Book x){
        hashtable.insert(key, x);
    }

    public boolean alreadyEx(Symbol k){
        return !(get(k) == null);
    }

    public Book get(Symbol k){
        return hashtable.lookup(k);
    }

    public void print() {
        hashtable.print();;
    }
}
