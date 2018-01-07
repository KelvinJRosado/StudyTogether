package com.example.kelvin.testcapstone;

/**
 * Created by kelvin on 9/4/17.
 */

public class Utility {

    /*
    public static boolean preventAttack(String input) {
        //Prevent SQL injection attempt

        return (input.contains(" ") && input.contains("*") && input.contains(";"));

    }
    */
    public static boolean validateEmail(String email) {
        //Make sure GSU email is used
        return !email.endsWith("@georgiasouthern.edu") && email.length() >= 21;

    }

    public static boolean validateUsername(String name){

        return name.matches("^.*[^a-zA-Z0-9].*$");

    }


}
