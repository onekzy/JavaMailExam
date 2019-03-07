package utils;

import java.util.ArrayList;

public class CommandList extends ArrayList<String> {


    @Override
    public String toString() {
        String returnString = "";
        for(String string : this) {
            returnString += string.concat("\n");
        }

        return returnString;
    }

}
