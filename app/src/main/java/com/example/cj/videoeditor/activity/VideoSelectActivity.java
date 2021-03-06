package com.example.cj.videoeditor.activity;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cj.videoeditor.R;
import com.example.cj.videoeditor.adapter.VideoAdapter;

import java.io.IOException;

/**
 * Created by cj on 2017/10/16.
 * desc: local video select activity
 */

public class VideoSelectActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,VideoAdapter.OnVideoSelectListener {
    ImageView ivClose;
    GridView gridview;
    public static final String PROJECT_VIDEO = MediaStore.MediaColumns._ID;
    private VideoAdapter mVideoAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_select);
        initView();
        initData();
    }

    private void initView() {
        ivClose= (ImageView) findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        gridview=(GridView)findViewById(R.id.gridview_media_video);
    }
    private void initData() {
        getLoaderManager().initLoader(0,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String order = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        return new CursorLoader(getApplicationContext(), videoUri, new String[]{MediaStore.Video.Media.DATA, PROJECT_VIDEO}, null, null, order);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() <= 0) {
            return;
        }
        if (mVideoAdapter == null) {
            mVideoAdapter = new VideoAdapter(getApplicationContext(), data);
            mVideoAdapter.setMediaSelectVideoActivity(this);
            mVideoAdapter.setOnSelectChangedListener(this);
        } else {
            mVideoAdapter.swapCursor(data);
        }


        if (gridview.getAdapter() == null) {
            gridview.setAdapter(mVideoAdapter);
        }
        mVideoAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mVideoAdapter != null)
            mVideoAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy() {
        getLoaderManager().destroyLoader(0);
        Glide.get(this).clearMemory();
        super.onDestroy();
    }

    @Override
    public void onSelect(String path, String cover) {
        int videoTrack=-1;
        int audioTrack=-1;
        MediaExtractor extractor=new MediaExtractor();
        try {
            extractor.setDataSource(path);
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                    videoTrack=i;
                    String videoMime = format.getString(MediaFormat.KEY_MIME);
                    if(!"video/avc".equals(videoMime)){
                        Toast.makeText(this,"视频格式不支持",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    continue;
                }
                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
                    audioTrack=i;
                    String audioMime = format.getString(MediaFormat.KEY_MIME);
                    if(!"audio/mp4a-latm".equals(audioMime)){
                        Toast.makeText(this,"视频格式不支持",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    continue;
                }
            }
            extractor.release();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"视频格式不支持",Toast.LENGTH_SHORT).show();
            extractor.release();
            return;
        }
        if(videoTrack==-1||audioTrack==-1){
            Toast.makeText(this,"视频格式不支持",Toast.LENGTH_SHORT).show();
            return;
        }
        //跳转预览界面 TODO
        if(!TextUtils.isEmpty(path)){
            Intent intent=new Intent(this,PreviewActivity.class);
            intent.putExtra("path",path);
            startActivity(intent);
        }
    }
}
