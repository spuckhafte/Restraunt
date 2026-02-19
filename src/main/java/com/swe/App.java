package com.swe;

import com.swe.core.utils.env.EnVars;
import com.swe.core.utils.env.MyEnv;

public class App {
    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );
        System.out.println(MyEnv.getVariable(EnVars.DB_NAME));
    }
}
