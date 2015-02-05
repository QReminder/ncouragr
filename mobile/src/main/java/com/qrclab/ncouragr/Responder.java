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
        final String pause = " ... ";
        final List<String> answer = Arrays.asList(
                "weather",    "There is an 80% chance of rain this afternoon."
                /**/          + pause + "The high will be 8 and the low 2.",
                "help me",    "Sending help to your location now.",
                "need help",  "Sending help to your location now.",
                "window",     "Sensors indicate that all windows are closed.",
                "heat down",  "Your thermostat is currently set to 20.",
                "thermostat", "Setting thermostat to 15.",
                "oven off",   "Do not worry. Your oven is off.",
                "walked",     "You walked 1.3 kilometers so far.",
                "this week",  "You are doing great!"
                /**/          + pause + "Walk another half kilometer, "
                /**/          + "and you will meet your goal for the week.",
                "fnord",      "Do not see the fnord!"
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
        final String oops = "I'm sorry.  I did not understand that.";
        for (Map.Entry<String, String> e: responseMap) {
            if (request.contains(e.getKey())) return e.getValue();
        }
        return oops;
    }
}
