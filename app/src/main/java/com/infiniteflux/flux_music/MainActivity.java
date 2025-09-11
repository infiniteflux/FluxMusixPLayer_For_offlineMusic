package com.infiniteflux.flux_music;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listview;
    String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listview = findViewById(R.id.listviewSong);
        runtimePermission();
    }

    public void runtimePermission() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
        }
        // Always add RECORD_AUDIO for recording audio
        permissions.add(Manifest.permission.RECORD_AUDIO);


        // Check if permissions are already granted
        if (permissions.stream().allMatch(p -> ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED)) {
            displaySongs();
        } else {
            Dexter.withContext(this)
                    .withPermissions(permissions)
                    .withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                displaySongs();
                            } else {
                                Toast.makeText(MainActivity.this, "All permission needed!", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }

                            if (report.isAnyPermissionPermanentlyDenied()) {
                                Toast.makeText(MainActivity.this, "All means all !", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest(); // Continue the permission request
                        }
                    }).check();
        }
    }


    public ArrayList<File> findsong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();

        File[] files = file.listFiles();

        // Check for null to avoid NullPointerException
        if (files != null) {
            for (File singleFile : files) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(findsong(singleFile));
                } else {
                    // Filter for audio files
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                        arrayList.add(singleFile);
                    }
                }
            }
        }
        return arrayList;
    }

    void displaySongs() {
        // Use a valid path and ensure it exists
        File musicDir = Environment.getExternalStorageDirectory();
        if (musicDir.exists() && musicDir.canRead()) {
            final ArrayList<File> mySongs = findsong(musicDir);

            items = new String[mySongs.size()];
            for (int i = 0; i < mySongs.size(); i++) {
                items[i] = mySongs.get(i).getName().replace(".mp3", "").replace(".wav", "");
            }
            /*ArrayAdapter<String> myAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
            listview.setAdapter(myAdapter);*/

            custonAdapter custonAdapter= new custonAdapter();
            listview.setAdapter(custonAdapter);

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String songname=(String) listview.getItemAtPosition(i);
                    startActivity(new Intent(getApplicationContext(),PlayerActivity.class)
                            .putExtra("songs",mySongs)
                            .putExtra("songname",songname)
                            .putExtra("pos",i));
                }
            });
        } else {
            // Handle case where the directory isn't accessible
        }
    }

    class custonAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myview=getLayoutInflater().inflate(R.layout.list_item,null);
            TextView txtsong=myview.findViewById(R.id.txtsongname);
            txtsong.setSelected(true);
            txtsong.setText(items[i]);

            return myview;
        }
    }
}
