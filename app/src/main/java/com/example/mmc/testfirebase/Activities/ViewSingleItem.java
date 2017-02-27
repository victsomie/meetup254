package com.example.mmc.testfirebase.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmc.testfirebase.Constants;
import com.example.mmc.testfirebase.Objects.Comments;
import com.example.mmc.testfirebase.Objects.ListIem;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewSingleItem extends AppCompatActivity implements View.OnClickListener, ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener {

    final String TAG = "ViewSingleItem";

    long numOfReportedVideos;
    long numOfDeletedVideos;

    SharedPreferences prefs;
    FirebaseDatabase database = FirebaseDatabase.getInstance();


    DatabaseReference myRef = database.getReference().child(Constants.firebase_reference_video);

    DatabaseReference mainRef = database.getReference();

    // Reference what to delete
    DatabaseReference videosReportedRef = database.getReference().child(Constants.firebase_reference_videos_reported);
    DatabaseReference videosDeletedRef = database.getReference().child(Constants.firebase_reference_videos_deleted);


    //    VideoView playVideo;
    SimpleExoPlayerView playVideo;
    TextView titleTextView, videoviews;
    ListView listComments;
    EditText addComment;
    String FirebaseKey, username;
    ListIem SelectVideoObject;

    Map<String, Object> newPost; // Map of the single video detail we are viewing. WIll be retrieved as a map from DB
    Comments commentobject;
    List<String> commentslazycount;
    SimpleExoPlayer player;

    ArrayAdapter<String> itemsAdapter;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_item);


        prefs = getApplication().getSharedPreferences(Constants.shared_preference, 0);
        FirebaseKey = getIntent().getExtras().getString(Constants.extras_firekeyreference);
        username = prefs.getString(Constants.firebase_reference_user_username, null);

//        playVideo=(VideoView)findViewById(R.id.viewsingleVlog);

        playVideo = (SimpleExoPlayerView) findViewById(R.id.viewsingleVlog);
        // Sets the callback to this Activity, since it inherits EasyVideoCallback
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
        playVideo.setPlayer(player);


        titleTextView = (TextView) findViewById(R.id.viewsingleVlogTitle);
        videoviews = (TextView) findViewById(R.id.viewsingleVlogViews);
        listComments = (ListView) findViewById(R.id.comments_listView);
        addComment = (EditText) findViewById(R.id.chat_editText);
        commentslazycount = new ArrayList<String>();


        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, commentslazycount);
        listComments.setAdapter(itemsAdapter);


        pDialog = new ProgressDialog(this);

        // Set progressbar message
        pDialog.setMessage("Buffering...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);


        fillFiredata();
        fillchatdata();


        addComment.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    String comment = addComment.getText().toString();
                    commentobject = new Comments(username, comment);

                    addComment.setText("");
                    myRef.child(FirebaseKey).child(Constants.firebase_reference_video_comments).push().setValue(commentobject);
                    return true;
                }

                return false;
            }
        });

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

    private void fillFiredata() {
        FirebaseKey = getIntent().getExtras().getString(Constants.extras_firekeyreference);
        myRef.orderByKey().equalTo(FirebaseKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
                newPost = (Map<String, Object>) dataSnapshot.getValue(); // the map of the file being viewed
                if (newPost != null) {
                    SelectVideoObject = new ListIem(
                            newPost.get(Constants.firebase_reference_video_title).toString(),
                            newPost.get(Constants.firebase_reference_video_path).toString(),
                            newPost.get(Constants.firebase_reference_video_uploader).toString(),
                            newPost.get(Constants.firebase_reference_video_views).toString(),
                            dataSnapshot.getKey());

                    // ==== FILL IN THE UI DATA of the single item =====
                    // videoviews.setText(SelectVideoObject.getViews() + "Views");
                    videoviews.setText(SelectVideoObject.getViews() + " Views"); // FirebaseKey is the key that fired this activity
                    titleTextView.setText(SelectVideoObject.getTitle());
                    setMediasource(Uri.parse(SelectVideoObject.getPath()));

                }


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Reading the commentings
    private void fillchatdata() {
        FirebaseKey = getIntent().getExtras().getString(Constants.extras_firekeyreference);
        // THE COMMENTING LINE SHOULD GO TO THE CORRECT VIDEO
        // myRef.child(FirebaseKey).child(Constants.firebase_reference_video_comments).addChildEventListener(new ChildEventListener() {
        myRef.child(FirebaseKey).child(Constants.firebase_reference_video_comments).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
                if (newPost != null) {
                    String whatineed = newPost.get(Constants.firebase_reference_video_comments_username).toString()
                            + ":" +
                            newPost.get(Constants.firebase_reference_video_comments_comment).toString();
                    // commentslazycount.add(whatineed); // adds comment as the last in the comments list
                    commentslazycount.add(0, whatineed);
                    itemsAdapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single_item, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        //numOfDeletedVideos = 0;

        if (menuId == R.id.menu_item_delete_video) {
            // Add video do delete
            Toast.makeText(this, "You want to delete video : " + FirebaseKey, Toast.LENGTH_SHORT).show();
            videosDeletedRef.push().setValue(FirebaseKey);

            mainRef.addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getKey() == Constants.firebase_reference_videos_deleted){
                        Log.i(TAG, "onDataChange: +++***bhrrrr***+++" + dataSnapshot.getKey() + " ++++>> " + dataSnapshot.getChildrenCount());
                        numOfDeletedVideos = dataSnapshot.getChildrenCount();
                        Toast.makeText(ViewSingleItem.this, "Videos to delete : " + numOfDeletedVideos, Toast.LENGTH_SHORT).show();
                    }

                    for (DataSnapshot snap: dataSnapshot.getChildren()) {
                        /*
                        numOfDeletedVideos = snap.getChildrenCount();
                        //Toast.makeText(ViewSingleItem.this, numOfDeletedVideos + " videos to be deleted!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "onDataChange: " + snap.getKey() + " >> " + numOfDeletedVideos);
                        */
                        if (snap.getKey() == Constants.firebase_reference_videos_deleted){
                            Log.i(TAG, "^^^^^^^^^^^^^^^^ " + snap.getChildrenCount() + " is the number! ^^^^^^^^^^^");
                            Log.i(TAG, "onDataChange: ++++++++" + snap.getKey() + " ++++++>> " + snap.getChildrenCount());
                            break;
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // User alertDialog to confirm deleting
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Video will be deleted")
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // ======TODO: ensure real video deleting is possible
                            // After deleting go back to the list of videos

                            // myRef.child(FirebaseKey).removeValue();
                            Log.e(TAG, "OK BUTTON will delete" + myRef.child(FirebaseKey).getKey());


                            // ====== Currently its just UI =====
                            Intent backToVideosList = new Intent(ViewSingleItem.this, ViewListVLogs.class);
                            startActivity(backToVideosList);

                            // Toast what you have done
                            Toast.makeText(ViewSingleItem.this, "Work on this later", Toast.LENGTH_SHORT).show();


                        }
                    });
            builder.create().show();
        }
        if (menuId == R.id.menu_item_report_video) {

            // Add video do delete
            Toast.makeText(this, "Reporting video : " + FirebaseKey, Toast.LENGTH_SHORT).show();
            videosReportedRef.push().setValue(FirebaseKey);

            videosReportedRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    numOfReportedVideos = dataSnapshot.getChildrenCount();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            Toast.makeText(this, numOfReportedVideos + " videos REPORTED!", Toast.LENGTH_SHORT).show();
        }


        return super.onOptionsItemSelected(item);
    }

}
