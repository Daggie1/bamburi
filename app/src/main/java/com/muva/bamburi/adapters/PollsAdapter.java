package com.muva.bamburi.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import androidx.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.R;
import com.muva.bamburi.databinding.PollFullBinding;
import com.muva.bamburi.models.Option;
import com.muva.bamburi.models.Option_;
import com.muva.bamburi.models.Poll;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.viewmodel.PollViewModel;

import java.util.ArrayList;
import java.util.List;

import io.objectbox.Box;

/**
 * Created by Njoro on 5/30/18.
 */
public class PollsAdapter extends ListAdapter<Poll, PollsAdapter.PollViewHolder> {

    private Context context;
    private RadioGroup optionsRadiogroup;
    private List<Option> optionList = new ArrayList<>();
    private Box<Option> optionBox;
    private final ActionCallback actionCallback;


    public interface ActionCallback{
        void onVoteDone();
        void onPollDismissed();
    }

    public PollsAdapter(Context context, @NonNull DiffUtil.ItemCallback<Poll> diffCallback, ActionCallback actionCallback) {
        super(diffCallback);
        this.context = context;
        optionBox = Bamburi.getInstance().getBoxStore().boxFor(Option.class);
        this.actionCallback = actionCallback;
    }

    @NonNull
    @Override
    public PollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PollFullBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.poll_full, parent, false);

        return new PollViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PollViewHolder holder, int position) {

        Poll currentPoll = getItem(position);

        holder.binding.setViewModel(new PollViewModel(context, currentPoll, actionCallback));

        optionList = optionBox.query().equal(Option_.poll_id, currentPoll.getId()).build().find();
        L.e("options size: " + optionList.size());

        optionsRadiogroup = holder.binding.optionsRadioGroup;

        for (Option option : optionList) {
            L.d(option.toString());

            RadioButton radioButton = new RadioButton(context);
            radioButton.setId((int) option.getId());
            radioButton.setText(option.getOption());
            radioButton.setTextColor(ContextCompat.getColorStateList(context, R.color.radio_btn_textcolor));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_enabled}, //disabled
                                new int[]{android.R.attr.state_enabled} //enabled
                        },
                        new int[]{
                                Color.WHITE //disabled
                                , context.getResources().getColor(R.color.colorAccent) //enabled

                        }
                );
                radioButton.setButtonTintList(colorStateList);
            }
            optionsRadiogroup.addView(radioButton);
        }
    }

    class PollViewHolder extends RecyclerView.ViewHolder {
        public PollFullBinding binding;
        public View mView;


        PollViewHolder(PollFullBinding rootView) {
            super(rootView.pollFull);
            this.binding = rootView;
            this.mView = this.binding.pollFull;
        }
    }
}
