package syntax_checker;
import syntaxtree.*;
public class MethodType {
    public Type reType;
    public NodeOptional argList;

    public MethodType(Type r, NodeOptional al){
        this.reType = r;
        this.argList = al;
    }

    public boolean equals(MethodType RHS){
        if(this.reType.f0.which == RHS.reType.f0.which){
            if(!this.argList.present() && !RHS.argList.present()){
                return true;
            }
            else {
                if(this.argList.present() && RHS.argList.present() && ((FormalParameterList)this.argList.node).f1.size() == ((FormalParameterList)RHS.argList.node).f1.size())
                    return true;
            }
        }
        return false;
    }
}
