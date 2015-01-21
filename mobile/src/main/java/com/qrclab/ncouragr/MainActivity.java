package com.qrclab.ncouragr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SPEECH_REQUEST_CODE = 0;

    private TextToSpeech mTextToSpeech;

    private TextView mInputTextView;
    private TextView mOutputTextView;

    private static ArrayList<Map.Entry<String, String>> makeResponseMap() {
        final List<String> answer = Arrays.asList(
                "walk",  "walk ten more minutes",
                "eat",   "no snacks for the rest of the day",
                "run",   "run around the block today",
                "fnord", "do not see the fnords"
        );
        final ArrayList<Map.Entry<String, String>> result
            = new ArrayList<>();
        for (int i = 0; i < answer.size(); ++i) {
            final Map.Entry<String, String> e
                = new AbstractMap.SimpleEntry<>(
                        answer.get(i), answer.get(++i));
            final boolean ok = result.add(e);
            if ((!ok)) throw new AssertionError();
        }
        return result;
    }

    private static final ArrayList<Map.Entry<String, String>> responseMap
        = makeResponseMap();

    private static String getResponse(String request) {
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

    private TextToSpeech makeTextToSpeech() {
        final TextToSpeech.OnInitListener listener
            = new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        Log.v(TAG, "TextToSpeech.OnInitListener.onInit()"
                                + " status == " + status);
                    } 
                };
        return new TextToSpeech(this, listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate() savedInstanceState == " + savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mInputTextView  = (TextView) findViewById(R.id.input);
        mOutputTextView = (TextView) findViewById(R.id.output);
        findViewById(R.id.button).setOnClickListener(this);
        mTextToSpeech = makeTextToSpeech();
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

    private void maybeTalkBack(String request, String response) {
        if (mTextToSpeech == null) return;
        if (request.equals(response)) return;
        mTextToSpeech.setLanguage(Locale.US);
        mTextToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v(TAG, "onActivityResult() requestCode == " + requestCode);
        Log.v(TAG, "onActivityResult() resultCode == " + resultCode);
        super.onActivityResult(requestCode, resultCode, intent);
        if (mInputTextView == null) throw new AssertionError();
        if (mOutputTextView == null) throw new AssertionError();
        final boolean ok
            =  requestCode == SPEECH_REQUEST_CODE
            && resultCode  == RESULT_OK;
        if (ok) {
            final ArrayList<String> said
                = intent.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
            final String request = said.get(0);
            final String response = getResponse(request);
            mInputTextView.setText(request);
            mOutputTextView.setText(response);
            maybeTalkBack(request, response);
        }
    }
}
