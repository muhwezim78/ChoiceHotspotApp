package com.muhwezi.choicehotspot.ui.users;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.muhwezi.choicehotspot.R;
import com.muhwezi.choicehotspot.api.ApiCallback;
import com.muhwezi.choicehotspot.models.user.HotspotUser;
import com.muhwezi.choicehotspot.repository.ApiRepository;

import java.util.List;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyView;
    private CircularProgressIndicator loadingIndicator;
    private com.google.android.material.tabs.TabLayout tabLayout;
    private com.google.android.material.textfield.TextInputEditText searchEditText;
    private UserAdapter adapter;

    private enum UserFilter {
        ACTIVE, ALL, EXPIRED
    }

    private UserFilter currentFilter = UserFilter.ACTIVE;

    // LiveData for active users
    private androidx.lifecycle.LiveData<List<HotspotUser>> activeUsersLiveData;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        emptyView = view.findViewById(R.id.empty_view);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        tabLayout = view.findViewById(R.id.tab_layout);
        searchEditText = view.findViewById(R.id.search_edit_text);

        setupRecyclerView();
        setupSearch();
        setupTabLayout();
        setupObservers();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateEmptyViewForSearch();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void updateEmptyViewForSearch() {
        if (adapter.getItemCount() == 0) {
            showEmpty(true);
            if (searchEditText.getText() != null && !searchEditText.getText().toString().isEmpty()) {
                emptyView.setText("No users match your search");
            } else {
                emptyView.setText(getEmptyMessage());
            }
        } else {
            showEmpty(false);
        }
    }

    private void setupObservers() {
        activeUsersLiveData = ApiRepository.getInstance().getActiveUsersLiveData();
        activeUsersLiveData.observe(getViewLifecycleOwner(), users -> {
            android.util.Log.d("UsersFragment", "LiveData triggered, users: " + (users != null ? users.size() : "null")
                    + ", filter: " + currentFilter);
            if (currentFilter == UserFilter.ACTIVE) {
                if (users == null || users.isEmpty()) {
                    showEmpty(true);
                    emptyView.setText("No active users found");
                } else {
                    showEmpty(false);
                    adapter.setUsers(users);
                    updateEmptyViewForSearch();
                    android.util.Log.d("UsersFragment", "Adapter updated with " + users.size() + " users");
                }
                showLoading(false);
            }
        });
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = UserFilter.ACTIVE;
                        // Trigger a background refresh but let LiveData handle UI
                        ApiRepository.getInstance().refreshActiveUsers();
                        break;
                    case 1:
                        currentFilter = UserFilter.ALL;
                        loadUsers(true);
                        break;
                    case 2:
                        currentFilter = UserFilter.EXPIRED;
                        loadUsers(true);
                        break;
                }
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                if (currentFilter == UserFilter.ACTIVE) {
                    ApiRepository.getInstance().refreshActiveUsers();
                } else {
                    loadUsers(true);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentFilter == UserFilter.ACTIVE) {
            ApiRepository.getInstance().refreshActiveUsers();
        } else {
            loadUsers(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // No handlers to remove
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(getContext(), user -> {
            UserDetailsBottomSheet bottomSheet = UserDetailsBottomSheet.newInstance(user);
            bottomSheet.show(getChildFragmentManager(), "UserDetails");
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadUsers(boolean showLoading) {
        if (currentFilter == UserFilter.ACTIVE)
            return; // Handled by LiveData

        if (showLoading)
            showLoading(true);

        ApiCallback<List<HotspotUser>> callback = new ApiCallback<List<HotspotUser>>() {
            @Override
            public void onSuccess(List<HotspotUser> data) {
                if (!isAdded())
                    return;
                if (showLoading)
                    showLoading(false);

                if (data == null || data.isEmpty()) {
                    showEmpty(true);
                    emptyView.setText(getEmptyMessage());
                } else {
                    showEmpty(false);

                    // Sort by Last Seen Descending
                    if (data != null && (currentFilter == UserFilter.EXPIRED || currentFilter == UserFilter.ALL)) {
                        java.util.Collections.sort(data, (u1, u2) -> {
                            String d1 = u1.getLastSeen();
                            String d2 = u2.getLastSeen();
                            if (d1 == null && d2 == null)
                                return 0;
                            if (d1 == null)
                                return 1; // null last
                            if (d2 == null)
                                return -1;
                            return d2.compareTo(d1); // Descending
                        });
                    }

                    adapter.setUsers(data);
                    updateEmptyViewForSearch();
                }
            }

            @Override
            public void onError(String message, Throwable error) {
                if (isAdded()) {
                    if (showLoading)
                        showLoading(false);
                    // Only show error if list is empty, otherwise toast
                    if (adapter.getItemCount() == 0) {
                        showEmpty(true);
                        emptyView.setText("Error: " + message);
                    } else {
                        Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        switch (currentFilter) {
            case ALL:
                ApiRepository.getInstance().getAllUsers(true, callback);
                break;
            case EXPIRED:
                ApiRepository.getInstance().getExpiredUsers(callback);
                break;
        }
    }

    private String getEmptyMessage() {
        switch (currentFilter) {
            case ACTIVE:
                return "No active users found";
            case EXPIRED:
                return "No expired users found";
            default:
                return "No users found";
        }
    }

    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmpty(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
