package com.example.user.newstest;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchableFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<News>>, SwipeRefreshLayout.OnRefreshListener {

    public static final String LOG_TAG = "SearchableFragment";

    public static final String BASE_SEARCH_URL = "https://newsapi.org/v2/everything?";
    public static final String QUERY_PARAM = "q";
    public static final String API_KEY_PARAM = "apiKey";

    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingIndicator;
    private TextView emptyView;
    private String query;

    public SearchableFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        query = getArguments().getString("query");
        Log.v(LOG_TAG, "onCreate: recieved query is " + query);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_news, container, false);

        loadingIndicator = v.findViewById(R.id.loading_indicator);
        emptyView = v.findViewById(R.id.empty_view);
        emptyView.setVisibility(View.GONE);

        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return v;
    }

    private void updateUI() {
        adapter = new RecyclerViewAdapter(getActivity(), new ArrayList<News>());
        recyclerView.setAdapter(adapter);

        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);

            getActivity().getSupportLoaderManager().initLoader(10, null, SearchableFragment.this);
        } else {
            recyclerView.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            loadingIndicator.setVisibility(View.GONE);

            emptyView.setText(R.string.no_connection);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public Loader<List<News>> onCreateLoader(int id, @Nullable Bundle args) {
        String builtUri = Uri.parse(BASE_SEARCH_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, query)
                .appendQueryParameter(API_KEY_PARAM, "636fabe95997449195a6e4d1c9b96b44")
                .build().toString();

        Log.v(LOG_TAG, "OnCreateLoader: builtUrl: " + builtUri);
        return new NewsLoader(getActivity(), builtUri);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<News>> loader, List<News> data) {
        Log.v(LOG_TAG, "onLoadFinished");
        loadingIndicator.setVisibility(View.GONE);

        swipeRefreshLayout.setRefreshing(false);
        adapter.clear();
        if (data != null && !data.isEmpty()) {
            adapter.addAll(data);
            adapter.notifyDataSetChanged();
        } else {
            emptyView.setText(R.string.no_news);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<News>> loader) {
        Log.v(LOG_TAG, "onLoaderReset");

        adapter.clear();
    }

    @Override
    public void onRefresh() {
        updateUI();
    }
}