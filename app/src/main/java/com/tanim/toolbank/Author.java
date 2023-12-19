package com.tanim.toolbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Author extends AppCompatActivity {

    LinearLayout facebook;
    LinearLayout github;
    LinearLayout website;
    LinearLayout address;
    LinearLayout email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author);
        facebook = findViewById(R.id.facebookButton);
        github = findViewById(R.id.githubButton);
        website = findViewById(R.id.websiteButton);
        address = findViewById(R.id.authorAddress);
        email= findViewById(R.id.authorMail);

        facebook.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com/tanim494"));
            startActivity(intent);
        });

        github.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tanim494"));
            startActivity(intent);
        });

        website.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://tanim.codes"));
            startActivity(intent);
        });

        address.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.app.goo.gl/KimSYJ3GMz9F6QfMA"));
            startActivity(intent);
        });

        email.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822"); // Set the MIME type for email

            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"Tanim494@gmail.com"}); // Replace with the recipient's email address
            intent.putExtra(Intent.EXTRA_SUBJECT, "Request Feature or Suggestion for Tool Bank"); // Set the email subject
            intent.putExtra(Intent.EXTRA_TEXT, "I have a suggestion for Tool Bank. \n ..."); // Set the email body

            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (ActivityNotFoundException e) {
                // Handle the case where no email client is installed on the device
                Toast.makeText(Author.this, "No App", Toast.LENGTH_SHORT).show();
            }
        });

    }
}