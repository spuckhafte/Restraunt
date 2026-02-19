package com.swe.core.utils.env;

import io.github.cdimascio.dotenv.Dotenv;

public class MyEnv {
    private static Dotenv dotenv;

    private static Dotenv myEnv() {
        if (dotenv == null)
            dotenv = Dotenv.load();
        return dotenv;
    }

    public static String getVariable(EnVars var) throws EnVariableNotFound {
        String value = myEnv().get(var.toString());

        if (value == null)
            throw new EnVariableNotFound(var);
        return value;
    }
}

class EnVariableNotFound extends Exception {
    public EnVariableNotFound(EnVars var) {
        super(var.toString() + " is not defined in the [.env] file");
    }
}

