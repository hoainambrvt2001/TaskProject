package com.sang.userlogin_registration;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class TodayTaskFragment extends Fragment {

    private TextView txtHello;

    //init bottomNavigation:
    private BottomNavigationView nav_bottomView;
    private FloatingActionButton btnAdd;

    //init recyclerView:
    private ArrayList<Task> userTasks;
    private TaskAdapter adapterTask;
    private RecyclerView recyclerView;

    //init firebase:
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_today_task, container, false);

        // init txtHello:
        txtHello = view.findViewById(R.id.txtHello);

        // init firebase current user:
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //Set up for recyclerView:
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //TODO: Get today tasks:
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(date);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Tasks");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userTasks = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    if (task != null) {
                        if (task.getUserID().equals(user.getUid())) {
                            if (onDurationDate(task.getStartDate(), task.getDueDate(), currentDate))
                                if (!task.isDoneStatus())
                                    userTasks.add(task);
                        }
                    }
                }
                if (userTasks.size() != 0) {
                    Comparator<Task> newTasksComparator = new Comparator<Task>() {
                        @Override
                        public int compare(Task o1, Task o2) {
                            int comparision = Integer.parseInt(o1.getPriority()) - Integer.parseInt(o2.getPriority());
                            if (comparision == 0) {
                                Date date1 = null, date2 = null;
                                try {
                                    date1 = new SimpleDateFormat("dd/MM/yyyy").parse(o1.getDueDate());
                                    date2 = new SimpleDateFormat("dd/MM/yyyy").parse(o2.getDueDate());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return date1.compareTo(date2);
                            }
                            return comparision;
                        }
                    };
                    Collections.sort(userTasks, newTasksComparator);
                }
                adapterTask = new TaskAdapter(getActivity(), userTasks);
                recyclerView.setAdapter(adapterTask);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //TODO: Set up bottom navigation:
        nav_bottomView = view.findViewById(R.id.nav_bottomView);
        nav_bottomView.getMenu().getItem(2).setEnabled(false);
        nav_bottomView.setBackground(null);
        nav_bottomView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //Handle item click
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        return true;
                    case R.id.nav_task:
                        startActivity(new Intent(getActivity(), TaskManagementActivity.class));
                        return true;
                    case R.id.nav_note:
                        return true;
                    case R.id.nav_community:
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });

        //TODO: Set up Floating Button Add:
        btnAdd = view.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateTaskActivity.class));
            }
        });

        //TODO: Set up Delete Action:
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }
        @Override
        public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();
            switch (direction) {
                case ItemTouchHelper.RIGHT:
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    databaseReference = firebaseDatabase.getReference("Tasks");
                    HashMap<String, Object> update = new HashMap<>();
                    update.put("doneStatus", true);
                    databaseReference.child(userTasks.get(position).getTaskId()).updateChildren(update);
                    break;
                case ItemTouchHelper.LEFT:
                    Task deletedTask = userTasks.get(position);
                    Snackbar.make(viewHolder.itemView,"You have successfully deleted " + deletedTask.getName(),Snackbar.LENGTH_INDEFINITE)
                            .setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                                    DatabaseReference databaseReference = firebaseDatabase.getReference("Tasks");
                                    databaseReference.child(deletedTask.getTaskId()).setValue(deletedTask);
                                }
                            }).show();
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("Tasks");
                    databaseReference.child(deletedTask.getTaskId()).removeValue();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red))
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .addSwipeLeftLabel(getString(R.string.action_delete))
                    .setSwipeLeftLabelColor(Color.WHITE)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green))
                    .addSwipeRightActionIcon(R.drawable.ic_archive)
                    .addSwipeRightLabel(getString(R.string.action_archive))
                    .setSwipeRightLabelColor(Color.WHITE)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };

    private boolean onDurationDate(String startDate, String dueDate, String currentDate) {
        Date start = null, end = null, entry = null;
        try {
            start = new SimpleDateFormat("dd/MM/yyyy").parse(startDate);
            end = new SimpleDateFormat("dd/MM/yyyy").parse(dueDate);
            entry = new SimpleDateFormat("dd/MM/yyyy").parse(currentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ((entry.equals(start) || entry.after(start)) && entry.before(end));
    }

}
