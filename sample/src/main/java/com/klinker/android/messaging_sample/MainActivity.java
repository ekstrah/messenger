/*
 * Copyright 2014 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.messaging_sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.logger.Log;
import com.klinker.android.logger.OnLogListener;
import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.BroadcastUtils;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private Settings settings;

    private Button setDefaultAppButton;
    private Button selectApns;
    private EditText messageField;
    private ImageView imageToSend;
    private Button sendButton;
    private Button loadButton;
    public int index;
    public int contact_size;
    public String[] lines;

    private RecyclerView log;

    private LogAdapter logAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("request_permissions", true) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(new Intent(this, PermissionActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        initSettings();
        initViews();
        initActions();
        initLogging();

        BroadcastUtils.sendExplicitBroadcast(this, new Intent(), "test action");

    }

    private void initSettings() {
        settings = Settings.get(this);

        if (TextUtils.isEmpty(settings.getMmsc()) &&
                Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            initApns();
        }
    }

    private void initApns() {
        ApnUtils.initDefaultApns(this, new ApnUtils.OnApnFinishedListener() {
            @Override
            public void onFinished() {
                settings = Settings.get(MainActivity.this, true);
            }
        });
    }

    private void initViews() {
        setDefaultAppButton = (Button) findViewById(R.id.set_as_default);
        selectApns = (Button) findViewById(R.id.apns);
        messageField = (EditText) findViewById(R.id.message);
        imageToSend = (ImageView) findViewById(R.id.image);
        sendButton = (Button) findViewById(R.id.send);
        loadButton = (Button) findViewById(R.id.load);
    }

    public int openDialog(String line) {
        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle("보내시겠습니까?");
        adb.setMessage("이 번호로 보내시겠습니까?" + line);
        final String finalLine = line;
        adb.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendMessage(finalLine);
            }
        });
        adb.show();
        return 0;
    }

    private void initActions() {
        if (Utils.isDefaultSmsApp(this)) {
            setDefaultAppButton.setVisibility(View.GONE);
        } else {
            setDefaultAppButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setDefaultSmsApp();
                }
            });
        }
        Log.d("mySuperTag", "My Test Value1");
        selectApns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initApns();
            }
        });
        Log.d("mySuperTag", "My Test Value2");

        imageToSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSendImage();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File("/storage/emulated/0/test1.txt");
                if(file.exists()) {
                    Log.d("mySuperTag", "yesItExist");
                    try {
                        FileInputStream is = new FileInputStream(file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        String line = reader.readLine();
                        while(line != null) {
                            Log.d("mySuperTagEdit", line);
                            openDialog(line);
                            line = reader.readLine();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("mySuperTag", "No It doesn't bitch");
                }
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView txtView = (TextView)findViewById(R.id.hellotxt);
                TextView conView = (TextView)findViewById(R.id.contact_plus);
                InputStream inputStream = getResources().openRawResource(R.raw.testy);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int i;
                try {
                    i = inputStream.read();
                    while (i != -1)
                    {
                        byteArrayOutputStream.write(i);
                        i = inputStream.read();
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                txtView.setText(byteArrayOutputStream.toString());
                lines = byteArrayOutputStream.toString().split("\\r?\\n");
                contact_size = lines.length;
                conView.setText(String.valueOf("Contact Length: " + contact_size));
            }
        });

    }

    private void initLogging() {
        Log.setDebug(true);
        Log.d("mySuperTag", "hi");
        Log.setPath("messenger_log.txt");
        Log.setLogListener(new OnLogListener() {
            @Override
            public void onLogged(String tag, String message) {
                logAdapter.addItem(tag + ": " + message);
            }
        });
    }

    private void setDefaultSmsApp() {
        setDefaultAppButton.setVisibility(View.GONE);
        Intent intent =
                new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                getPackageName());
        startActivity(intent);
    }

    private void toggleSendImage() {
        if (imageToSend.isEnabled()) {
            imageToSend.setEnabled(false);
            imageToSend.setAlpha(0.3f);
        } else {
            imageToSend.setEnabled(true);
            imageToSend.setAlpha(1.0f);
        }
    }

    public void sendMessage(final String contacty) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
                sendSettings.setMmsc(settings.getMmsc());
                sendSettings.setProxy(settings.getMmsProxy());
                sendSettings.setPort(settings.getMmsPort());
                sendSettings.setUseSystemSending(true);

                Transaction transaction = new Transaction(MainActivity.this, sendSettings);
                Message message = new Message(messageField.getText().toString(), contacty, "김동호입니다");

                if (imageToSend.isEnabled()) {
                    Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.android1);//assign your bitmap;
                    Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),R.drawable.android2);//assign your bitmap;
                    Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.android3);
                    Bitmap[] arrayOfBitmap = {bitmap1, bitmap2, bitmap3};
//                    message.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.android));
                    message.setImages(arrayOfBitmap);
                }

                transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);

            }

        }).start();
    }
}
