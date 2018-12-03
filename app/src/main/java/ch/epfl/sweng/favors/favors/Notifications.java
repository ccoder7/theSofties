package ch.epfl.sweng.favors.favors;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ch.epfl.sweng.favors.R;
import ch.epfl.sweng.favors.database.Database;
import ch.epfl.sweng.favors.database.Favor;
import ch.epfl.sweng.favors.database.ObservableArrayList;
import ch.epfl.sweng.favors.database.User;
import ch.epfl.sweng.favors.databinding.FavorsListBinding;
import ch.epfl.sweng.favors.databinding.FragmentNotificationsBinding;

/**
 * The fragment which displays the notification of the current user authenticated.
 */
public class Notifications extends Fragment {
    private static final String TAG = "NOTIFICATIONS_LIST";

    FragmentNotificationsBinding binding;
    ArrayList<Notification> notificationsList;
    NotificationListAdapter listAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notifications,container,false);
        binding.setElements(this);
        notificationsList = (ArrayList<Notification>)User.getMain().get(User.ObjectFields.notifications);
        binding.notificationsList.setLayoutManager(new LinearLayoutManager(getContext()));
        listAdapter = new NotificationListAdapter(notificationsList);
        binding.notificationsList.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
