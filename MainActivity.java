package com.abir.ai;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int REQUEST_SPEECH = 1001;
    private static final int REQUEST_MIC = 1002;
    private TextView status;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(this, this);
        buildUi();
        say("নমস্কার! আমি আবির AI। মাইক্রোফোনে চাপ দিয়ে কথা বলুন।");
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 36, 28, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("🤖 Abir AI");
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("আপনার বাংলা ভয়েস অ্যাসিস্ট্যান্ট");
        subtitle.setTextSize(17);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(0, 12, 0, 24);
        root.addView(subtitle, sp);

        Button speak = new Button(this);
        speak.setText("🎤 কথা বলুন");
        speak.setTextSize(20);
        speak.setOnClickListener(v -> startListening());
        root.addView(speak, new LinearLayout.LayoutParams(-1, -2));

        status = new TextView(this);
        status.setText("এখানে আপনার কথা ও উত্তর দেখা যাবে।");
        status.setTextSize(17);
        status.setPadding(8, 24, 8, 24);
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        TextView help = new TextView(this);
        help.setText("উদাহরণ:\n• WhatsApp খোলো\n• YouTube খোলো\n• Chrome খোলো\n• Google Maps খোলো\n• ক্যামেরা খোলো\n• আজকের সময় বলো\n• [কোনো প্রশ্ন] সার্চ করো");
        help.setTextSize(15);
        root.addView(help, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    private void startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MIC);
            return;
        }
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "বাংলায় আপনার নির্দেশ বলুন");
            startActivityForResult(intent, REQUEST_SPEECH);
        } catch (ActivityNotFoundException e) {
            status.setText("এই ফোনে speech recognition সুবিধা পাওয়া যাচ্ছে না।");
            say("দুঃখিত, এই ফোনে কথা শোনার সুবিধা পাওয়া যাচ্ছে না।");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String command = results.get(0);
                status.setText("আপনি বলেছেন: " + command);
                handleCommand(command);
            }
        }
    }

    private void handleCommand(String raw) {
        String c = raw.toLowerCase(Locale.ROOT);
        if (c.contains("whatsapp") || c.contains("হোয়াটসঅ্যাপ") || c.contains("হোয়াটসঅ্যাপ")) {
            if (launchPackage("com.whatsapp")) reply("WhatsApp খুলছি।");
            else reply("আপনার ফোনে WhatsApp পাওয়া গেল না।");
        } else if (c.contains("youtube") || c.contains("ইউটিউব")) {
            if (launchPackage("com.google.android.youtube")) reply("YouTube খুলছি।");
            else openWeb("https://www.youtube.com", "YouTube খুলছি।");
        } else if (c.contains("chrome") || c.contains("ক্রোম")) {
            if (launchPackage("com.android.chrome")) reply("Chrome খুলছি।");
            else openWeb("https://www.google.com", "ব্রাউজার খুলছি।");
        } else if (c.contains("maps") || c.contains("ম্যাপ") || c.contains("মানচিত্র")) {
            if (launchPackage("com.google.android.apps.maps")) reply("Google Maps খুলছি।");
            else openWeb("https://maps.google.com", "Google Maps খুলছি।");
        } else if (c.contains("camera") || c.contains("ক্যামেরা")) {
            try {
                Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivity(i);
                reply("ক্যামেরা খুলছি।");
            } catch (Exception e) { reply("ক্যামেরা খোলা যাচ্ছে না।"); }
        } else if (c.contains("সময়") || c.contains("সময়") || c.contains("time")) {
            String time = new java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(new java.util.Date());
            reply("এখন সময় " + time);
        } else if (c.contains("সার্চ") || c.contains("search") || c.contains("খুঁজে") || c.contains("খোঁজ")) {
            openSearch(raw);
        } else {
            openSearch(raw);
            say("আপনার কথাটি Google-এ খুঁজছি।");
        }
    }

    private boolean launchPackage(String packageName) {
        Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launch == null) return false;
        try { startActivity(launch); return true; }
        catch (Exception e) { return false; }
    }

    private void openSearch(String query) {
        try {
            String encoded = URLEncoder.encode(query, "UTF-8");
            openWeb("https://www.google.com/search?q=" + encoded, "Google-এ খুঁজছি।");
        } catch (Exception e) {
            reply("দুঃখিত, সার্চ করা যায়নি।");
        }
    }

    private void openWeb(String url, String spoken) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
            say(spoken);
        } catch (Exception e) { reply("এই কাজটি করা যাচ্ছে না।"); }
    }

    private void reply(String text) {
        status.setText(text);
        say(text);
    }

    private void say(String text) {
        if (ttsReady && tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "abir_speech");
    }

    @Override
    public void onInit(int statusCode) {
        if (statusCode == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("bn", "IN"));
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            if (!ttsReady) ttsReady = true; // Use the device's default voice if Bengali voice is unavailable.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_MIC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        } else if (requestCode == REQUEST_MIC) {
            reply("কথা শোনার জন্য মাইক্রোফোনের অনুমতি দরকার।");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
