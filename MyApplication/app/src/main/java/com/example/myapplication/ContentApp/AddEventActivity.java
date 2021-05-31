package com.example.myapplication.ContentApp;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapter.PhotoAdapter;
import com.example.myapplication.ImgFullscreenActivity;
import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AddEventActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_LOAD_FULLSCREEN_IMAGE = 2;
    private static final int RESULT_BACK = 2;
    private static final int RESULT_DELETE_IMAGE = 3;
    private static final String BUCKET = "Event Images";
    private static final String TBL_EVENTS = "Events";
    private static final String TBL_USERS = "Users";
    private static final String PROP_USER_EVENTS = "userEvents";
    private static final String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private DatabaseReference mDatabase;
    private AlertDialog.Builder confirmDialogBuilder;
    private Button backHomeBtn ;
    private Button pickStartDate;
    private Button pickEndDate;
    private Button pickerImg;
    private RecyclerView rcvPhoto;
    private PhotoAdapter photoAdapter;
    private EditText startDateET, endDateET, limitNumberET, eventNameET,descriptionET, placeET, editText1;
    private Switch unlimitChecker;
    public List<Uri> fileUriList;
    private Button saveBtn;
    private List<String> firebaseUriList;
    private RadioGroup eventTypeRG;
    int uploadImgCounter;
    private TextView placeTW;

    private String  eventName, description, place, startDate, endDate;
    private boolean isOnlineType;
    private int limitedNumber;
    private StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // mapping to UI component
        eventNameET = findViewById(R.id.eventNameET);
        descriptionET = findViewById(R.id.descriptionET);
        placeET = findViewById(R.id.placeET);
        pickStartDate = findViewById(R.id.PickerStartDate);
        pickEndDate = findViewById(R.id.PickerEndDate);
        startDateET = findViewById(R.id.startDateET);
        endDateET = findViewById(R.id.endDateET);
        unlimitChecker = findViewById(R.id.switchUnlimit);
        limitNumberET = findViewById(R.id.limitET);
        pickerImg = findViewById(R.id.pickerImage);
        rcvPhoto = findViewById(R.id.imgListView);
        saveBtn = findViewById(R.id.saveBtn);
        limitNumberET.setText("1");
        backHomeBtn = findViewById(R.id.backHomeBtn);
        eventTypeRG = findViewById(R.id.radioGroup);
        placeTW = findViewById(R.id.PlaceTW);
        isOnlineType = false;
        // init new object
        storageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fileUriList = new ArrayList<>();
        firebaseUriList = new ArrayList<>();

        buildRecyclerView();

        eventTypeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId){
                    case R.id.onlineRB:
                        placeTW.setText("Event Link");
                        isOnlineType = true;
                        break;
                    case R.id.offlineRB:
                        placeTW.setText("Place");
                        isOnlineType = false;
                        break;
                    default:
                        placeTW.setText("Place");
                }
            }
        });

        saveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                eventName = eventNameET.getText().toString();
                description = descriptionET.getText().toString();
                place = placeET.getText().toString();
                startDate = startDateET.getText().toString();
                endDate = endDateET.getText().toString();
                limitedNumber = Integer.parseInt(limitNumberET.getText().toString());

                    if(isEmptyField(eventName, description,place,startDate,endDate)) {
                        Toast.makeText(AddEventActivity.this, "Enter all field before saving Event", Toast.LENGTH_LONG).show();

                    }else {
                        try {
                            if (isOnlineType) {
                                if (!validateWebUrl(place)) {
                                    placeET.setError("Event Link not matches Web Url");
                                }
                                else if (!checkEndDate(startDate, endDate)) {
                                    endDateET.setError("End date is before Start date");
                                    //Toast.makeText(AddEventActivity.this, "End date is before Start date", Toast.LENGTH_LONG).show();
                                } else {
                                    savePost(eventName, description, place, startDate, endDate, limitedNumber, isOnlineType);
                                }
                            }else {
                                if (!checkEndDate(startDate, endDate)) {
                                    endDateET.setError("End date is before Start date");
                                    //Toast.makeText(AddEventActivity.this, "End date is before Start date", Toast.LENGTH_LONG).show();
                                } else {
                                    savePost(eventName, description, place, startDate, endDate, limitedNumber, isOnlineType);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
            }
        });

        pickerImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openBottomPicker();
            }
        });


        pickStartDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimeDialog(startDateET);
            }
        });

        pickEndDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimeDialog(endDateET);
            }
        });

        unlimitChecker.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    limitNumberET.setText("0");
                    limitNumberET.setVisibility(View.GONE);
                }else {
                    limitNumberET.setVisibility(View.VISIBLE);
                    limitNumberET.setText("1");
                }
            }
        });
        backHomeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                backHome();
            }
        });
    }
    private void buildRecyclerView(){
        photoAdapter = new PhotoAdapter(AddEventActivity.this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,3, LinearLayoutManager.VERTICAL, false);
        rcvPhoto.setLayoutManager(gridLayoutManager);
        rcvPhoto.setFocusable(false);
        rcvPhoto.setAdapter(photoAdapter);
        photoAdapter.setOnItemClickListener(new PhotoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Uri uri = fileUriList.get(position);
                Intent intent = new Intent(AddEventActivity.this, ImgFullscreenActivity.class);
                intent.putExtra("imgUri", uri);
                intent.putExtra("listPosition", position);
                //startActivity(intent);
                startActivityForResult(intent,REQUEST_LOAD_FULLSCREEN_IMAGE);
            }

            @Override
            public void onDeleteClick(int position) {
                confirmDeleteDialog(position);
            }
        });
    }
    private void backHome(){
        Intent intent = new Intent(this, BottomNavbarActivity.class);
        startActivity(intent);
    }
    // show Datetime dialog
    private void showDateTimeDialog( EditText editText){
        final Calendar calendar= Calendar.getInstance();
        OnDateSetListener dateSetListener = new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                OnTimeSetListener timeSetListener = new OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE,minute);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        editText.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                };
                new TimePickerDialog(AddEventActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false).show();
            }
        };
        new DatePickerDialog(AddEventActivity.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openBottomPicker(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"SELECT PICTURE"),RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            if(data.getClipData() !=null){
               int totalItemSelected = data.getClipData().getItemCount();
               for (int i=0; i<totalItemSelected; i++){
                   Uri fileUri = data.getClipData().getItemAt(i).getUri();
                   Log.d("ImgUri",fileUri.toString());
                   fileUriList.add(fileUri);
                   photoAdapter.setData(fileUriList);
                   photoAdapter.notifyDataSetChanged();
                }
            }else if(data.getData() !=null){
                Uri fileUri = data.getData();
                fileUriList.add(fileUri);
                photoAdapter.setData(fileUriList);
                photoAdapter.notifyDataSetChanged();
            }
        }
        else if(requestCode == REQUEST_LOAD_FULLSCREEN_IMAGE && resultCode == RESULT_BACK){
            Toast.makeText(this, "Wellcome back to Add Event.", Toast.LENGTH_SHORT);
        }
        else if(requestCode == REQUEST_LOAD_FULLSCREEN_IMAGE && resultCode == RESULT_DELETE_IMAGE){
            int position = data.getIntExtra("uriListPosition", -1);
            if(position ==- 1){
                Toast.makeText(AddEventActivity.this,"Couldn`t delete image", Toast.LENGTH_SHORT).show();
            }
            else {
                removeItem(position);
                Toast.makeText(AddEventActivity.this,"Deleted image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // savePost
    private void savePost(final String eventName, final String description, final String place, final String startDate, final String endDate, final int limitedNumber,final boolean isOnlineEventType){
        int totalImgFile = fileUriList.size();
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploaded 0/"+totalImgFile);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if(totalImgFile < 1 || fileUriList == null){
            Toast.makeText(AddEventActivity.this,"Please!! pick images", Toast.LENGTH_SHORT).show();
        }

        else {

           for (int i=0; i<totalImgFile; i++){
               Date date = new Date();
               String timestamp = String.valueOf(new Timestamp(date.getTime()).getTime());
               String fileName = "IMG_" + timestamp + ".jpg";
               StorageReference fileToUpload = storageRef.child(BUCKET).child(UID).child(fileName);
               fileToUpload.putFile(fileUriList.get(i)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                       if(task.isSuccessful()){
                           storageRef.child(BUCKET).child(UID).child(fileName).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                               @Override
                               public void onComplete(@NonNull Task<Uri> task) {
                                   uploadImgCounter++;
                                   progressDialog.setMessage("Uploaded: "+uploadImgCounter+"/"+totalImgFile);
                                    if (task.isSuccessful()){
                                        firebaseUriList.add(task.getResult().toString());
                                    }else {
                                        Toast.makeText(AddEventActivity.this," Please!!Check your netWork!!", Toast.LENGTH_SHORT).show();
                                    }
                                    if(uploadImgCounter == totalImgFile){
                                        saveDataToFirebase(eventName,description,place,startDate,endDate,limitedNumber,firebaseUriList, progressDialog,isOnlineEventType);
                                    }
                               }
                           });
                       }else {
                           uploadImgCounter++;
                           Toast.makeText(AddEventActivity.this,"Couldn`t upload image", Toast.LENGTH_SHORT).show();
                       }
                   }
               });
           }

        }
    }
    private void saveDataToFirebase(final String eventName, final String description, final String place, final String startDate, final String endDate, final int limitedNumber, List<String> firebaseUriList, ProgressDialog progressDialog, final boolean isOnlineEventType){
        progressDialog.setMessage("Saving Your Post.....");
        Date date = new Date();
        long timestamp = new Timestamp(date.getTime()).getTime();
        HashMap<String,Object> dataMap = new HashMap<>();
        dataMap.put("event_name", eventName);
        dataMap.put("description", description);
        dataMap.put("place", place);
        dataMap.put("start_date", startDate);
        dataMap.put("end_date", endDate);
        dataMap.put("Limit", limitedNumber);
        dataMap.put("uid", UID );
        dataMap.put("isOnline",isOnlineEventType);
        dataMap.put("ImgUri_list", firebaseUriList);
        dataMap.put("createdAt",timestamp);
        String eventId = UUID.randomUUID().toString();
        mDatabase.child(TBL_EVENTS).child(eventId).setValue(dataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
//                    Toast.makeText(AddEventActivity.this, "Event is successful posted!!", Toast.LENGTH_SHORT).show();
//                    progressDialog.dismiss();
                    String key = mDatabase.child(TBL_USERS).child(UID).child("userEvents").push().getKey();
                    HashMap<String, Object> userEventMap = new HashMap<>();
                    userEventMap.put(key, eventId);
                    mDatabase.child(TBL_USERS).child(UID).child("userEvents").updateChildren(userEventMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(AddEventActivity.this, "Event is successful posted!!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            backHome();

                        }
                    });
                    //backHome();
                }
            }
        });
    }
    private Boolean isEmptyField( String eventName,  String description,  String place,  String startDate,  String endDate){
        if(!TextUtils.isEmpty(eventName) && !TextUtils.isEmpty(description) && !TextUtils.isEmpty(place) && !TextUtils.isEmpty(startDate) && !TextUtils.isEmpty(endDate)){
            return false;
        }else {
            return true;
        }
    }
    private void removeItem(final int position){
        fileUriList.remove(position);
        photoAdapter.notifyDataSetChanged();
    }
    public void confirmDeleteDialog(final int position) {
        confirmDialogBuilder = new AlertDialog.Builder(this);
        confirmDialogBuilder.setMessage("Are you sure want to delete the Image")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeItem(position);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = confirmDialogBuilder.create();
        alertDialog.show();
    }
    private boolean checkEndDate(String startDateStr, String endDateStr) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date startDate = simpleDateFormat.parse(startDateStr.trim());
        Date endDate =  simpleDateFormat.parse(endDateStr.trim());
        if(endDate.before(startDate) || endDate.equals(startDate)){
            return false;
        }
        return true;
    }
    private boolean validateWebUrl(String webUrl){
        return Patterns.WEB_URL.matcher(webUrl).matches();
    }
}