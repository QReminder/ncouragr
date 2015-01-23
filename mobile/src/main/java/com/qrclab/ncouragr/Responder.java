package com.qrclab.ncouragr;

import android.util.Log;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


// Reponder.respondTo(request) returns a canned response to request.
//
public class Responder
{
    private static final String TAG = Responder.class.getSimpleName();

    private static ArrayList<Map.Entry<String, String>> makeResponseMap() {
        final List<String> answer = Arrays.asList(
                "eat",   "no snacks for the rest of the day",
                "jump",  "keep jumping until i say stop",
                "noise", "fo shizzle dude",
                "run",   "run around the block today",
                "walk",  "walk ten more minutes",
                "fnord", "do not see the fnord"
        );
        final ArrayList<Map.Entry<String, String>> result
            = new ArrayList<>();
        for (int i = 0; i < answer.size(); ++i) {
            final Map.Entry<String, String> e
                = new AbstractMap.SimpleEntry<>(
                        answer.get(i), answer.get(++i));
            if (!result.add(e)) throw new AssertionError();
        }
        return result;
    }

    private static final ArrayList<Map.Entry<String, String>> responseMap
        = makeResponseMap();

    // Return a canned response to request.
    //
    public static String respondTo(String request) {
        Log.v(TAG, "request == " + request);
        final String great = "You're doing great!\n";
        final String attaboy = "\nand you'll beat your record this week!";
        final String oops = "I'm sorry.  I did not understand that.";
        for (Map.Entry<String, String> e: responseMap) {
            if (request.contains(e.getKey())) {
                return great + e.getValue() + attaboy;
            }
        }
        return oops;
    }
}
