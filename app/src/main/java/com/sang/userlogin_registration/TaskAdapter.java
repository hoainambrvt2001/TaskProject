package com.sang.userlogin_registration;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder>{

    public static final String EDIT_TASK = "Edit Task";
    private static final String TAG = "TaskAdapter";

    private Context context;
    private ArrayList<Task> userTasks;

    public TaskAdapter(Context context, ArrayList<Task> userTasks) {
        this.context = context;
        this.userTasks = userTasks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Setting checkBox:
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                DatabaseReference databaseReference = firebaseDatabase.getReference("Tasks");
                HashMap<String, Object> update = new HashMap<>();
                update.put("doneStatus", true);
                databaseReference.child(userTasks.get(position).getTaskId()).updateChildren(update);
            }
        });

        //Get Task data:
        String taskName = String.valueOf(userTasks.get(position).getName());
        String taskStartDate = String.valueOf(userTasks.get(position).getStartDate());
        String taskDueDate = String.valueOf(userTasks.get(position).getDueDate());

        //Set Views:
        holder.show_title.setText(taskName);
        holder.txtStartDate.setText(taskStartDate);
        holder.txtDueDate.setText(taskDueDate);
        switch (Integer.parseInt(userTasks.get(position).getPriority())) {
            case 0:
                holder.imagePriority.setImageResource(R.drawable.green_tag);
                break;
            case 1:
                holder.imagePriority.setImageResource(R.drawable.red_tag);
                break;
            case 2:
                holder.imagePriority.setImageResource(R.drawable.blue_tag);
                break;
            case 3:
                holder.imagePriority.setImageResource(R.drawable.yellow_tag);
                break;
        }

        if (userTasks.get(position).isDoneStatus()) {
            holder.checkBox.setChecked(true);
        }

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Edit Task")
                        .setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, EditTaskActivity.class);
                                intent.putExtra(EDIT_TASK, userTasks.get(position));
                                context.startActivity(intent);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //dismiss dialog
                                dialog.dismiss();
                            }
                        });
                //show dialog
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return userTasks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageTag;
        private TextView show_title, txtStartDate, txtDueDate;
        private CheckBox checkBox;
        private ImageView imagePriority;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageTag = itemView.findViewById(R.id.imageTag);
            show_title = itemView.findViewById(R.id.show_title);
            txtStartDate = itemView.findViewById(R.id.txtStartDate);
            txtDueDate = itemView.findViewById(R.id.txtDueDate);
            checkBox = itemView.findViewById(R.id.checkBox);
            imagePriority = itemView.findViewById(R.id.imagePriority);
        }
    }
}