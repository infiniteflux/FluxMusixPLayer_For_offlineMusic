package com.infiniteflux.flux_music;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.chibde.visualizer.BarVisualizer;
import java.io.File;

import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    Button btnplay, btnnext, btnprev, btnff, btnfr;
    TextView txtsname, txtsstart,txtsstop;
    SeekBar seekbar;
    ImageView imageview;
    BarVisualizer barVisualizer;

    String sname;
    public static final String EXTRA_NAME="song_name";
    static MediaPlayer mediaplayer;
    int position;
    ArrayList<File> mySong;
    Thread updateseekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(barVisualizer != null){
            barVisualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnprev=findViewById(R.id.btnprev);
        btnnext=findViewById(R.id.btnnext);
        btnplay=findViewById(R.id.playbtn);
        btnff=findViewById(R.id.btnff);
        btnfr=findViewById(R.id.btnfr);
        txtsname=findViewById(R.id.txtsn);
        txtsstart=findViewById(R.id.txtsstart);
        txtsstop=findViewById(R.id.txtsstop);
        seekbar= findViewById(R.id.seekbar);
        imageview=findViewById(R.id.imageview);
        barVisualizer = findViewById(R.id.blast1);

        if(mediaplayer != null){
            mediaplayer.stop();
            mediaplayer.release();
        }

        Intent i=getIntent();
        Bundle bundle =i.getExtras();


        mySong= (ArrayList) bundle.getParcelableArrayList("songs");
        String songName=i.getStringExtra("songname");
        position= bundle.getInt("pos",0);
        txtsname.setSelected(true);
        Uri uri=Uri.parse(mySong.get(position).toString());
        sname=mySong.get(position).getName();
        txtsname.setText(sname);

        mediaplayer=MediaPlayer.create(getApplicationContext(),uri);
        mediaplayer.start();

        updateseekbar = new Thread(){
            @Override
            public void run(){
                int totalduration = mediaplayer.getDuration();
                int currentposition =0;

                while(currentposition < totalduration){
                    try {
                        sleep(500);
                        currentposition=mediaplayer.getCurrentPosition();
                        seekbar.setProgress(currentposition);
                    }
                    catch (InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        seekbar.setMax(mediaplayer.getDuration());
        updateseekbar.start();
        seekbar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.MULTIPLY);
        seekbar.getThumb().setColorFilter(getResources().getColor(R.color.primary),PorterDuff.Mode.SRC_IN);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaplayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime =createTime(mediaplayer.getDuration());
        txtsstop.setText(endTime);

        final Handler handler = new Handler();
        final  int delay=1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currenttime=createTime(mediaplayer.getCurrentPosition());
                txtsstart.setText(currenttime);
                handler.postDelayed(this,delay);
            }
        },delay);

        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaplayer.isPlaying()){
                    btnplay.setBackgroundResource(R.drawable.ic_play);
                    mediaplayer.pause();
                }
                else{
                    btnplay.setBackgroundResource(R.drawable.ic_pause);
                    mediaplayer.start();
                }
            }
        });
        //next listner

        mediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnnext.performClick();
            }
        });

 //       from where i learn
//        int audiosessionid = mediaplayer.getAudioSessionId();

//        if(audiosessionid != -1) {
//            barVisualizer.setAudioSessionId(audiosessionid);
//        }

//        for my reference
//        int audioSessionId = mediaplayer.getAudioSessionId();
//        if (audioSessionId != AudioEffect.ERROR_BAD_VALUE) {
//            BarVisualizer barVisualizer = findViewById(R.id.blast1);
//            barVisualizer.setColor(ContextCompat.getColor(this, R.color.primary));
//            barVisualizer.setDensity(70);
//            barVisualizer.setPlayer(audioSessionId);
//        }

        int audiosessionid = mediaplayer.getAudioSessionId();
        if (audiosessionid != -1) {
            barVisualizer.setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));
            barVisualizer.setDensity(70);
            barVisualizer.setPlayer(audiosessionid);
        }


        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaplayer.stop();
                mediaplayer.release();
                position=((position+1)%mySong.size());
                Uri u=Uri.parse(mySong.get(position).toString());
                mediaplayer = MediaPlayer.create(getApplicationContext(),u);
                sname=mySong.get(position).getName();
                txtsname.setText(sname);
                mediaplayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                startAnimmation(imageview);

                int audiosessionid = mediaplayer.getAudioSessionId();
                if (audiosessionid != -1) {
                    barVisualizer.setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));
                    barVisualizer.setDensity(70);
                    barVisualizer.setPlayer(audiosessionid);
                }


            }
        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaplayer.stop();
                mediaplayer.release();
                position = ((position-1)<0)?(mySong.size()-1):(position-1);
                Uri u=Uri.parse(mySong.get(position).toString());
                mediaplayer = MediaPlayer.create(getApplicationContext(),u);
                sname=mySong.get(position).getName();
                txtsname.setText(sname);
                mediaplayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                startAnimmation(imageview);

                int audiosessionid = mediaplayer.getAudioSessionId();
                if (audiosessionid != -1) {
                    barVisualizer.setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary));
                    barVisualizer.setDensity(70);
                    barVisualizer.setPlayer(audiosessionid);
                }

            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaplayer.isPlaying()){
                    mediaplayer.seekTo(mediaplayer.getCurrentPosition()+10000);
                }
            }
        });

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaplayer.isPlaying()){
                    mediaplayer.seekTo(mediaplayer.getCurrentPosition()-10000);
                }
            }
        });

    }
    public void startAnimmation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageview,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration){
        String time="";
        int min=duration /1000 /60;
        int sec=duration/1000 %60;

        time+=min+":";

        if(sec<10){
            time+="0";
        }
        time+=sec;

        return time;
    }
}