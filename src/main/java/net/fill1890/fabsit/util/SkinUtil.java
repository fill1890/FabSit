package net.fill1890.fabsit.util;

import net.fill1890.fabsit.error.LoadSkinException;
import net.minecraft.nbt.NbtCompound;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

public abstract class SkinUtil {
    private static final String MOJANG_UUID2SKIN = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

    public static NbtCompound fetchByUuid(UUID uuid) throws LoadSkinException {
        URL url;
        String reply;

        try {
            url = new URL(String.format(MOJANG_UUID2SKIN, uuid.toString()));
        } catch (MalformedURLException e) {
            throw new LoadSkinException.UrlException();
        }

        try {
            reply = queryUrl(url);
        } catch (IOException e) {
            throw new LoadSkinException.SkinIOException();
        }

        if (reply == null || reply.isEmpty()) {
            throw new LoadSkinException.NoResponseException();
        }

        if (reply.contains("error")) {
            throw new LoadSkinException.ErrorResponseException();
        }

        // regexes taken from Taterzens logic
        String value = reply.split("\"value\":\"")[1].split("\"")[0];
        String signature = reply.split("\"signature\":\"")[1].split("\"")[0];

        if (value.isEmpty() || signature.isEmpty()) {
            throw new LoadSkinException.InvalidResponseException();
        }

        NbtCompound nbt = new NbtCompound();

        nbt.putString("value", value);
        nbt.putString("signature", signature);

        return nbt;
    }

    // taken from Taterzens
    private static String queryUrl(URL url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        String reply;

        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");

        if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            return null;
        }

        try (
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                Scanner scanner = new Scanner(isr)
        ) {
            StringBuilder replyBuilder = new StringBuilder();
            while(scanner.hasNextLine()) {
                String line = scanner.next();
                if(line.trim().isEmpty())
                    continue;
                replyBuilder.append(line);
            }

            reply = replyBuilder.toString();
        }

        connection.disconnect();

        return reply;
    }
}
