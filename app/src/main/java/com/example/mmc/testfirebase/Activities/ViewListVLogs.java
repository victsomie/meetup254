package com.example.mmc.testfirebase.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mmc.testfirebase.Adapters.ListItemsRecyclerAdapter;
import com.example.mmc.testfirebase.Constants;
import com.example.mmc.testfirebase.Objects.ListIem;
import com.example.mmc.testfirebase.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by MMc on 2/22/2017.
 */
public class ViewListVLogs extends AppCompatActivity {

    SharedPreferences prefs;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference().child(Constants.firebase_reference_video);
    RecyclerView listVlogs;
    List<ListIem> listVideos;
    ListItemsRecyclerAdapter adapter;
    NestedScrollView coordinatorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list_vlogs);

        coordinatorLayout = (NestedScrollView) findViewById(R.id
                .forthesnackbar);

        listVideos = new ArrayList<ListIem>();

        listVlogs = (RecyclerView) findViewById(R.id.recyclelistVideos);
        listVlogs.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        adapter = new ListItemsRecyclerAdapter(getApplicationContext(), listVideos, myRef);
        listVlogs.setAdapter(adapter);
        getalldata();

        // ========= TODO ========
        // Uncomment the below method
        // getOverflowMenu();


        // You will add floating action button here
        // ========== TODO =========
    }

    // Method to get all the data from child event listener
    private void getalldata() {
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String s) {
                Map<String, Object> newPost = (Map<String, Object>) snapshot.getValue();
                if (newPost != null) {
                    listVideos.add(new ListIem(
                            newPost.get(Constants.firebase_reference_video_title).toString(),
                            // newPost.get(Constants.firebase_reference_video_path).toString(),
                            newPost.get(Constants.firebase_reference_video_uploader).toString(),
                            newPost.get(Constants.firebase_reference_video_views).toString(),
                            snapshot.getKey()
                    ));


                    listVlogs.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
                    adapter = new ListItemsRecyclerAdapter(getApplicationContext(), listVideos, myRef);
                    listVlogs.setAdapter(adapter);

                    adapter.notifyDataSetChanged();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_post_new_video) {
            // Go to post a video activity
            Intent i = new Intent(getApplicationContext(), PostItemActivity.class);
            startActivity(i);
            finish();
        }
        if (id == R.id.action_logout) {
            //Logout a user and go back to login activity
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            finish();

        }
        return super.onOptionsItemSelected(item);

    }

    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /**
        if (exit) {
            finish();
        } else {
            Toast.makeText(this, "Click exit again to close", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
         **/
    }


}

