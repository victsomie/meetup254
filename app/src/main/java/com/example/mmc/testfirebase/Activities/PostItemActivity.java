package com.example.mmc.testfirebase.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmc.testfirebase.Constants;
import com.example.mmc.testfirebase.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class PostItemActivity extends AppCompatActivity implements View.OnClickListener,ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener {


    SharedPreferences prefs ;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference().child(Constants.firebase_reference_video);

    FirebaseStorage storage = FirebaseStorage.getInstance();

    //StorageReference storageRef = storage.getReferenceFromUrl(Constants.firebase_storage);
    StorageReference storageRef = storage.getReference();

    SimpleExoPlayer player;


    ImageView promptupload;


    TextView VideoTitle;
    Button btnpost;
    public static final int REQUEST_TAKE_GALLERY_VIDEO = 0;

    String selectedImagePath;


    String filemanagerstring;
    String user;


    SimpleExoPlayerView touploadvideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_item);getUser();

        prefs = getApplication().getSharedPreferences(Constants.shared_preference, 0);
        promptupload = (ImageView)findViewById(R.id.uploadprompt);
        touploadvideo=(SimpleExoPlayerView) findViewById(R.id.postvideoView);
        touploadvideo.setControllerVisibilityListener(this);
        touploadvideo.requestFocus();






// 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

// 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

// 3. Create the player
        player =
                ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);

// Bind the player to the view.
        touploadvideo.setPlayer(player);



        VideoTitle=(TextView)findViewById(R.id.postvideotitle);
        btnpost=(Button)findViewById(R.id.post);
        promptupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);

            }
        });


        touploadvideo.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);
            }
        });

        btnpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String, Object> fillData =new HashMap<String, Object>();
                fillData.put(Constants.firebase_reference_video_title,VideoTitle.getText().toString());
                // fillData.put(Constants.firebase_reference_video_path,prefs.getString(Constants.firebase_reference_video_path,null));
                fillData.put(Constants.firebase_reference_video_uploader,user=prefs.getString(Constants.firebase_reference_user_username,null));
                fillData.put(Constants.firebase_reference_video_views,String.valueOf(0));
                myRef.push().setValue(fillData);
                Toast.makeText(getApplicationContext(),"Vlog Sucessfully Uploaded",Toast.LENGTH_SHORT).show();
                prefs.edit().remove(Constants.firebase_reference_video_path).commit();


                Intent i=new Intent(getApplicationContext(),ViewListVLogs.class);
                startActivity(i);
                finish();


            }
        });

    }


    private void getUser() {
        try{
            user=prefs.getString(Constants.firebase_reference_user_username,null);
        }catch (Exception IDGAF){

        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                touploadvideo.setVisibility(View.VISIBLE);
                promptupload.setVisibility(View.GONE);
                Uri selectedImageUri = data.getData();

//                // OI FILE Manager
//                filemanagerstring = selectedImageUri.getPath();
//                Uri x=Uri.parse(filemanagerstring);
//                prefs.edit().putString(Constants.firebase_reference_video_path,
//                        filemanagerstring).commit();
//
//
//               setMediasource(x);
//                prefs.edit().putString(Constants.firebase_reference_video_path,
//                        filemanagerstring).commit();





                // MEDIA GALLERY
                selectedImagePath = getPath(selectedImageUri);
                if (selectedImagePath != null) {

//                    touploadvideo.setVideoURI(Uri.parse(selectedImagePath));
//                    touploadvideo.seekTo(100);
                    setMediasource(Uri.parse(selectedImagePath));




                    // do background work here
                    //saving storage

                    StorageReference riversRef = storageRef.child("Vlogs"+selectedImageUri.getLastPathSegment());

                    UploadTask uploadTask;

                    uploadTask = riversRef.putFile(selectedImageUri);

                    // Register observers to listen for when the download is done or if it fails
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            prefs.edit().putString(Constants.firebase_reference_video_path,
                                    String.valueOf(downloadUrl)).commit();





                        }
                    });




                }
            }
        }
    }

    private void setMediasource(Uri x) {
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
// Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "Vlog"), bandwidthMeter);
// Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
// This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(x,
                dataSourceFactory, extractorsFactory, null, null);
// Prepare the player with the source.
        player.prepare(videoSource);
    }

    // UPDATED!
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onVisibilityChange(int visibility) {

    }
}
