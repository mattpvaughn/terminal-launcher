package io.github.httpmattpvaughn.terminallauncher;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * From Inkplayer Music Player by Matt
 */

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    private List<String> suggestionList;
    private SuggestionInterface suggestionInterface;


    public interface SuggestionInterface {
        void prepareForNextInput();

        void parseInput(String input);
    }

    public SuggestionAdapter(List<String> suggestionList, SuggestionInterface suggestionInterface) {
        this.suggestionList = suggestionList;
        this.suggestionInterface = suggestionInterface;
    }

    @Override
    public SuggestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_suggestion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SuggestionAdapter.ViewHolder holder, final int position) {
        final String suggestion = suggestionList.get(position);
        holder.text.setText(suggestion);
        holder.suggestionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Test whether suggestion is an app or
                suggestionInterface.parseInput("o " + suggestion.toLowerCase());
                suggestionInterface.prepareForNextInput();
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestionList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        View suggestionContainer;

        public ViewHolder(View itemView) {
            super(itemView);
            suggestionContainer = itemView.findViewById(R.id.suggestion_container);
            text = itemView.findViewById(R.id.suggestion_text);
        }
    }

}
