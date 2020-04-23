package com.lawrence.sweeper;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.lawrence.sweeper.ui.home.GameCanvas;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private GameLogic gameLogic;
    Chronometer chronometer;
    boolean flagSet = false;
    boolean fabRight = true;

    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;
    boolean fromOrientation = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*preferencesEditor = preferences.edit();
        preferences = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        fromOrientation = preferences.getString("fromOrient", false);
*/
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Drawable tile = ContextCompat.getDrawable(getApplicationContext(), R.drawable.tile);
        final Drawable flag = ContextCompat.getDrawable(getApplicationContext(), R.drawable.flag);

        Button resetButton = findViewById(R.id.reset_button);
        final TextView bombCount = findViewById(R.id.bomb_count);
        chronometer = findViewById(R.id.timer);
        final FloatingActionButton fab = findViewById(R.id.fab);

        if(savedInstanceState != null){
            gameLogic = savedInstanceState.getParcelable("gameLogic");
            if(gameLogic!=null && gameLogic.gameOn()) {
                chronometer.setBase(savedInstanceState.getLong("chronoTime"));
                if (!gameLogic.isGameOver()) {
                    chronometer.start();
                }
            } else {
                gameLogic = new GameLogic(0);
            }

            flagSet = savedInstanceState.getBoolean("fab_icon");
            if(flagSet){
                fab.setImageDrawable(flag);
                gameLogic.changeLongPress(true);
            }

            fabRight = savedInstanceState.getBoolean("fab_side");
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            if (fabRight) {
                params.gravity = Gravity.BOTTOM | Gravity.END;
            } else {
                params.gravity = Gravity.BOTTOM | Gravity.START;
            }
            fab.setLayoutParams(params);
        } else {
            gameLogic = new GameLogic(0);
        }

        fab.setOnTouchListener(new View.OnTouchListener() {
            int moveX;
            int moveY;
            boolean moved = false;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int newX = (int)motionEvent.getX();
                int newY = (int)motionEvent.getY();
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        moveX = newX;
                        moveY = newY;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(newX - moveX) + Math.abs(newY - moveY) > 30 && !moved) {
                            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                            if (fabRight) {
                                params.gravity = Gravity.BOTTOM | Gravity.START;
                            } else {
                                params.gravity = Gravity.BOTTOM | Gravity.END;
                            }
                            moved = true;
                            fabRight = !fabRight;
                            fab.setLayoutParams(params);
                        }
                        return false;
                    case MotionEvent.ACTION_UP:
                        if (!moved && gameLogic!=null) {
                            if (flagSet) {
                                fab.setImageDrawable(tile);
                            } else {
                                fab.setImageDrawable(flag);
                            }
                            flagSet = !flagSet;
                            gameLogic.changeLongPress(flagSet);
                        }
                        moved = false;
                        return false;
                    default:
                        return true;
                }
            }

        });
        fab.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                return false;
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_game, R.id.nav_settings, R.id.nav_records)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        resetButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                resetGame();
            }
        });

        String str = String.format(Locale.getDefault(), "%03d", gameLogic.getFlaggedTiles());
        bombCount.setText(str);

        gameLogic.setGameLogicListener(new GameLogic.GameLogicListener() {
            @Override
            public void gameStarted(boolean started) {
                if(started) {
                    String str = String.format(Locale.getDefault(), "%03d", gameLogic.getFlaggedTiles());
                    bombCount.setText(str);
                    chronometer.start();
                    chronometer.setBase(SystemClock.elapsedRealtime());
                } else {
                    chronometer.stop();
                }
            }

            @Override
            public void gameOver(boolean win) {
                chronometer.stop();
                //Log.i("MAIN ACTIVITY: STOP TIME: ", ""+(SystemClock.elapsedRealtime() - chronometer.getBase()));

                long gameTime = SystemClock.elapsedRealtime() - chronometer.getBase();
                int difficulty = gameLogic.getDifficulty();

                CharSequence text = "You win!";
                if (!win) {
                    text = "You lost";
                }
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
                toast.show();
            }

            @Override
            public void tileFlagged(int flags) {
                String str = String.format(Locale.getDefault(), "%03d", flags);
                bombCount.setText(str);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_easy:
                gameLogic.setDifficulty(0);
                resetGame();
                return true;
            case R.id.action_medium:
                gameLogic.setDifficulty(1);
                resetGame();
                return true;
            case R.id.action_hard:
                gameLogic.setDifficulty(2);
                resetGame();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void onResume() {
        super.onResume();

    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public void resetGame(){
        GameCanvas model = findViewById(R.id.custom_canvas);
        if(model != null && gameLogic != null && chronometer != null) {
            chronometer.stop();
            gameLogic.reset();
            model.resetView();
            chronometer.setBase(SystemClock.elapsedRealtime());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("gameLogic", gameLogic);
        outState.putLong("chronoTime", chronometer.getBase());
        outState.putBoolean("fab_icon", flagSet);
        outState.putBoolean("fab_side", fabRight);
    }

    public void stopGame(){
        chronometer.stop();
    }

    /*@Override
    public Object onRetainCustomNonConfigurationInstance() {
        super.onRetainNonConfigurationInstance();
        preferencesEditor.putBoolean("fromOrient", true);
        preferencesEditor.commit();
        return null;
    }

    @Override
    protected void onDestroy() {
        if(fromOrientation) {
            preferencesEditor.putBoolean("fromOrient", false);
            preferencesEditor.commit();
        }
        super.onDestroy();
    }*/


}
