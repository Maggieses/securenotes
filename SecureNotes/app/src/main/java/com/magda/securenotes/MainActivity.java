package com.magda.securenotes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.magda.securenotes.crypto.CryptoUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    private byte[] encodedNotes;
    private byte[] encodedPassword;

    private Button encodeBtn;
    private Button decodeBtn;
    private EditText notesView;
    private EditText password;
    private TextView passwordTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();

    }

    private void initListeners() {
        notesView.setMovementMethod(new ScrollingMovementMethod());

        encodeBtn.setOnClickListener((View v) -> {
            final String passwordValue = password.getText().toString().trim();
            if (passwordValue.length() < 8) {
                setPasswordTip(R.string.password_to_short, Color.RED);
            } else {
                try {
                    System.out.println("Encoding password");
                    SecretKey secretKey = CryptoUtils.generateKey(passwordValue);
                    final String notesValue = notesView.getText().toString().trim();
                    final byte[] encodedNotes = CryptoUtils.encryptMsg(notesValue, secretKey);
                    savePassword(secretKey.getEncoded());
                    saveNotes(encodedNotes);
                    notesView.setText("");
                    setPasswordTip(R.string.password_tip, Color.BLACK);
                    password.setText("");
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    System.out.println(e.getMessage());
                    setPasswordTip(R.string.password_sth_wrong, Color.RED);
                } catch (InvalidKeyException | NoSuchPaddingException | BadPaddingException | UnsupportedEncodingException | IllegalBlockSizeException e) {
                    System.out.println(e.getMessage());
                    setPasswordTip(R.string.cant_encode, Color.RED);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        decodeBtn.setOnClickListener((View v) -> {
            final String passwordValue = password.getText().toString().trim();
            if (passwordValue.length() < 8) {
                setPasswordTip(R.string.password_to_short, Color.RED);
            } else {
                System.out.println("Decoding password");
                try {
                    byte[] notes = loadNotes();
                    byte[] previousPassword = loadPassword();
                    SecretKey secretKey = CryptoUtils.generateKey(passwordValue);
                    byte[] currentPassword = secretKey.getEncoded();
                    if (previousPassword.length != currentPassword.length) {
                        setPasswordTip(R.string.wrong_password, Color.RED);
                        return;
                    } else {
                        for (int i = 0; i < previousPassword.length; i++) {
                            if (previousPassword[i] != currentPassword[i]) {
                                setPasswordTip(R.string.wrong_password, Color.RED);
                                return;
                            }
                        }
                    }
                    final String notesValue = CryptoUtils.decryptMsg(notes, secretKey);
                    notesView.setText(notesValue);
                    password.setText("");
                    setPasswordTip(R.string.password_tip, Color.BLACK);
                } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                    System.out.println(e.getMessage());
                    setPasswordTip(R.string.password_sth_wrong, Color.RED);
                } catch (NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void saveNotes(byte[] notes) throws IOException {
        this.encodedNotes = notes;
    }

    private byte[] loadNotes() throws IOException {
        return this.encodedNotes;
    }

    private void savePassword(byte[] password) throws IOException {
        this.encodedPassword = password;
    }

    private byte[] loadPassword() throws IOException {
        return this.encodedPassword;
    }

    private void setPasswordTip(final int message, final int color) {
        passwordTip.setText(message);
        passwordTip.setTextColor(color);
    }

    private void initViews() {
        encodeBtn = findViewById(R.id.encodeBtn);
        decodeBtn = findViewById(R.id.decodeBtn);
        notesView = findViewById(R.id.notes);
        password = findViewById(R.id.password);
        passwordTip = findViewById(R.id.password_tip);
    }

}
