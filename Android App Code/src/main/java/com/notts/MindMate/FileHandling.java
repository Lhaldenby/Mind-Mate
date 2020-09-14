package com.notts.MindMate;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public final class FileHandling {

    public FileHandling(Context context) {
    }

    public static void writeFile(Context context, String Filename, String Text) throws IOException {
        FileOutputStream fos = context.openFileOutput(Filename, Context.MODE_APPEND);
        fos.write(Text.getBytes());
        fos.close();
    }


    public static void writeFileNoAppend(Context context, String Filename, String Text) throws IOException {
        FileOutputStream fos = context.openFileOutput(Filename, Context.MODE_PRIVATE);
        fos.write(Text.getBytes());
        fos.close();
    }

    public static void clearFile(Context context, String Filename) throws IOException {
        FileOutputStream fos = context.openFileOutput(Filename, Context.MODE_PRIVATE);
        fos.write("".getBytes());
        fos.close();
    }

    public static String readFile(Context context, String file) throws IOException {
        String content = "";
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(context.openFileInput(file)));
            String inputString;

            inputString = inputReader.readLine();

            while(inputString != null){
                stringBuilder.append(inputString);
                inputString = inputReader.readLine();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            content = stringBuilder.toString();
            return content;
        }
    }
}
