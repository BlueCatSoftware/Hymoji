package com.nerbly.bemoji;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.nerbly.bemoji.Functions.FileUtil;
import com.nerbly.bemoji.Functions.Utils;
import com.nerbly.bemoji.UI.MainUIMethods;

import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class PreviewActivity extends AppCompatActivity {

    private final Timer _timer = new Timer();
    private final ObjectAnimator downAnim = new ObjectAnimator();
    BottomSheetBehavior sheetBehavior;
    com.google.android.material.snackbar.Snackbar _snackBarView;
    com.google.android.material.snackbar.Snackbar.SnackbarLayout _sblayout;
    private String downloadPath = "";
    private String downloadUrl = "";
    private boolean isDownloading = false;
    private CoordinatorLayout linear1;
    private LinearLayout bsheetbehavior;
    private LinearLayout relativeview;
    private TextView title;
    private TextView subtitle;
    private TextView information;
    private LinearLayout download;
    private LinearLayout linear2;
    private ImageView imageview1;
    private ImageView imageview7;
    private ImageView imageview6;
    private TextView textview3;
    private TimerTask fixUIIssues;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle _savedInstanceState) {
        super.onCreate(_savedInstanceState);
        setContentView(R.layout.preview);
        initialize(_savedInstanceState);
        com.google.firebase.FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize(Bundle _savedInstanceState) {
        linear1 = findViewById(R.id.linear1);
        bsheetbehavior = findViewById(R.id.bsheetbehavior);
        relativeview = findViewById(R.id.relativeview);
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        information = findViewById(R.id.information);
        download = findViewById(R.id.download);
        linear2 = findViewById(R.id.linear2);
        imageview1 = findViewById(R.id.imageview1);
        imageview7 = findViewById(R.id.imageview7);
        imageview6 = findViewById(R.id.imageview6);
        textview3 = findViewById(R.id.textview3);
        sharedPref = getSharedPreferences("AppData", Activity.MODE_PRIVATE);

        linear1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _view) {
                if (!isDownloading && !textview3.getText().toString().contains("Saved")) {
                    if (sharedPref.getString("downloadPath", "").equals("")) {
                        downloadPath = FileUtil.getPublicDir(Environment.DIRECTORY_DOWNLOADS).concat("/Bemojis");
                    } else {
                        downloadPath = sharedPref.getString("downloadPath", "");
                    }
                    downloadUrl = getIntent().getStringExtra("imageUrl");
                    _startDownload("Bemoji_".concat(downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1)), downloadUrl, downloadPath);
                }
            }
        });
    }

    private void initializeLogic() {
        _LOGIC_BACKEND();
        _LOGIC_FRONTEND();
    }


    @Override
    public void onBackPressed() {
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void _LOGIC_BACKEND() {
        sheetBehavior = BottomSheetBehavior.from(bsheetbehavior);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        fixUIIssues = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                });
            }
        };
        _timer.schedule(fixUIIssues, 200);
        _setBlurImageUrl(imageview1, 25, getIntent().getStringExtra("imageUrl"));
        _setImageFromUrl(imageview7, getIntent().getStringExtra("imageUrl"));
        title.setText(getIntent().getStringExtra("title"));
        subtitle.setText("Submitted by: ".concat(getIntent().getStringExtra("submitted_by")));
        _BottomBehaviourListener();
        MainUIMethods.shadAnim(linear1, "alpha", 1, 200);
    }


    public void _LOGIC_FRONTEND() {
        MainUIMethods.DARK_ICONS(this);

        MainUIMethods.transparentStatusBar(this);

        MainUIMethods.changeActivityFont("whitney", this);

        MainUIMethods.setClippedView(relativeview, "#FFFFFF", 25, 7);

        MainUIMethods.rippleRoundStroke(download, "#7289DA", "#687DC8", 25, 0, "#7289DA");
        android.graphics.drawable.GradientDrawable JJACCAI = new android.graphics.drawable.GradientDrawable();
        int[] JJACCAIADD = new int[]{Color.parseColor("#00000000"), Color.parseColor("#80000000")};
        JJACCAI.setColors(JJACCAIADD);
        JJACCAI.setOrientation(android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM);
        JJACCAI.setCornerRadius(0);
        linear1.setBackground(JJACCAI);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/whitney.ttf"), Typeface.BOLD);
    }


    public void _BottomBehaviourListener() {
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (isDownloading) {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        MainUIMethods.shadAnim(linear1, "alpha", 0, 200);
                        fixUIIssues = new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                });
                            }
                        };
                        _timer.schedule(fixUIIssues, 150);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

    }


    public void _startDownload(final String _name, final String _url, final String _path) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_DENIED || androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_DENIED) {
            androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            _showCustomSnackBar("Please give the access to access your storage to continue");
        } else {
            isDownloading = true;
            textview3.setText("Downloading");
            imageview6.setImageResource(R.drawable.loadingimg);
            downAnim.setTarget(imageview6);
            downAnim.setPropertyName("rotation");
            downAnim.setFloatValues((float) (1000));
            downAnim.setRepeatCount(999);
            downAnim.setDuration(1000);
            downAnim.setRepeatMode(ValueAnimator.REVERSE);
            downAnim.start();
            PRDownloader.download(_url, _path, _name)
                    .build()
                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                        @Override
                        public void onStartOrResume() {

                        }
                    })
                    .setOnPauseListener(new OnPauseListener() {
                        @Override
                        public void onPause() {

                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel() {

                        }
                    })
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {
                            long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                            textview3.setText("Downloading ".concat(String.valueOf(progressPercent).concat("%")));
                        }
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {

                            isDownloading = false;
                            textview3.setText("Saved to downloads folder");
                            imageview6.setImageResource(R.drawable.round_done_white_48dp);
                            imageview6.setRotation((float) (0));
                            information.setText("Full download path: ".concat(downloadPath + "/" + _name));
                            information.setVisibility(View.VISIBLE);
                            downAnim.cancel();

                        }

                        @Override
                        public void onError(Error error) {


                            isDownloading = false;
                            textview3.setText("Download");
                            imageview6.setImageResource(R.drawable.round_get_app_white_48dp);
                            _showCustomSnackBar("Something went wrong, please try again later.");
                            imageview6.setRotation((float) (0));
                            downAnim.cancel();
                        }
                    });

        }
    }


    public void _showCustomSnackBar(final String _text) {
        ViewGroup parentLayout = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);

        _snackBarView = com.google.android.material.snackbar.Snackbar.make(parentLayout, "", com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        _sblayout = (com.google.android.material.snackbar.Snackbar.SnackbarLayout) _snackBarView.getView();

        View _inflate = getLayoutInflater().inflate(R.layout.snackbar, null);
        _sblayout.setPadding(0, 0, 0, 0);
        _sblayout.setBackgroundColor(Color.argb(0, 0, 0, 0));
        LinearLayout back =
                _inflate.findViewById(R.id.linear1);

        TextView text =
                _inflate.findViewById(R.id.textview1);
        MainUIMethods.setViewRadius(back, 20, "#202125");
        text.setText(_text);
        text.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/whitney.ttf"), Typeface.NORMAL);
        _sblayout.addView(_inflate, 0);
        _snackBarView.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                _performClick(download);
            } else {
                _showCustomSnackBar("You can't download emojis without storage access permission. Please allow it.");
            }
        } else {

        }
    }


    public void _performClick(final View _view) {
        _view.performClick();
    }


    public void _setBlurImageUrl(final ImageView _image, final double _blur, final String _url) {
        try {
            RequestOptions options1 = new RequestOptions()
                    .priority(Priority.HIGH);

            Glide.with(this)

                    .load(_url)
                    .apply(options1)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .apply(bitmapTransform(new BlurTransformation((int) _blur, 4)))
                    .into(_image);
        } catch (Exception e) {
            Utils.showMessage(getApplicationContext(), (e.toString()));
        }

    }


    public void _setImageFromUrl(final ImageView _image, final String _url) {
        RequestOptions options = new RequestOptions()
                .priority(Priority.IMMEDIATE);

        Glide.with(this)
                .load(_url)
                .apply(options)
                .into(_image);

    }

}