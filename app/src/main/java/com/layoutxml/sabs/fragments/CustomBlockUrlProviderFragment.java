package com.layoutxml.sabs.fragments;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.layoutxml.sabs.App;
import com.layoutxml.sabs.MainActivity;
import com.layoutxml.sabs.R;
import com.layoutxml.sabs.adapter.BlockUrlProviderAdapter;
import com.layoutxml.sabs.db.AppDatabase;
import com.layoutxml.sabs.db.entity.BlockUrl;
import com.layoutxml.sabs.db.entity.BlockUrlProvider;
import com.layoutxml.sabs.utils.BlockUrlUtils;
import com.layoutxml.sabs.viewmodel.BlockUrlProvidersViewModel;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.layoutxml.sabs.Global.BlockedUniqueUrls;

public class CustomBlockUrlProviderFragment extends LifecycleFragment {

    private static final String TAG = CustomBlockUrlProviderFragment.class.getCanonicalName();
    private AppDatabase mDb;
    private EditText blockUrlProviderEditText;
    private Button addBlockUrlProviderButton;
    private ListView blockListView;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDb = AppDatabase.getAppDatabase(App.get().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_custom_url_provider, container, false);
        getActivity().setTitle(R.string.subscribe_to_providers);
        blockUrlProviderEditText = view.findViewById(R.id.blockUrlProviderEditText);
        addBlockUrlProviderButton = view.findViewById(R.id.addBlockUrlProviderButton);
        blockListView = view.findViewById(R.id.blockUrlProviderListView);
        TextView uniqueTextView = view.findViewById(R.id.uniqueDomains);
        Button updateBlockUrlProvidersButton = view.findViewById(R.id.updateBlockUrlProvidersButton);

        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (BlockedUniqueUrls==0)
        BlockedUniqueUrls = sharedPreferences.getInt("blockedUrls", 0);
        uniqueTextView.setText("Blocked unique domains: "+BlockedUniqueUrls+". Note that you need to reapply blocking for the number to update.");

        ((MainActivity)getActivity()).hideBottomBar();

        updateBlockUrlProvidersButton.setOnClickListener(v -> {
            // TODO: getAll all
            // TODO: then loop and delete and update
            Maybe.fromCallable(() -> {
                List<BlockUrlProvider> blockUrlProviders = mDb.blockUrlProviderDao().getAll2();
                mDb.blockUrlDao().deleteAll();
                for (BlockUrlProvider blockUrlProvider : blockUrlProviders) {
                    try {
                        List<BlockUrl> blockUrls = BlockUrlUtils.loadBlockUrls(blockUrlProvider);
                        blockUrlProvider.count = blockUrls.size();
                        blockUrlProvider.lastUpdated = new Date();
                        mDb.blockUrlProviderDao().updateBlockUrlProviders(blockUrlProvider);
                        mDb.blockUrlDao().insertAll(blockUrls);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to fetch url from urlProvider", e);
                    }
                }
                return null;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();

        });
        addBlockUrlProviderButton.setOnClickListener(v -> {
            String urlProvider = blockUrlProviderEditText.getText().toString();

            // Validation for checking whether a local file has been entered
            String localpathPattern = "(?i)^([/]storage[/])([A-Z0-9-_./]+)([.]txt)$|^([/]sdcard[0-9]?[/])([A-Z0-9-_./]+)([.]txt)$";
            Pattern r = Pattern.compile(localpathPattern);
            Matcher m = r.matcher(urlProvider);

            // If a value has been entered and matches our local host file regex

            if (!urlProvider.isEmpty() && m.matches()) {

                // Construct the local path
                String localfilePath = urlProvider;

                // Create a new file object for verifying
                File localhostFile = new File(localfilePath);

                // If the file exists
                if(localhostFile.exists())
                {
                    Log.d(TAG, "Local host file detected: " + localfilePath);

                    Maybe.fromCallable(() -> {
                        BlockUrlProvider blockUrlProvider = new BlockUrlProvider();
                        blockUrlProvider.url = localfilePath;
                        blockUrlProvider.count = 0;
                        blockUrlProvider.deletable = true;
                        blockUrlProvider.lastUpdated = new Date();
                        blockUrlProvider.selected = false;
                        blockUrlProvider.id = mDb.blockUrlProviderDao().insertAll(blockUrlProvider)[0];
                        // Try to download and parse urls
                        try {
                            List<BlockUrl> blockUrls = BlockUrlUtils.loadBlockUrls(blockUrlProvider);
                            blockUrlProvider.count = blockUrls.size();
                            Log.d(TAG, "Number of urls to insert: " + blockUrlProvider.count);
                            // Save url provider
                            mDb.blockUrlProviderDao().updateBlockUrlProviders(blockUrlProvider);
                            // Save urls from providers
                            mDb.blockUrlDao().insertAll(blockUrls);
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to download links from urlproviders", e);
                        }

                        return null;
                    })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                    blockUrlProviderEditText.setText("");
                }
                else
                {
                    Toast.makeText(getContext(), "Host file not found.", Toast.LENGTH_LONG).show();
                }
            }
            // Check if normal url
            else if (!urlProvider.isEmpty() && Patterns.WEB_URL.matcher(urlProvider).matches()) {
                Maybe.fromCallable(() -> {
                    BlockUrlProvider blockUrlProvider = new BlockUrlProvider();
                    blockUrlProvider.url = (URLUtil.isValidUrl(urlProvider)) ? urlProvider : "http://" + urlProvider;
                    blockUrlProvider.count = 0;
                    blockUrlProvider.deletable = true;
                    blockUrlProvider.lastUpdated = new Date();
                    blockUrlProvider.selected = false;
                    blockUrlProvider.id = mDb.blockUrlProviderDao().insertAll(blockUrlProvider)[0];
                    // Try to download and parse urls
                    try {
                        List<BlockUrl> blockUrls = BlockUrlUtils.loadBlockUrls(blockUrlProvider);
                        blockUrlProvider.count = blockUrls.size();
                        Log.d(TAG, "Number of urls to insert: " + blockUrlProvider.count);
                        // Save url provider
                        mDb.blockUrlProviderDao().updateBlockUrlProviders(blockUrlProvider);
                        // Save urls from providers
                        mDb.blockUrlDao().insertAll(blockUrls);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to download links from urlproviders", e);
                    }

                    return null;
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();
                blockUrlProviderEditText.setText("");
            } else {
                Toast.makeText(getContext(), "Url is invalid", Toast.LENGTH_LONG).show();
            }
        });

        BlockUrlProvidersViewModel model = ViewModelProviders.of(getActivity()).get(BlockUrlProvidersViewModel.class);
        model.getBlockUrlProviders().observe(this, blockUrlProviders -> {
            BlockUrlProviderAdapter adapter = new BlockUrlProviderAdapter(this.getContext(), blockUrlProviders);
            blockListView.setAdapter(adapter);
        });

        return view;
    }
}
