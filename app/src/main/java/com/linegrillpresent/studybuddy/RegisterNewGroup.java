package com.linegrillpresent.studybuddy;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.List;

import sbrequest.SBRequestQueue;
import system.Course;
import system.UISystem;
import user.Student;

public class RegisterNewGroup extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner courseSpinner;
    private Spinner numSpinner;
    private ArrayList<String> courseNames;
    private Switch isPrivate;
    private EditText inviteCode;
    private EditText et_name;
    //private  course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new_group);
        final SBRequestQueue SBQueue = SBRequestQueue.getInstance(this);
        Button btm = (Button) findViewById(R.id.btm_RegisterGroup);
        et_name = (EditText) findViewById(R.id.et_GroupName);
        isPrivate = (Switch) findViewById(R.id.sw_private);
        inviteCode = (EditText) findViewById(R.id.et_inviteC);
        inviteCode.setVisibility(View.INVISIBLE);

        isPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean on){
                if(on) inviteCode.setVisibility(View.VISIBLE);
                else inviteCode.setVisibility(View.INVISIBLE);
            }
        });
        courseSpinner = (Spinner) findViewById(R.id.sp_courseNames);
        numSpinner = (Spinner) findViewById(R.id.sp_num);
        ArrayList<Course> course = UISystem.getInstance().getCourseNames(this);
        courseNames = new ArrayList<String>();

        for(int i = 0; i < course.size();i++) {
            if(!courseNames.contains(course.get(i).getName()))
                courseNames.add(course.get(i).getName());
        }

        Log.d("newgroup", Integer.toString(courseNames.size()));
        ArrayAdapter<String> arr_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseNames);
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(arr_adapter);
        courseSpinner.setOnItemSelectedListener(this);

        btm.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String url = generateURL();
                Log.d("newgroup", url);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                String resText = response;

                                if("failed".equals(resText)) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterNewGroup.this);
                                    builder.setMessage("Fail to create the group")
                                            .setNegativeButton("RETRY", null)
                                            .create()
                                            .show();
                                } else {
                                    /* success create
                                       Save the token in a Bundle object and pass it to the userMainActivity
                                     */
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterNewGroup.this);
                                    builder.setMessage("Create group success!")
                                            .setPositiveButton("Back",  new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do things
                                                    Intent mainpageIntent = new Intent(RegisterNewGroup.this, MainPage.class);
                                                    Student student = Student.getInstance();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("token", student.getToken());
                                                    mainpageIntent.putExtras(bundle);
                                                    RegisterNewGroup.this.startActivity(mainpageIntent);
                                                }
                                            })
                                            .create()
                                            .show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //need an empty override method
                    }
                });
                // Add the request to the RequestQueue
                SBQueue.addToRequestQueue(stringRequest);
            }
        });


    }

    private String generateURL() {
        String name = et_name.getText().toString();
        int privateOrNot;
        if(isPrivate.isChecked())
            privateOrNot = 1;
        else privateOrNot = 0;
        String inCode = inviteCode.getText().toString();

        String course_name = courseSpinner.getSelectedItem().toString();
        int course_num = Integer.parseInt(numSpinner.getSelectedItem().toString());
        int course_id = UISystem.getInstance().getCourseID(course_name, course_num);

        final Student student = Student.getInstance();

        String staticURL = getResources().getString(R.string.deployURL) + "group?";
        String url = staticURL + "token=" + student.getToken() + "&isPrivate=" + privateOrNot +
                "&groupName=" + name +
                "&inviteCode=" + inCode +
                "&courseId=" + course_id +
                "&action=createGroup";

        return url;
    }




    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id) {
//  设置第二个控件给定数据源绑定适配器
        String courseName = courseNames.get(position).trim();
        List<String> numbers = UISystem.getInstance().getAllCourseNum(courseName);
        Log.d("newgroup", Integer.toString(numbers.size()));
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                numbers);
        numSpinner.setAdapter(adapter2);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    //need an empty method to override
    }


}
