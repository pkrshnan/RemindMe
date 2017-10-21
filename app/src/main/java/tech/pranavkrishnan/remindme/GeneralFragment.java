package tech.pranavkrishnan.remindme;

import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class GeneralFragment extends Fragment {
    private RemindersAdapter mAdapter;
    private RecyclerView recyclerView;

    public static GeneralFragment newInstance(String add) {
        GeneralFragment generalFragment = new GeneralFragment();
        Bundle bundle = new Bundle();
        bundle.putString("Type", add);
        generalFragment.setArguments(bundle);
        return generalFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_general, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), 1);
        recyclerView.addItemDecoration(dividerItemDecoration);

        Bundle args = this.getArguments();
        String type = "General";
        if (args == null) {
            Log.d("Bundle", "Null");
        } else {
            type = args.getString("Type");
            Log.d("Type", type);

            sortList(type);
        }

        return rootView;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void sortList(String type) {
        List<Reminder> reminderList = new ArrayList<Reminder>();
        for (Reminder i : MainActivity.reminderList) {
            if (i.getCategory().equals(type)) {
                reminderList.add(i);
            }
            if (type.equals("General") && !reminderList.contains(i)) {
                reminderList.add(i);
            }
        }

        mAdapter = new RemindersAdapter(reminderList);
        if (recyclerView != null) {
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
        }
    }


}
