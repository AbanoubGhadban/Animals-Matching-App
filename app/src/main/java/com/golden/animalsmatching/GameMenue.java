package com.golden.animalsmatching;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GameMenue extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menue);
    }

    public void onStartClicked(View view) {
        String levelStr = ((Button)view).getText().toString();
        int level = Integer.parseInt(levelStr.substring(levelStr.length() - 1));
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("level", level);
        startActivity(intent);
    }
}
