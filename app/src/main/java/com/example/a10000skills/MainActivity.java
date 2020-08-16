package com.example.a10000skills;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.a10000skills.data.SkillEntity;
import com.example.a10000skills.viewmodel.MainViewModel;
import com.example.a10000skills.viewmodel.MainViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SkillsRecyclerViewAdapter recyclerViewAdapter;
    private MainViewModel viewModel;
    private LiveData<List<SkillEntity>> skillsListLiveData;

    private MutableLiveData<List<SkillEntity>> selectedSkillsLData;

    private TextView titleView;

    private List<SkillEntity> selectedItems;

    private FloatingActionButton addSkillBtn;
    private FloatingActionButton deleteSkillsBtn;

    private DefaultClickListener defaultClickListener;
    private SelectionClickListener selectionClickListener;

    private final String LOG_TAG = "DEBUG_10000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Click listeners for recyclerview items
        selectionClickListener = new SelectionClickListener();
        defaultClickListener = new DefaultClickListener();

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdapter = new SkillsRecyclerViewAdapter();
        recyclerView.setAdapter(recyclerViewAdapter);

        // Set default click listeners
        recyclerViewAdapter.setItemClickListener(defaultClickListener);
        recyclerViewAdapter.setItemLongClickListener(defaultClickListener);

        // Views
        titleView = findViewById(R.id.toolbarTitleView);
        addSkillBtn = findViewById(R.id.addSkillBtn);
        deleteSkillsBtn = findViewById(R.id.deleteSkillsBtn);
        deleteSkillsBtn.hide();

        // ViewModel & LiveData
        viewModel = new ViewModelProvider(this, new MainViewModelFactory(this.getApplication())).get(MainViewModel.class);
        skillsListLiveData = viewModel.getSkills();

        selectedSkillsLData = viewModel.getSelectedSkillsLData();

        selectedItems = new ArrayList<>();



        // Update RecyclerView when chats are changed
        skillsListLiveData.observe(this, new Observer<List<SkillEntity>>() {
            @Override
            public void onChanged(@Nullable List<SkillEntity> skills) {

                if (skills != null) {

                    if (! skills.isEmpty()) {
                        recyclerViewAdapter.setItems(skills);
                    }
                }
            }
        });


        // Obsrver for selected items
        selectedSkillsLData.observe(this, new Observer<List<SkillEntity>>() {
            @Override
            public void onChanged(@Nullable List<SkillEntity> skills) {

                if (skills != null) {

                    if (! skills.isEmpty()) {
                        Log.d(LOG_TAG, "some items are selected: " + skills);

                        addSkillBtn.hide();
                        deleteSkillsBtn.show();

                        recyclerViewAdapter.setItemClickListener(selectionClickListener);
                        recyclerViewAdapter.setItemLongClickListener(selectionClickListener);


                    } else {
                        Log.d(LOG_TAG, "no items selected now");

                        deleteSkillsBtn.hide();
                        addSkillBtn.show();

                        recyclerViewAdapter.setItemClickListener(defaultClickListener);
                        recyclerViewAdapter.setItemLongClickListener(defaultClickListener);

                    }

                }
            }
        });

    }


    public void btnAddSkill(View v) {
        Log.d(LOG_TAG, "add skill btn pressed");

        // Creating "new skill" dialog
       AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

       // Use custom layout for dialog
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_create_skill, null);
        builder.setView(dialogView);

        // Disable canceling on touching outside
        builder.setCancelable(false);

        // Make final to use in inner class
        final AlertDialog dialog = builder.create();

        // Create button (textview)
        TextView createBtn = dialogView.findViewById(R.id.createBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get skill name
                EditText skillNameField = dialogView.findViewById(R.id.skillNameField);
                String skillName = skillNameField.getText().toString();

                // Create skill
                SkillEntity newSkill = new SkillEntity(skillName);
                viewModel.addSkill(newSkill);

                dialog.dismiss();
            }
        });

        // Cancel button (textview)
        TextView cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Show dialog
        dialog.show();
    }



    // Default click listener for recyclerview items
    class DefaultClickListener implements SkillsRecyclerViewAdapter.SkillClickListener,
                                             SkillsRecyclerViewAdapter.SkillLongClickListener {


        @Override
        public void onSkillClick(int skill_id, int position) {
            Log.d(LOG_TAG, "clicked position " + position + " id " + skill_id);

            Intent intent = new Intent(MainActivity.this, SkillActivity.class);
            intent.putExtra("REQUESTED_SKILL", skill_id);
            startActivity(intent);
        }

        @Override
        public void onLongSkillClick(int skill_id, int position) {
            Log.d(LOG_TAG, "clicked (LONG) position " + position + " id " + skill_id);

            recyclerViewAdapter.selectItem(position);
            selectedSkillsLData.postValue(recyclerViewAdapter.getSelectedItems());

            Log.d(LOG_TAG, selectedSkillsLData.getValue().toString());
        }
    }


    // Set if at least one item in RecyclerView is selected
    // Replace by default click listener when no items selected
    class SelectionClickListener implements SkillsRecyclerViewAdapter.SkillClickListener,
                                             SkillsRecyclerViewAdapter.SkillLongClickListener {


        @Override
        public void onSkillClick(int skill_id, int position) {
            Log.d(LOG_TAG, "SELECTED_CL: click item " + position + " skill_id " + skill_id);

            // Select item if not selected
            // Else unselect

            if (recyclerViewAdapter.checkIfItemSelected(position)) {
                recyclerViewAdapter.unselectItem(position);
            }
            else {
                recyclerViewAdapter.selectItem(position);
            }

            // Update livedata
            selectedSkillsLData.setValue(recyclerViewAdapter.getSelectedItems());

        }

        @Override
        public void onLongSkillClick(int skill_id, int position) {
            Log.d(LOG_TAG, "SELECTED_CL: LONG click item " + position + " skill_id " + skill_id);

            // Do nothing
            // May be changed later
        }
    }


}
