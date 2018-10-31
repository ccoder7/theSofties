package ch.epfl.sweng.favors.favors;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.GeoPoint;

import ch.epfl.sweng.favors.R;
import ch.epfl.sweng.favors.database.Favor;
import ch.epfl.sweng.favors.databinding.FragmentFavorDetailViewBinding;
import ch.epfl.sweng.favors.location.LocationHandler;


public class FavorDetailView extends android.support.v4.app.Fragment  {

    public ObservableField<String> title;
    public ObservableField<String> description;
    public ObservableField<String> location;
    public ObservableField<String> category;
    public ObservableField<Object> geo;
    public ObservableField<String> distance = new ObservableField<>();

    private Favor localFavor;

    FragmentFavorDetailViewBinding binding;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String FAVOR_ID = "favorID";
    private String currentFavorID;


    public FavorDetailView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedViewFavor model = ViewModelProviders.of(getActivity()).get(SharedViewFavor.class);
        if(savedInstanceState != null) {
            currentFavorID = savedInstanceState.getString(FAVOR_ID);
            localFavor = new Favor(currentFavorID);
            setFields(localFavor);
        }
        else {
            model.getFavor().observe(this, newFavor -> {
                setFields(newFavor);
                //TODO add token cost binding with new database implementation
            });
        }
    }

    private void setFields(Favor favor) {
        title = favor.getObservableObject(Favor.StringFields.title);
        description = favor.getObservableObject(Favor.StringFields.description);
        category = favor.getObservableObject(Favor.StringFields.category);
        location = favor.getObservableObject(Favor.StringFields.locationCity);
        geo = favor.getObservableObject(Favor.ObjectFields.location);
        distance.set(LocationHandler.distanceBetween((GeoPoint)geo.get()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favor_detail_view,container,false);
        binding.setElements(this);

        binding.favReportAbusiveAdd.setOnClickListener((l)->{
            Toast.makeText(this.getContext(), "issue has been reported! Sorry for the inconvenience", Toast.LENGTH_LONG).show();
        });

        binding.favIntrestedButton.setOnClickListener((l)->{
            Toast.makeText(this.getContext(), "We will inform the poster of the add that you are intrested to help!", Toast.LENGTH_LONG).show();
        });

        return binding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        currentFavorID = null;
    }

}