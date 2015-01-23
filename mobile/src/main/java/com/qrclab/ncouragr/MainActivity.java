package com.qrclab.ncouragr;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
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
    private static final int SPEECH_REQUEST_CODE = 1;

    private Context mContext;

    private TextToSpeech mTextToSpeech;

    private ImageButton mButton;
    private TextView mInputTextView;
    private TextView mOutputTextView;

    private static ArrayList<Map.Entry<String, String>> makeResponseMap() {
        final List<String> answer = Arrays.asList(
                "walk",  "walk ten more minutes",
                "eat",   "no snacks for the rest of the day",
                "run",   "run around the block today",
                "fnord", "do not see the fnord"
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
        Log.v(TAG, "getResponse() request == " + request);
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
                        final String me
                            = "TextToSpeech.OnInitListener.onInit()";
                            Log.v(TAG, me + " status == " + status);
                        if (status == TextToSpeech.SUCCESS) {
                            final int lang
                                = mTextToSpeech.setLanguage(Locale.US);
                            final boolean no
                                =  lang == TextToSpeech.LANG_MISSING_DATA
                                || lang == TextToSpeech.LANG_NOT_SUPPORTED;
                            if (no) {
                                Log.v(TAG, me + " lang == " + lang);
                                Toast.makeText(mContext, R.string.dumb,
                                        Toast.LENGTH_LONG) .show();
                            } else {
                                mButton.setEnabled(true);
                            }
                        }
                    }
                };
        return new TextToSpeech(this, listener);
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy()");
        super.onDestroy();
        if (mTextToSpeech != null) mTextToSpeech.shutdown();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        Log.v(TAG, "onCreate() bundle == " + bundle);
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        mContext = this;
        mButton = (ImageButton) findViewById(R.id.button);
        mButton.setEnabled(false);
        mTextToSpeech = makeTextToSpeech();
        mInputTextView  = (TextView) findViewById(R.id.input);
        mOutputTextView = (TextView) findViewById(R.id.output);
        findViewById(R.id.button).setOnClickListener(this);
    }

    Notification makeWearableNotification() {
        final RemoteInput remoteInput
            = new RemoteInput.Builder("extra_voice_reply")
            .setLabel(getString(R.string.talk))
            .build();
        final Intent replyIntent = new Intent(this, MainActivity.class);
        final PendingIntent replyPendingIntent =
            PendingIntent.getActivity(this, 0, replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Action action =
            new NotificationCompat.Action.Builder(R.drawable.ic_launcher,
                    getString(R.string.app_name), replyPendingIntent)
            .addRemoteInput(remoteInput)
            .build();
        final Notification result =
            new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.talk))
            .extend(new WearableExtender().addAction(action))
            .build();
        return result;
    }

    private int nextId = 0;
    private int getNextId() { return ++nextId; }
    
    private int putNotification(String title, String event) {
        final int result = getNextId();
        final Notification notification = makeWearableNotification();
        final NotificationManagerCompat nm
            = NotificationManagerCompat.from(this);
        nm.notify(result, notification);
        return result;
    }

    private String getSpokenText() {
        Log.v(TAG, "getSpokenText()");
        String result = "";
        final Bundle bundle = RemoteInput.getResultsFromIntent(getIntent());
        if (bundle != null) {
            final CharSequence cs
                = bundle.getCharSequence("extra_voice_reply");
            if (cs != null) {
                final String s = cs.toString();
                if (s != null) result = s;
            }
        }
        Log.v(TAG, "getSpokenText() returns " + result);
        return result;
    }

    private void maybeTalkBack(String request, String response) {
        Log.v(TAG, "maybeTalkBack() mTextToSpeech == " + mTextToSpeech);
        if (mTextToSpeech == null) return;
        mTextToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void updateDisplay(String request) {
        Log.v(TAG, "updateDisplay() request == " + request);
        if (mInputTextView == null) throw new AssertionError();
        if (mOutputTextView == null) throw new AssertionError();
        final String response = getResponse(request);
        Log.v(TAG, "updateDisplay() response == " + response);
        mInputTextView.setText(request);
        mOutputTextView.setText(response);
        maybeTalkBack(request, response);
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
        final String request = getSpokenText();
        if (request.isEmpty()) {
            final int id
                = putNotification(getString(R.string.app_name),
                    getString(R.string.start));
            Log.v(TAG, "onResume() id == " + id);
        } else {
            Log.v(TAG, "onResume() request == " + request);
            updateDisplay(request);
        }
    }

    private Intent makeRecognizerIntent() {
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
            final Intent intent = makeRecognizerIntent();
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception x) {
            Toast.makeText(this, R.string.deaf, Toast.LENGTH_LONG)
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
            final String request = said.get(0);
            updateDisplay(request);
        }
    }
}
