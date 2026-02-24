package org.spacegram.translator;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class SimplyTranslateTranslator implements BaseTranslator {

    private static final String API_URL = "https://simplytranslate.org/api/translate";
    private final String engine;

    public SimplyTranslateTranslator(String engine) {
        this.engine = engine;
    }

    @Override
    public void translate(String text, String fromLang, String toLang, Utilities.Callback2<String, Boolean> done) {
        if (TextUtils.isEmpty(text) || done == null) {
            return;
        }

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String sourceLang = TextUtils.isEmpty(fromLang) ? "auto" : fromLang;
                String url = API_URL + "?engine=" + Uri.encode(engine)
                        + "&from=" + Uri.encode(sourceLang)
                        + "&to=" + Uri.encode(toLang)
                        + "&text=" + Uri.encode(text);

                connection = (HttpURLConnection) new URI(url).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    boolean rateLimit = responseCode == 429;
                    AndroidUtilities.runOnUIThread(() -> done.run(null, rateLimit));
                    return;
                }

                StringBuilder resultBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        resultBuilder.append(line);
                    }
                }

                JSONObject response = new JSONObject(resultBuilder.toString());
                String translatedText = response.optString("translated_text", null);
                AndroidUtilities.runOnUIThread(() -> done.run(translatedText, false));

            } catch (Exception e) {
                Log.e("SimplyTranslate", "Translation failed", e);
                AndroidUtilities.runOnUIThread(() -> done.run(null, false));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
