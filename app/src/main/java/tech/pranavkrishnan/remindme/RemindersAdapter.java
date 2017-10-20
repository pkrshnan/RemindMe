package tech.pranavkrishnan.remindme;

/**
 * Created by Pranav Krishnan on 10/11/2017.
 */
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;


public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.MyViewHolder> {
    private List<Reminder> remindersList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, location, tag;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.row_title);
            location = (TextView) view.findViewById(R.id.row_location);
            tag = (TextView) view.findViewById(R.id.row_tag);

            RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.list_item_click);
            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), EditActivity.class);
                    intent.putExtra("Title", remindersList.get(getAdapterPosition()).getTitle());
                    intent.putExtra("Address", remindersList.get(getAdapterPosition()).getAddress());
                    intent.putExtra("Tag", remindersList.get(getAdapterPosition()).getCategory());
                    intent.putExtra("Priority", remindersList.get(getAdapterPosition()).getPriority());
                    intent.putExtra("Repeat", remindersList.get(getAdapterPosition()).getRepeat());
                    v.getContext().startActivity(intent);
                }
            });

        }
    }


    public RemindersAdapter(Context context, List<Reminder> remindersList) {
        this.remindersList = remindersList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminders_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Reminder reminder = remindersList.get(position);
        holder.title.setText(reminder.getTitle());
        holder.location.setText(reminder.getAddress());
        holder.tag.setText(reminder.getCategory());
    }

    @Override
    public int getItemCount() {
        return remindersList.size();
    }

}
