package org.spacegram.translator;

import android.text.TextUtils;

import org.spacegram.SpaceGramConfig;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Helper class to handle translation based on SpaceGramConfig.translateStyle.
 */
public class TranslationHelper {

    /**
     * Translate a message respecting the user's style preference.
     */
    public static void translateMessage(
            MessageObject messageObject,
            String toLanguage,
            int currentAccount,
            Runnable onComplete
    ) {
        if (messageObject == null || messageObject.messageOwner == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        String text = messageObject.messageOwner.message;
        if (TextUtils.isEmpty(text)) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        LanguageDetector.detectLanguage(
                text,
                detectedLang -> translateWithStyle(
                        messageObject,
                        text,
                        detectedLang,
                        toLanguage,
                        currentAccount,
                        onComplete
                ),
                error -> translateWithStyle(
                        messageObject,
                        text,
                        "auto",
                        toLanguage,
                        currentAccount,
                        onComplete
                )
        );
    }

    private static void translateWithStyle(
            MessageObject messageObject,
            String text,
            String fromLang,
            String toLang,
            int currentAccount,
            Runnable onComplete
    ) {
        if (SpaceGramConfig.translateStyle == 0) {
            translateInline(
                    messageObject,
                    text,
                    fromLang,
                    toLang,
                    currentAccount,
                    onComplete
            );
        } else if (onComplete != null) {
            AndroidUtilities.runOnUIThread(onComplete);
        }
    }

    /**
     * Translate inline: Store translation in messageObject and notify UI.
     */
    private static void translateInline(
            MessageObject messageObject,
            String text,
            String fromLang,
            String toLang,
            int currentAccount,
            Runnable onComplete
    ) {
        final LinkMaskedText linkMaskedText = maskLinks(text, messageObject.messageOwner.entities);
        SpaceGramTranslator.getInstance().translate(
                linkMaskedText.maskedText,
                fromLang,
                toLang,
                (result, rateLimit) -> AndroidUtilities.runOnUIThread(() -> {

                    if (result != null && messageObject.messageOwner != null) {

                        TLRPC.TL_textWithEntities translatedText =
                                new TLRPC.TL_textWithEntities();

                        translatedText.text = restorePlaceholders(result, linkMaskedText.placeholders, translatedText.entities = new ArrayList<>());

                        messageObject.messageOwner.translatedText =
                                translatedText;
                        messageObject.messageOwner.originalLanguage =
                                fromLang;
                        messageObject.messageOwner.translatedToLanguage =
                                toLang;
                        messageObject.translated = true;

                        NotificationCenter.getInstance(currentAccount)
                                .postNotificationName(
                                        NotificationCenter.messageTranslated,
                                        messageObject.getDialogId(),
                                        messageObject.getId()
                                );
                    }

                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
        );
    }

    private static class LinkPlaceholder {
        final String token;
        final String visibleText;
        final String url;
        final boolean textUrl;

        LinkPlaceholder(String token, String visibleText, String url, boolean textUrl) {
            this.token = token;
            this.visibleText = visibleText;
            this.url = url;
            this.textUrl = textUrl;
        }
    }

    private static class LinkMaskedText {
        final String maskedText;
        final ArrayList<LinkPlaceholder> placeholders;

        LinkMaskedText(String maskedText, ArrayList<LinkPlaceholder> placeholders) {
            this.maskedText = maskedText;
            this.placeholders = placeholders;
        }
    }

    private static LinkMaskedText maskLinks(String text, ArrayList<TLRPC.MessageEntity> entities) {
        if (TextUtils.isEmpty(text) || entities == null || entities.isEmpty()) {
            return new LinkMaskedText(text, new ArrayList<>());
        }

        ArrayList<TLRPC.MessageEntity> linkEntities = new ArrayList<>();
        for (int i = 0; i < entities.size(); i++) {
            TLRPC.MessageEntity entity = entities.get(i);
            if (entity == null) {
                continue;
            }
            if (entity instanceof TLRPC.TL_messageEntityTextUrl || entity instanceof TLRPC.TL_messageEntityUrl) {
                linkEntities.add(entity);
            }
        }
        if (linkEntities.isEmpty()) {
            return new LinkMaskedText(text, new ArrayList<>());
        }

        Collections.sort(linkEntities, Comparator.comparingInt(e -> e.offset));
        StringBuilder builder = new StringBuilder();
        ArrayList<LinkPlaceholder> placeholders = new ArrayList<>();
        int cursor = 0;
        int tokenIndex = 0;
        for (int i = 0; i < linkEntities.size(); i++) {
            TLRPC.MessageEntity entity = linkEntities.get(i);
            int start = Math.max(0, entity.offset);
            int end = Math.min(text.length(), entity.offset + entity.length);
            if (start < cursor || end <= start) {
                continue;
            }
            builder.append(text, cursor, start);
            String token = "__SG_LINK_" + tokenIndex++ + "__";
            String visibleText = text.substring(start, end);
            String url = entity instanceof TLRPC.TL_messageEntityTextUrl ? entity.url : visibleText;
            placeholders.add(new LinkPlaceholder(token, visibleText, url, entity instanceof TLRPC.TL_messageEntityTextUrl));
            builder.append(token);
            cursor = end;
        }
        builder.append(text, cursor, text.length());
        return new LinkMaskedText(builder.toString(), placeholders);
    }

    private static String restorePlaceholders(String translatedText, ArrayList<LinkPlaceholder> placeholders, ArrayList<TLRPC.MessageEntity> outputEntities) {
        if (TextUtils.isEmpty(translatedText) || placeholders == null || placeholders.isEmpty()) {
            return translatedText;
        }

        String restored = translatedText;
        for (int i = 0; i < placeholders.size(); i++) {
            LinkPlaceholder placeholder = placeholders.get(i);
            int start = restored.indexOf(placeholder.token);
            if (start < 0) {
                continue;
            }
            String replacement = placeholder.visibleText;
            restored = restored.substring(0, start) + replacement + restored.substring(start + placeholder.token.length());

            if (!TextUtils.isEmpty(placeholder.url) && outputEntities != null) {
                if (placeholder.textUrl) {
                    TLRPC.TL_messageEntityTextUrl entity = new TLRPC.TL_messageEntityTextUrl();
                    entity.offset = start;
                    entity.length = replacement.length();
                    entity.url = placeholder.url;
                    outputEntities.add(entity);
                } else {
                    TLRPC.TL_messageEntityUrl entity = new TLRPC.TL_messageEntityUrl();
                    entity.offset = start;
                    entity.length = replacement.length();
                    outputEntities.add(entity);
                }
            }
        }

        return restored;
    }

    /**
     * Check if a message is currently translated.
     */
    public static boolean isTranslated(MessageObject messageObject) {
        return messageObject != null
                && messageObject.messageOwner != null
                && messageObject.translated
                && messageObject.messageOwner.translatedText != null
                && !TextUtils.isEmpty(
                        messageObject.messageOwner.translatedText.text
                );
    }

    /**
     * Get translated text from message.
     */
    public static String getTranslatedText(
            MessageObject messageObject
    ) {
        if (isTranslated(messageObject)) {
            return messageObject.messageOwner.translatedText.text;
        }
        return null;
    }

    /**
     * Clear translation from message (show original).
     */
    public static void clearTranslation(
            MessageObject messageObject,
            int currentAccount
    ) {
        if (messageObject != null
                && messageObject.messageOwner != null) {

            messageObject.messageOwner.translatedText = null;
            messageObject.messageOwner.originalLanguage = null;
            messageObject.messageOwner.translatedToLanguage = null;
            messageObject.translated = false;

            NotificationCenter.getInstance(currentAccount)
                    .postNotificationName(
                            NotificationCenter.messageTranslated,
                            messageObject.getDialogId(),
                            messageObject.getId()
                    );
        }
    }
}
