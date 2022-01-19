package com.nerbly.bemoji.Activities;

import static com.nerbly.bemoji.Configurations.ASSETS_SOURCE_LINK;
import static com.nerbly.bemoji.Functions.MainFunctions.getScreenWidth;
import static com.nerbly.bemoji.Functions.MainFunctions.loadLocale;
import static com.nerbly.bemoji.Functions.Utils.ZIP;
import static com.nerbly.bemoji.Functions.Utils.isStoragePermissionGranted;
import static com.nerbly.bemoji.Functions.Utils.requestStoragePermission;
import static com.nerbly.bemoji.UI.MainUIMethods.DARK_ICONS;
import static com.nerbly.bemoji.UI.MainUIMethods.advancedCorners;
import static com.nerbly.bemoji.UI.MainUIMethods.marqueeTextView;
import static com.nerbly.bemoji.UI.MainUIMethods.rippleRoundStroke;
import static com.nerbly.bemoji.UI.MainUIMethods.setViewRadius;
import static com.nerbly.bemoji.UI.MainUIMethods.shadAnim;
import static com.nerbly.bemoji.UI.MainUIMethods.transparentStatusBar;
import static com.nerbly.bemoji.UI.UserInteractions.showCustomSnackBar;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nerbly.bemoji.Functions.FileUtil;
import com.nerbly.bemoji.R;
import com.nerbly.bemoji.UI.DownloaderSheet;
import com.nerbly.bemoji.UI.UserInteractions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PackPreviewActivity extends AppCompatActivity {
    private final ArrayList<HashMap<String, Object>> emojisListMap = new ArrayList<>();
    private final ObjectAnimator downAnim = new ObjectAnimator();
    private GridLayoutManager layoutManager1 = new GridLayoutManager(this, 3);
    private BottomSheetBehavior<LinearLayout> sheetBehavior;
    private boolean isDownloading = false;
    private int downloadPackPosition = 0;
    private boolean isPackDownloaded = false;
    private String tempPackName = "";
    private boolean isGoingToZipPack = false;
    private String downloadPackPath = "";
    private String packEmojisArrayString = "";
    private ArrayList<String> downloadPackArrayList = new ArrayList<>();
    private RelativeLayout relativeView;
    private LinearLayout download;
    private LinearLayout bsheetbehavior;
    private LinearLayout background;
    private LinearLayout slider;
    private TextView activityTitle;
    private RecyclerView packsRecycler;
    private ImageView download_ic;
    private TextView download_tv;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale(this);
        setContentView(R.layout.packpreview);
        initialize();
        FirebaseApp.initializeApp(this);
        initializeLogic();
    }

    private void initialize() {
        relativeView = findViewById(R.id.relativeView);
        download = findViewById(R.id.download);
        bsheetbehavior = findViewById(R.id.sheetBehavior);
        background = findViewById(R.id.background);
        slider = findViewById(R.id.slider);
        activityTitle = findViewById(R.id.activityTitle);
        packsRecycler = findViewById(R.id.packEmojisRecycler);
        download_ic = findViewById(R.id.download_ic);
        download_tv = findViewById(R.id.download_tv);
        sharedPref = getSharedPreferences("AppData", Activity.MODE_PRIVATE);

        relativeView.setOnClickListener(_view -> {
            shadAnim(download, "translationY", 200, 200);
            shadAnim(download, "alpha", 0, 200);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        });

        download.setOnClickListener(_view -> {
            if (!isDownloading && !isPackDownloaded) {
                if (Build.VERSION.SDK_INT >= 30) {
                    downloadPack(packEmojisArrayString, tempPackName);
                } else {
                    askForZippingSheet();
                }
            }
        });
    }

    private void initializeLogic() {
        LOGIC_FRONTEND();
        LOGIC_BACKEND();
    }

    public void LOGIC_BACKEND() {
        overridePendingTransition(R.anim.fade_in, 0);
        sheetBehavior = BottomSheetBehavior.from(bsheetbehavior);
        activityTitle.setText(getIntent().getStringExtra("packName"));
        setGridColumns();
        bottomSheetBehaviorListener();
        try {
            tempPackName = getIntent().getStringExtra("packName");
            packEmojisArrayString = getIntent().getStringExtra("packEmojisArray");
            ArrayList<String> emojisStringArray = new Gson().fromJson(getIntent().getStringExtra("packEmojisArray"), new TypeToken<ArrayList<String>>() {
            }.getType());
            for (int i = 0; i < emojisStringArray.size(); i++) {
                HashMap<String, Object> emojisMap = new HashMap<>();
                emojisMap.put("emoji_link", ASSETS_SOURCE_LINK + emojisStringArray.get(i));
                emojisMap.put("slug", emojisStringArray.get(i));
                emojisListMap.add(emojisMap);
            }
            packsRecycler.setAdapter(new Recycler1Adapter(emojisListMap));
        } catch (Exception e) {
            UserInteractions.showCustomSnackBar(getString(R.string.failed_to_load_emojis), this);
            new Handler().postDelayed(() -> sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN), 3000);
        }
    }

    public void LOGIC_FRONTEND() {
        advancedCorners(background, "#FFFFFF", 40, 40, 0, 0);
        marqueeTextView(activityTitle);
        setViewRadius(slider, 90, "#E0E0E0");
        rippleRoundStroke(download, "#7289DA", "#687DC8", getResources().getDimension(R.dimen.buttons_corners_radius), 0, "#7289DA");
        DARK_ICONS(this);
        transparentStatusBar(this);
    }

    private void setImgURL(final String url, final ImageView image) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.loading)
                .priority(Priority.HIGH);

        Glide.with(this)
                .load(url)
                .apply(options)
                .into(image);

    }

    private void setGridColumns() {
        float scaleFactor = getResources().getDisplayMetrics().density * 60;
        int screenWidth = getScreenWidth(this);
        int columns = (int) ((float) screenWidth / scaleFactor);
        layoutManager1 = new GridLayoutManager(this, columns);
        packsRecycler.setLayoutManager(layoutManager1);
    }

    private void bottomSheetBehaviorListener() {
        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        shadAnim(background, "elevation", 20, 200);
                        shadAnim(slider, "translationY", 0, 200);
                        shadAnim(slider, "alpha", 1, 200);
                        shadAnim(download, "translationY", 0, 200);
                        shadAnim(download, "alpha", 1, 200);
                        slider.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        shadAnim(background, "elevation", 20, 200);
                        shadAnim(slider, "translationY", 0, 200);
                        shadAnim(slider, "alpha", 1, 200);
                        slider.setVisibility(View.VISIBLE);
                        if (!isDownloading) {
                            shadAnim(download, "translationY", 200, 200);
                            shadAnim(download, "alpha", 0, 200);
                        }
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        shadAnim(background, "elevation", 0, 200);
                        shadAnim(slider, "translationY", -200, 200);
                        shadAnim(slider, "alpha", 0, 200);
                        shadAnim(download, "translationY", 0, 200);
                        shadAnim(download, "alpha", 1, 200);
                        slider.setVisibility(View.INVISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        if (isDownloading) {
                            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        } else {
                            shadAnim(relativeView, "alpha", 0, 200);
                            new Handler().postDelayed(() -> finish(), 150);
                        }
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

    }


    private void startPackDownload(String name, String path, String url) {
        if (!isDownloading) {
            isDownloading = true;
            download_tv.setText(R.string.downloading);
            download_ic.setImageResource(R.drawable.loadingimg);
            downAnim.setTarget(download_ic);
            downAnim.setPropertyName("rotation");
            downAnim.setFloatValues((float) (1000));
            downAnim.setRepeatCount(999);
            downAnim.setDuration(1000);
            downAnim.setRepeatMode(ValueAnimator.REVERSE);
            downAnim.start();
        }
        PRDownloader.download(url, path, name)
                .build()
                .setOnStartOrResumeListener(() -> {

                })
                .setOnPauseListener(() -> {

                })
                .setOnCancelListener(() -> {

                })
                .setOnProgressListener(progress -> {
                })
                .start(new OnDownloadListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDownloadComplete() {
                        downloadPackPosition++;
                        download_tv.setText(getString(R.string.pack_downloading_progress) + " " + downloadPackPosition + "/" + downloadPackArrayList.size());
                        downloadPack(new Gson().toJson(downloadPackArrayList), tempPackName);
                        MediaScannerConnection.scanFile(PackPreviewActivity.this,
                                new String[]{path}, null,
                                (path1, uri) -> {
                                });
                    }

                    @Override
                    public void onError(Error error) {
                        isDownloading = false;
                        download_tv.setText(R.string.download_btn_txt);
                        download_ic.setImageResource(R.drawable.round_get_app_white_48dp);
                        showCustomSnackBar(getString(R.string.error_msg_2), PackPreviewActivity.this);
                        download_ic.setRotation((float) (0));
                        downAnim.cancel();
                    }
                });

    }

    public void downloadPack(String array, String packName) {
        try {
            downloadPackArrayList = new Gson().fromJson(array, new TypeToken<ArrayList<String>>() {
            }.getType());
            if (downloadPackPosition == downloadPackArrayList.size()) {
                if (isGoingToZipPack) {
                    zippingTask();
                } else {
                    isDownloading = false;
                    isPackDownloaded = true;
                    download_tv.setText(R.string.download_success);
                    download_ic.setImageResource(R.drawable.round_done_white_48dp);
                    download_ic.setRotation(0);
                    showCustomSnackBar(getString(R.string.full_download_path) + " " + downloadPackPath, this);
                    downAnim.cancel();
                }
            } else {
                String downloadPackUrl = ASSETS_SOURCE_LINK + downloadPackArrayList.get(downloadPackPosition);
                String downloadPackName = getString(R.string.app_name) + "_" + downloadPackArrayList.get(downloadPackPosition);
                if (isGoingToZipPack) {
                    downloadPackPath = FileUtil.getPackageDataDir(getApplicationContext()) + "/Zipper/" + packName;
                } else {
                    downloadPackPath = FileUtil.getPublicDir(Environment.DIRECTORY_DOWNLOADS) + "/" + getString(R.string.app_name) + "/" + packName;
                }
                startPackDownload(downloadPackName, downloadPackPath, downloadPackUrl);
            }
        } catch (Exception ignored) {
        }
    }

    public void askForZippingSheet() {
        if (isStoragePermissionGranted(this)) {

            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.materialsheet);
            View bottomSheetView;
            bottomSheetView = getLayoutInflater().inflate(R.layout.infosheet, (ViewGroup) null);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);

            final ImageView image = bottomSheetView.findViewById(R.id.image);
            final TextView infook = bottomSheetView.findViewById(R.id.infosheet_ok);
            final TextView infocancel = bottomSheetView.findViewById(R.id.infosheet_cancel);
            final TextView infotitle = bottomSheetView.findViewById(R.id.infosheet_title);
            final TextView infosub = bottomSheetView.findViewById(R.id.infosheet_description);
            final LinearLayout infoback = bottomSheetView.findViewById(R.id.infosheet_back);
            final LinearLayout slider = bottomSheetView.findViewById(R.id.slider);

            advancedCorners(infoback, "#ffffff", 38, 38, 0, 0);
            setViewRadius(slider, 180, "#BDBDBD");
            infotitle.setText(R.string.pack_confirmation_sheet_title);
            infosub.setText(R.string.pack_confirmation_sheet_subtitle);
            infook.setText(R.string.pack_confirmation_sheet_btn1);
            infocancel.setText(R.string.pack_confirmation_sheet_btn2);
            image.setImageResource(R.drawable.ic_files_and_folder_flatline);
            infook.setOnClickListener(v -> {
                isGoingToZipPack = true;
                downloadPack(packEmojisArrayString, tempPackName);
                bottomSheetDialog.dismiss();
            });
            infocancel.setOnClickListener(v -> {
                isGoingToZipPack = false;
                downloadPack(packEmojisArrayString, tempPackName);
                bottomSheetDialog.dismiss();
            });
            if (!isFinishing()) {
                try {
                    bottomSheetDialog.show();
                } catch (Exception e) {
                    showCustomSnackBar(getString(R.string.error_msg), this);
                }
            } else {
                showCustomSnackBar(getString(R.string.error_msg), this);
            }
        } else {
            requestStoragePermission(1, this);
            showCustomSnackBar(getString(R.string.ask_for_permission), this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download.performClick();
            } else {
                showCustomSnackBar(getString(R.string.permission_denied_packs), this);
            }
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void zippingTask() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            ZIP(downloadPackPath, Environment.DIRECTORY_DOWNLOADS + "/" + getString(R.string.app_name) + "/" + tempPackName + ".zip");
            downloadPackPath = Environment.DIRECTORY_DOWNLOADS + "/" + getString(R.string.app_name) + "/" + tempPackName + ".zip";

            handler.post(() -> {
                isDownloading = false;
                download_tv.setText(R.string.download_success);
                download_ic.setImageResource(R.drawable.round_done_white_48dp);
                download_ic.setRotation((float) (0));
                showCustomSnackBar(R.string.full_download_path + downloadPackPath, PackPreviewActivity.this);
                downAnim.cancel();
                FileUtil.deleteFile(downloadPackPath);
            });
        });
    }

    @Override
    public void onBackPressed() {
        shadAnim(download, "translationY", 200, 200);
        shadAnim(download, "alpha", 0, 200);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public class Recycler1Adapter extends RecyclerView.Adapter<Recycler1Adapter.ViewHolder> {
        ArrayList<HashMap<String, Object>> data;

        public Recycler1Adapter(ArrayList<HashMap<String, Object>> arr) {
            data = arr;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.emojisview, parent, false);
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            v.setLayoutParams(layoutParams);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            View view = holder.itemView;

            final LinearLayout space = view.findViewById(R.id.space);
            LinearLayout emojisBackground = view.findViewById(R.id.emojiBackground);
            ImageView emoji = view.findViewById(R.id.emoji);

            setImgURL(Objects.requireNonNull(data.get(position).get("emoji_link")).toString(), emoji);
            emojisBackground.setOnClickListener(_view -> {
                try {
                    DownloaderSheet downloaderSheet = new DownloaderSheet();
                    downloaderSheet.showEmojiSheet(PackPreviewActivity.this, Objects.requireNonNull(data.get(position).get("emoji_link")).toString(), Objects.requireNonNull(data.get(position).get("slug")).toString(), "Emoji lovers");
                } catch (Exception e) {
                    Log.e("downloader", e.toString());
                    e.printStackTrace();
                }
            });

            if (position == getItemCount() - 1) {
                space.setVisibility(View.VISIBLE);
            } else {
                space.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View v) {
                super(v);
            }
        }
    }
}
