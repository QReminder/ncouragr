package com.qrclab.ncouragr;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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

import java.util.ArrayList;


public class NcouragrActivity extends Activity implements OnClickListener
{
    private static final String TAG = NcouragrActivity.class.getSimpleName();
    private static final int SPEECH_REQUEST_CODE = 1;

    private Context mContext;

    private Talker mTalkerHackForDebugging;

    private ImageButton mButton;
    private TextView mInputTextView;
    private TextView mOutputTextView;

    @Override
    protected void onCreate(Bundle bundle) {
        Log.v(TAG, "onCreate() bundle == " + bundle);
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        mContext = this;
        mButton = (ImageButton) findViewById(R.id.button);
        mInputTextView  = (TextView) findViewById(R.id.input);
        mOutputTextView = (TextView) findViewById(R.id.output);
        findViewById(R.id.button).setOnClickListener(this);
    }

    Notification makeWearableNotification() {
        final Intent replyIntent = new Intent(this, NcouragrActivity.class);
        final PendingIntent replyPendingIntent =
            PendingIntent.getActivity(this, 0, replyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        final RemoteInput remoteInput
            = new RemoteInput.Builder("extra_voice_reply")
            .setLabel(getString(R.string.talk))
            .build();
        final NotificationCompat.Action action =
            new NotificationCompat.Action.Builder(R.mipmap.ic_ncouragr,
                    getString(R.string.app_name), replyPendingIntent)
            .addRemoteInput(remoteInput)
            .build();
        final Bitmap bitmap = BitmapFactory.decodeResource(
                getResources(), R.mipmap.ic_ncouragr);
        final Notification result =
            new NotificationCompat.Builder(this)
            .setLargeIcon(bitmap)
            .setSmallIcon(R.mipmap.ic_ncouragr)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.talk))
            .extend(new WearableExtender().addAction(action))
            .build();
        return result;
    }

    private int nextId = 0;
    private int putNotification(String title, String event) {
        final int result = ++nextId;
        final Notification notification = makeWearableNotification();
        final NotificationManagerCompat nm
            = NotificationManagerCompat.from(this);
        nm.notify(result, notification);
        return result;
    }

    private String getSpokenText() {
        Log.v(TAG, "getSpokenText()");
        String result = "";
        final Intent intent = getIntent();
        final Bundle bundle = RemoteInput.getResultsFromIntent(intent);
        if (bundle == null) {
            final ArrayList<String> said
                = intent.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
            if (said != null) result = said.get(0);
        } else {
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

    private void updateUi(String request) {
        Log.v(TAG, "updateUi() request == " + request);
        if (mInputTextView == null) throw new AssertionError();
        if (mOutputTextView == null) throw new AssertionError();
        final String response = Responder.respondTo(request);
        Log.v(TAG, "updateUi() response == " + response);
        mTalkerHackForDebugging = new Talker(this, response);
        mInputTextView.setText(request);
        mOutputTextView.setText(response);
        mButton.setEnabled(true);
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
            updateUi(request);
        }
    }

    private Intent makeRecognizerIntent() {
        final Intent result
            = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        result.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        result.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        result.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                NcouragrActivity.class.getName());
        result.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.talk));
        return result;
    }

    @Override
    public void onClick(View view) {
        Log.v(TAG, "onClick() view == " + view);
        try {
            mButton.setEnabled(false);
            final Intent intent = makeRecognizerIntent();
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception x) {
            Toast.makeText(this, R.string.deaf, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.v(TAG, "onNewIntent() intent == " + intent);
        setIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.v(TAG, "onActivityResult() requestCode == " + requestCode);
        Log.v(TAG, "onActivityResult() resultCode == " + resultCode);
        final boolean ok
            =  requestCode == SPEECH_REQUEST_CODE
            && resultCode  == RESULT_OK;
        if (ok) setIntent(intent);
    }
}
