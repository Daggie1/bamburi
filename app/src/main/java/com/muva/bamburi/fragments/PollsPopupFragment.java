package com.muva.bamburi.fragments;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.adapters.PollsAdapter;
import com.muva.bamburi.models.Poll;
import com.muva.bamburi.models.Poll_;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;

/**
 * A simple {@link Fragment} subclass.
 */
public class PollsPopupFragment extends Fragment {
    public static PollsPopupFragment newInstance() {

        Bundle args = new Bundle();

        PollsPopupFragment fragment = new PollsPopupFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private Context context;
    private Box<Poll> pollBox;
    private Query<Poll> pollQuery;

    public PollsAdapter pollsAdapter;

    private DiffUtil.ItemCallback<Poll> pollItemCallback = new DiffUtil.ItemCallback<Poll>() {
        @Override
        public boolean areItemsTheSame(Poll oldItem, Poll newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Poll oldItem, Poll newItem) {
            try {
                //compare the dates
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date oldDate = format.parse(oldItem.getCreated_at());
                Date newDate = format.parse(newItem.getCreated_at());

                return newDate.compareTo(oldDate) == 0;
            } catch (Exception exception) {

                return false;
            }
        }
    };

    public PollsPopupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BoxStore boxStore = Bamburi.getInstance().getBoxStore();
        pollBox = boxStore.boxFor(Poll.class);

        //fetch the active polls
        pollQuery = pollBox.query().equal(Poll_.dismissed, false)
                .filter(Poll::isActive)
                .build();

        pollsAdapter = new PollsAdapter(context, pollItemCallback, new PollsAdapter.ActionCallback() {
            @Override
            public void onVoteDone() {
                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).onBackPressed();
                }
            }

            @Override
            public void onPollDismissed() {
                if (context instanceof AppCompatActivity) {
                    ((AppCompatActivity) context).onBackPressed();
                }
            }
        });

//        if (getActivity().getWindow() != null) {
//            getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#BF000000")));
//        }
//        else{
//            L.e("window is null");
//        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_polls_popup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.polls_recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(pollsAdapter);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        pollsAdapter.submitList(pollQuery.find());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

    }
}
