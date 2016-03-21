package com.framgia.project1.humanresourcemanagement.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.framgia.project1.humanresourcemanagement.R;
import com.framgia.project1.humanresourcemanagement.data.model.Constant;
import com.framgia.project1.humanresourcemanagement.data.model.Staff;
import com.framgia.project1.humanresourcemanagement.data.remote.DatabaseRemote;
import com.framgia.project1.humanresourcemanagement.ui.adapter.RecyclerViewStaffAdapter;
import com.paginate.Paginate;
import com.paginate.recycler.LoadingListItemCreator;

import java.sql.SQLException;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

public class ListStaffActivity extends AppCompatActivity implements Paginate.Callbacks {
    private List<Staff> listStaff;
    private List<Staff> allStaff;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    RecyclerViewStaffAdapter recyclerViewStaffAdapter;
    private boolean loading = false;
    private int itemLoaded = 0;
    private int numberOfStaff;
    private Handler handler;
    private Paginate paginate;
    private DatabaseRemote mDatabaseRemote;
    private int departmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_staff);
        findView();
        handler = new Handler();
        setupPagination();
    }

    private void findView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getCharSequenceExtra(Constant.INTENT_DATA_TITLE));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_list_staff);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new SlideInUpAnimator());
        getListStaff();
        recyclerViewStaffAdapter = new RecyclerViewStaffAdapter(this, listStaff);
        recyclerView.setAdapter(recyclerViewStaffAdapter);
    }

    private void getListStaff() {
        departmentId = getIntent().getIntExtra(Constant.INTENT_DATA, 0);
        mDatabaseRemote = new DatabaseRemote(this);
        try {
            mDatabaseRemote.openDataBase();
            allStaff = mDatabaseRemote.getListStaff(departmentId);
            numberOfStaff = allStaff.size();
            int end = (numberOfStaff > Constant.STAFF_PER_PAGE) ? Constant.STAFF_PER_PAGE : numberOfStaff;
            listStaff = allStaff.subList(0, end);
            mDatabaseRemote.closeDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupPagination() {
        if (paginate != null) {
            paginate.unbind();
        }
        handler.removeCallbacks(fakeCallback);
        paginate = Paginate.with(recyclerView, this)
                .addLoadingListItem(!hasLoadedAllItems())
                .setLoadingListItemCreator(new CustomLoadingItemCreator())
                .build();
    }

    @Override
    public synchronized void onLoadMore() {
        loading = true;
        handler.postDelayed(fakeCallback, Constant.DELAY_TIME);
    }

    @Override
    public synchronized boolean isLoading() {
        return loading;
    }

    @Override
    public boolean hasLoadedAllItems() {
        if (itemLoaded == numberOfStaff) {
            return true;
        }
        return false;
    }

    private Runnable fakeCallback = new Runnable() {
        @Override
        public void run() {
            int end = itemLoaded + Constant.STAFF_PER_PAGE;
            if (end >= numberOfStaff) {
                end = numberOfStaff;
            }
            listStaff = allStaff.subList(itemLoaded, end);
            recyclerViewStaffAdapter = new RecyclerViewStaffAdapter(ListStaffActivity.this, listStaff);
            recyclerView.setAdapter(recyclerViewStaffAdapter);
            itemLoaded = end;
            loading = false;
            setupPagination();
        }
    };

    private class CustomLoadingItemCreator implements LoadingListItemCreator {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.custom_loading_list_item, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View view) {
            super(view);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getListStaff();
        recyclerViewStaffAdapter.notifyDataSetChanged();
        recyclerView.invalidate();
        Toast.makeText(getApplicationContext(), "onResume", Toast.LENGTH_LONG).show();
    }
}