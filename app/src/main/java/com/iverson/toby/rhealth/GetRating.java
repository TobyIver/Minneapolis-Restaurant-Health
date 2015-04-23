package com.iverson.toby.rhealth;

import java.util.ArrayList;

/**
 * Created by Toby on 4/23/2015.
 */
public class GetRating {

    public int GetRating(ArrayList<Violation> Vs){
        int rating = 100;

        int count = Vs.size();
        for (int i = 0; i > count; i++ ){
            Violation v = Vs.get(i);
            int r = 0;
            r = 2 * (4 - Integer.parseInt(v.getRiskLevel()));  //risk levels 1-3, 1 being worse
            if (v.getCritical() == "Yes"){
                r = r * 5;
            }

            rating = rating - r;

        }


        return rating;
    }
}
