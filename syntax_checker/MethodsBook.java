package syntax_checker;
import java.util.List;
import java.util.ArrayList;

public class MethodsBook extends Book {
    TypeBook typeb;
    int paramNum;
    List<String> ptypes;

    public MethodsBook(){
        ptypes = new ArrayList<>();
    }
}
