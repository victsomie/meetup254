package com.example.mmc.testfirebase.Activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewSingleItem extends AppCompatActivity implements View.OnClickListener,ExoPlayer.EventListener,
        PlaybackControlView.VisibilityListener{


    SharedPreferences prefs;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference().child(Constants.firebase_reference_video);

    //    VideoView playVideo;
    SimpleExoPlayerView playVideo;
    TextView titleTextView, videoviews;
    ListView listComments;
    EditText addComment;
    String FirebaseKey, username;
    ListIem SelectVideoObject;
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
                Map<String, Object> newPost = (Map<String, Object>) dataSnapshot.getValue();
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

}
