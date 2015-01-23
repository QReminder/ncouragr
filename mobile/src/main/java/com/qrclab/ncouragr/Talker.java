package com.qrclab.ncouragr;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;


public class Talker
    implements TextToSpeech.OnInitListener,
               TextToSpeech.OnUtteranceCompletedListener
{
    private static final String TAG = Talker.class.getSimpleName();

    private final Context mContext;

    private final TextToSpeech mTts;
    private final HashMap<String, String> mUtterance
        = new HashMap<String, String>();

    public void onInit(int status) {
        Log.v(TAG, "onInit() status == " + status);
        if (status == TextToSpeech.SUCCESS) {
            final int lang = mTts.setLanguage(Locale.US);
            final boolean no
                =  lang == TextToSpeech.LANG_MISSING_DATA
                || lang == TextToSpeech.LANG_NOT_SUPPORTED;
            if (no) {
                Log.v(TAG, "onInit() lang == " + lang);
                Toast.makeText(mContext, R.string.dumb,
                        Toast.LENGTH_LONG).show();
            } else {
                final String u = mUtterance.get(
                        TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                Log.v(TAG, "onInit() u == " + u);
                mTts.setOnUtteranceCompletedListener(this);
                mTts.speak(u, TextToSpeech.QUEUE_ADD, null);
                Log.v(TAG, "onInit() called mTts.speak()");
            }
        }
    }

    public void onUtteranceCompleted(String id) {
        Log.v(TAG, "onUtteranceCompleted() id == " + id);
        mTts.shutdown();
    }

    // Say u for c.
    //
    Talker(Context c, String u) {
        mContext = c;
        mUtterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, u);
        mTts = new TextToSpeech(c, this);
    }
}
