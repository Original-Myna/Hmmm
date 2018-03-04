package com.layoutxml.sabs.utils;

import android.util.Log;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CheckWhitelisted {

    public static boolean MatchesWhitelist(List<String> WhitelistedDomains, String Domain)
    {
        // For each whitelisted domain
        for(String whitelisted : WhitelistedDomains)
        {
            // Replace characters ready for regex compare
            String regexString = whitelisted
                    .replace(".", "[.]")
                    .replace("*", "(.*)");

            // Construct regex pattern
            String whitelistexistPattern = String.format("(?i)(%s)", regexString);

            // Compile regex pattern
            Pattern r = Pattern.compile(whitelistexistPattern);

            // Check for matches
            Matcher m = r.matcher(Domain);

            // If there are matches
            if(m.matches())
            {
                return true;
            }
        }

        return false;
    }

}
