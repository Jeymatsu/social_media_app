package com.company.scoolsocialmedia.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.company.scoolsocialmedia.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // Get the video URL from the intent
        videoUrl = getIntent().getStringExtra("videoUrl");

        // Find the VideoView in the layout
        videoView = findViewById(R.id.videoView);

        // Set the video URI
        videoView.setVideoURI(Uri.parse(videoUrl));

        // Create MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);

        // Set MediaController to VideoView
        videoView.setMediaController(mediaController);

        // Start playing the video
        videoView.start();

        // Setup save button click listener
        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVideo();
            }
        });
    }

    private void saveVideo() {
        // Implement logic to save the video
        // For example, you can use DownloadManager to download the video to the device's external storage
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(videoUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Video");
        request.setDescription("Downloading");
        request.setDestinationInExternalFilesDir(VideoPlayerActivity.this, Environment.DIRECTORY_MOVIES, "video.mp4");

        downloadManager.enqueue(request);

        Toast.makeText(VideoPlayerActivity.this, "Video saved", Toast.LENGTH_SHORT).show();
    }
}