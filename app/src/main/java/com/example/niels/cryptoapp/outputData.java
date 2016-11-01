package com.example.niels.cryptoapp;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Niels on 17-10-16.
 */

public class outputData {

    File file;
    FileWriter writer;
    PrintWriter write;

    public outputData(String path, boolean append) throws IOException {
        file = Environment.getExternalStoragePublicDirectory(path);
        file.createNewFile();
        writer = new FileWriter(file, append);
        write = new PrintWriter(writer);
    }

    public void write(String str) {
        write.println(str);
    }

    public void close() {
        write.close();
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
