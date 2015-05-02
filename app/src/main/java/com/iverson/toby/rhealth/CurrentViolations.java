package com.iverson.toby.rhealth;

import java.util.ArrayList;

/**
 * Created by Toby on 5/2/2015.
 */
public class CurrentViolations {
    private ArrayList<Violation> violations;

    public static void add(Violation v){violations.add(v);}
    public Violation get(int i){
        return violations.get(i);
    }
    public int size(){
       return violations.size();
    }
    public void start(){violations = null;}

}
