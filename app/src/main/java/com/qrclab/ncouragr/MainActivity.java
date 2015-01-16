package com.qrclab.ncouragr;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SPEECH_REQUEST_CODE = 0;

    private static ArrayList<Map.Entry<String, String>> makeResponseMap() {
        final List<String> answer = Arrays.asList(
                "walk",  "Walk 10 more minutes",
                "eat",   "Elminate snacks for the rest of the day",
                "run",   "Run around the block today",
                "fnord", "Don't see the fnords"
        );
        final ArrayList<Map.Entry<String, String>> result
            = new ArrayList<Map.Entry<String, String>>();
        for (int i = 0; i < answer.size(); ++i) {
            final Map.Entry<String, String> e
                = new AbstractMap.SimpleEntry(answer.get(i), answer.get(++i));
            final boolean ok = result.add(e);
            assert(ok);
        }
        return result;
    }

    private static final ArrayList<Map.Entry<String, String>> responseMap
        = makeResponseMap();

    private static String getResponse(String request) {
        final String great = "You're doing great! ";
        final String attaboy = " and you'll beat your record this week!";
        for (Map.Entry<String, String> e: responseMap) {
            if (request.contains(e.getKey())) {
                return great + e.getValue() + attaboy;
            }
        }
        return request;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate() savedInstanceState == " + savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button).setOnClickListener(this);
    }

    private Intent makeIntent() {
        final Intent result
            = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        result.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        result.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        result.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                MainActivity.class.getName());
        result.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.talk));
        return result;
    }

    @Override
    public void onClick(View view) {
        Log.v(TAG, "onClick() view == " + view);
        try {
            final Intent intent = makeIntent();
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception x) {
            Toast.makeText(this, R.string.cannot_hear, Toast.LENGTH_LONG)
                .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v(TAG, "onActivityResult() requestCode == " + requestCode);
        Log.v(TAG, "onActivityResult() resultCode == " + resultCode);
        super.onActivityResult(requestCode, resultCode, intent);
        final boolean ok
            =  requestCode == SPEECH_REQUEST_CODE
            && resultCode  == RESULT_OK;
        if (ok) {
            final ArrayList<String> said
                = intent.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
            final TextView tv = (TextView) findViewById(R.id.text);
            tv.setText(getResponse(said.get(0)));
        }
    }
}
