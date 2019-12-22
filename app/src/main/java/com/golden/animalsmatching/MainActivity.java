package com.golden.animalsmatching;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout photosGrid;
    private int level;
    private ImageView clickedImageView;
    List<String> randomAnimals;
    List<String> hiddenAnimals;
    int clickedImageIndex;
    int remainingMatches;
    int imagePoints;
    int score;
    boolean isPaused;
    MediaPlayer mPlayer;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        makeFullScreen();
        setContentView(R.layout.activity_main);
        photosGrid = findViewById(R.id.photos_grid);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                level = intent.getIntExtra("level", 0);
            } else {
                level = 0;
            }

            generateRandomAnimals();
            adjustScore();
        } else {
            restoreState(savedInstanceState);
            if (randomAnimals.size() == (hiddenAnimals.size()*2)) {
                showScore();
            }
            adjustScore(false);
        }
        displayPhotos();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (randomAnimals == null)
            return;

        outState.putInt("level", level);
        outState.putStringArrayList("random_animals", (ArrayList<String>) randomAnimals);
        outState.putStringArrayList("hidden_animals", (ArrayList<String>) hiddenAnimals);
        outState.putInt("clicked_index", clickedImageIndex);
        outState.putInt("remaining_matches", remainingMatches);
        outState.putInt("image_points", imagePoints);
        outState.putInt("score", score);
    }

    private void restoreState(Bundle savedInstanceState) {
        level = savedInstanceState.getInt("level");
        randomAnimals = savedInstanceState.getStringArrayList("random_animals");
        hiddenAnimals = savedInstanceState.getStringArrayList("hidden_animals");
        clickedImageIndex = savedInstanceState.getInt("clicked_index");
        remainingMatches = savedInstanceState.getInt("remaining_matches");
        imagePoints = savedInstanceState.getInt("image_points");
        score = savedInstanceState.getInt("score");
    }

    private void makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
             WindowManager.LayoutParams.FLAG_FULLSCREEN); //show the activity in full screen
    }

    private void displayPhotos() {
        photosGrid.removeAllViews();
        int noOfCells = getNoOfCells();
        int columns = noOfCells > 10? 3: 2;
        int rows = noOfCells/columns;
        int middleRow = rows/2;
        int middleCol = columns/2;
        photosGrid.setOrientation(isVertical()? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);

        int i = 0;
        for (int r = 0; r < rows; ++r) {
            LinearLayout rowLayout = createRow();
            for (int c = 0; c < columns; ++c) {
                if ((noOfCells%2 == 1) && r == middleRow && c == middleCol) {
                    createEmptyView(rowLayout);
                } else {
                    ImageView imageView = createImageView(rowLayout, i);
                    if (hiddenAnimals.contains(randomAnimals.get(i)))
                        imageView.setVisibility(View.INVISIBLE);
                    if (i == clickedImageIndex) {
                        clickedImageView = imageView;
                        imageView.setImageResource(getImageResource(randomAnimals.get(i)));
                    }
                    ++i;
                }
            }
        }
    }

    private void generateRandomAnimals() {
        List<String> animals = new ArrayList<>();
        List<String> chosenAnimals = new ArrayList<>();
        Collections.addAll(animals, getResources().getStringArray(R.array.animals));
        int noOfPhotos = (getNoOfCells()/2) * 2;
        randomAnimals = new ArrayList<>();
        hiddenAnimals = new ArrayList<>();
        clickedImageIndex = -1;
        remainingMatches = noOfPhotos/2;
        List<Boolean> isAnimalChosen = new ArrayList<>(Collections.nCopies(animals.size(), false));

        Random rand = new Random();
        for (int i = 0; i < noOfPhotos/2; ++i) {
            int randIndex = rand.nextInt(animals.size());
            chosenAnimals.add(animals.get(randIndex));
            animals.remove(randIndex);
        }

        for (int i = 0; i < noOfPhotos; ++i) {
            int randIndex = rand.nextInt(chosenAnimals.size());
            randomAnimals.add(chosenAnimals.get(randIndex));
            if (isAnimalChosen.get(randIndex)) {
                isAnimalChosen.remove(randIndex);
                chosenAnimals.remove(randIndex);
            } else {
                isAnimalChosen.set(randIndex, true);
            }
        }
    }

    private LinearLayout createRow() {
        boolean isVertical = isVertical();
        LinearLayout rowLayout = new LinearLayout(this);
        int rowWidth = isVertical? ViewGroup.LayoutParams.MATCH_PARENT : 0;
        int rowHeight = isVertical? 0 : ViewGroup.LayoutParams.MATCH_PARENT;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(rowWidth, rowHeight);
        layoutParams.weight = 1;
        rowLayout.setOrientation(isVertical? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

        final float scale = this.getResources().getDisplayMetrics().density;
        final int padding = (int)(5 * scale + .5f);
        rowLayout.setPadding(padding, padding, padding, padding);

        photosGrid.addView(rowLayout);
        rowLayout.setLayoutParams(layoutParams);
        return rowLayout;
    }

    private ImageView createImageView(LinearLayout rowLayout, int imageIndex) {
        boolean isVertical = isVertical();
        ImageView imageView = new ImageView(this);
        int imgWidth = isVertical? 0 : ViewGroup.LayoutParams.MATCH_PARENT;
        int imgHeight = isVertical? ViewGroup.LayoutParams.MATCH_PARENT : 0;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(imgWidth, imgHeight);
        layoutParams.weight = 1;

        final float scale = this.getResources().getDisplayMetrics().density;
        final int margins = (int)(5 * scale + .5f);
        layoutParams.setMargins(margins, margins, margins, margins);

        imageView.setImageResource(R.drawable.question_mark);
        imageView.setBackgroundColor(Color.parseColor("#D0D0D0"));
        imageView.setOnClickListener(this);
        imageView.setTag(imageIndex);

        rowLayout.addView(imageView);
        imageView.setLayoutParams(layoutParams);
        return imageView;
    }

    private View createEmptyView(LinearLayout rowLayout) {
        boolean isVertical = isVertical();
        View view = new View(this);
        int viewWidth = isVertical? 0 : ViewGroup.LayoutParams.MATCH_PARENT;
        int viewHeight = isVertical? ViewGroup.LayoutParams.MATCH_PARENT : 0;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(viewWidth, viewHeight);
        layoutParams.weight = 1;

        final float scale = this.getResources().getDisplayMetrics().density;
        final int margins = (int)(5 * scale + .5f);
        layoutParams.setMargins(margins, margins, margins, margins);

        rowLayout.addView(view);
        view.setLayoutParams(layoutParams);
        return view;
    }

    private boolean isVertical() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (metrics.heightPixels > metrics.widthPixels);
    }

    private int getNoOfCells() {
        switch (level) {
            case 0:
                return 4;
            case 1:
                return 6;
            case 2:
                return 10;
            case 3:
                return 12;
            case 4:
                return 15;
            case 5:
                return 18;
            default:
                return 21;
        }
    }

    private int getImageResource(String imageName) {
        return getResources().getIdentifier(imageName, "drawable", getPackageName());
    }

    private int getAudioResource(String audioName) {
        return getResources().getIdentifier(audioName, "raw", getPackageName());
    }

    private void playAudioFile(String audioName) {
        playAudioFile(getAudioResource(audioName));
    }

    private void playAudioFile(int audioRes) {
        if (mPlayer != null) {
            mPlayer.stop();
        }
        mPlayer = MediaPlayer.create(this, audioRes);
        mPlayer.start();
    }

    private void adjustScore() {
        adjustScore(true);
    }

    private void adjustScore(boolean resetScore) {
        if (resetScore) {
            score = 0;
            imagePoints = 10;
        }

        if (handler == null)
            handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    --imagePoints;
                }
                if (imagePoints > 1)
                    handler.postDelayed(this, 10000);
            }
        }, 10000);
    }

    private void showScore() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Level Finished");

        builder.setMessage("You get score: " + score + "\nDo you want go to next level ?")
                .setCancelable(false)
                .setPositiveButton("Let's Go", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (level < 6) {
                            ++level;
                            generateRandomAnimals();
                            adjustScore();
                            displayPhotos();
                        } else {
                            finish();
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        final ImageView imageView = (ImageView)v;
        int index = (int)imageView.getTag();
        final String imageName = randomAnimals.get(index);

        imageView.setImageResource(getImageResource(imageName));
        if (clickedImageIndex == -1) {
            clickedImageView = imageView;
            clickedImageIndex = index;
        } else if (index != clickedImageIndex) {
            final boolean matched = imageName.equals(randomAnimals.get(clickedImageIndex));
            if (matched) {
                --remainingMatches;
                hiddenAnimals.add(imageName);
                playAudioFile(imageName);
                score += imagePoints;
            }
            final ImageView tmpClickedImage = clickedImageView;
            final int tmpRemainingMatches = remainingMatches;
            clickedImageIndex = -1;
            clickedImageView = null;

            if (handler == null)
                handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (matched) {
                        tmpClickedImage.setVisibility(View.INVISIBLE);
                        imageView.setVisibility(View.INVISIBLE);
                    } else {
                        tmpClickedImage.setImageResource(R.drawable.question_mark);
                        imageView.setImageResource(R.drawable.question_mark);
                    }

                    if (tmpRemainingMatches == 0) {
                        showScore();
                    }
                }
            }, 300);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }
}
