package com.lkrh.storescontrol.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lkrh.storescontrol.R;
import com.lkrh.storescontrol.bean.ArrivalHeadBean;
import com.lkrh.storescontrol.bean.ConfirmBean;
import com.lkrh.storescontrol.bean.LoginBean;

import com.lkrh.storescontrol.bean.WarehouseBean;
import com.lkrh.storescontrol.url.Request;
import com.lkrh.storescontrol.url.iUrl;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.GenericSignatureFormatError;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PutListActivity extends BaseActivity {
    RecyclerView recyclerView;
    private  FunctionAdapter functionAdapter;
    Button buttonsubmit;
    private ImageView imageViewreturn;
    TextView textViewtitle,textViewtotal;
    ArrayList<ArrivalHeadBean> arrivalHeadBeans=new ArrayList<>();
    SharedPreferences sharedPreferences;
    LoginBean.Menu menuBean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_put_list);
        menuBean=getIntent().getParcelableExtra("menubean");
        recyclerView=findViewById(R.id.rv_list);
        textViewtitle=findViewById(R.id.tv_title);
        buttonsubmit=findViewById(R.id.b_submit);
        textViewtotal=findViewById(R.id.tv_total);
        textViewtitle.setText(menuBean.getMenushowname()+"列表");

        imageViewreturn=findViewById(R.id.iv_return);
        imageViewreturn.setVisibility(View.VISIBLE);
        imageViewreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE);
        String data="";


        data= sharedPreferences.getString(menuBean.getMenucode()+"List","");
        if (!data.equals("")){

            try {
                Gson gson = new Gson();
                JsonArray arry = new JsonParser().parse(data).getAsJsonArray();
                for (JsonElement jsonElement : arry) {
                    arrivalHeadBeans.add(gson.fromJson(jsonElement, ArrivalHeadBean.class));
                }
                Log.i("arrivalHeadBeans",arrivalHeadBeans.size()+"");
                textViewtotal.setText("总计:"+arrivalHeadBeans.size()+"条");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }




        functionAdapter=new PutListActivity.FunctionAdapter(arrivalHeadBeans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(functionAdapter);
        buttonsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getIntent().getStringExtra("menuname").equals("货位调整") && company.equals("林肯SKF")) {
                    et = new EditText(PutListActivity.this);
                  alertDialog=new AlertDialog.Builder(PutListActivity.this).setTitle("请输入调入货位：")
                            .setIcon(android.R.drawable.sym_def_app_icon)
                            .setView(et)
                            .setPositiveButton("确定", null).setNegativeButton("取消",null).show();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                             if(isParse) {
                                alertDialog.dismiss();
                                putData();
                             }else {
                                 getData(et.getText().toString());
                             }
                        }
                    });

                }else {

                    putData();
                }


            }
        });

    }
     EditText et=null;
    AlertDialog alertDialog;
    Boolean isParse=false;
    private void getData(String code) {
        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("methodname","getBarcodeValues");
            jsonObject.put("usercode",usercode);
            jsonObject.put("acccode",acccode);
            jsonObject.put("menucode",menuBean.getMenucode());
            jsonObject.put("layout","2");
            jsonObject.put("barcode",code);
            jsonObject.put("ccode","");
            jsonObject.put("irowno","");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        String obj=jsonObject.toString();
        Log.i("request-->",obj);

        Call<ResponseBody> data = Request.getRequestbody(obj);
        data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {

                    JSONObject object=new JSONObject(response.body().string());
                    WarehouseBean   warehouseBean=new Gson().fromJson(object.toString(), WarehouseBean.class);
                    et.setText(warehouseBean.getFormdata().getCposition()+"\\"+warehouseBean.getFormdata().getCwhName());
                    arrivalHeadBeans.get(0).setInposition(warehouseBean.getFormdata().getCposition());
                    arrivalHeadBeans.get(0).setInwhcode(warehouseBean.getFormdata().getCwhcode());
                    isParse=true;


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void putData() {
        dialog.show();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        String strings=simpleDateFormat.format(date);
        if(getTime().isEmpty()) {
            setTime(strings);
        }

        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("methodname","updateDocVouch");
            jsonObject.put("usercode",usercode);
            jsonObject.put("acccode",acccode);
            jsonObject.put("menucode",menuBean.getMenucode());
            jsonObject.put("layout","1");
            jsonObject.put("button",buttonsubmit.getText().toString());
            jsonObject.put("condition",getTime());
            jsonObject.put("formdata",new Gson().toJson(arrivalHeadBeans));


        } catch (JSONException e) {
            e.printStackTrace();
        }
        String obj=jsonObject.toString();
        Log.i("requestbody-->",obj);
        Call<ResponseBody> data = Request.getRequestbody(obj);
        data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                dialog.dismiss();
                ConfirmBean bean= null;
                try {
                    bean = new Gson().fromJson(response.body().string(), ConfirmBean.class);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Log.i("bean-->",new Gson().toJson(bean));
                if(bean.getResultcode().equals("200")){
                    arrivalHeadBeans.clear();
                    functionAdapter.notifyDataSetChanged();
                    SharedPreferences sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE);
                    setTime("");

                    sharedPreferences.edit().putString(menuBean.getMenucode()+"List","").commit();
                    sharedPreferences.edit().putString(menuBean.getMenucode()+"Scan","").commit();

                }

                Toast.makeText(PutListActivity.this,bean.getResultMessage(),Toast.LENGTH_LONG).show();
                textViewtotal.setText("");
                finish();

            }
            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(PutListActivity.this,t.getMessage(),Toast.LENGTH_LONG).show();
            } });
    }

    private void setTime(String strings) {
        sharedPreferences.edit().putString(menuBean.getMenucode()+"Time", strings).commit();
    }
    private String getTime() {
        String stringscandata="";
        stringscandata=sharedPreferences.getString(menuBean.getMenucode()+"Time","");
        return stringscandata;

    }

    class FunctionAdapter extends RecyclerView.Adapter<PutListActivity.FunctionAdapter.VH>{

        @NonNull
        @Override
        public PutListActivity.FunctionAdapter.VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v=getLayoutInflater().inflate(R.layout.item_input,viewGroup,false);
            return new PutListActivity.FunctionAdapter.VH(v);


        }

        private List<ArrivalHeadBean> mDatas;
        public FunctionAdapter(List<ArrivalHeadBean> data) {
            this.mDatas = data;
        }

        @Override
        public void onBindViewHolder(@NonNull  PutListActivity.FunctionAdapter.VH vh,final int i) {
            vh.textViewnumber.setText(i+1+"");
            vh.textViewccode.setText(arrivalHeadBeans.get(i).getCpocode());
            vh.textViewline.setText(arrivalHeadBeans.get(i).getIrowno());
            vh.textViewcposcode.setText("货位："+arrivalHeadBeans.get(i).getCposition());
            vh.textViewmaterial.setText("料号："+arrivalHeadBeans.get(i).getcInvCode());
            vh.textViewbatch.setText("批号："+arrivalHeadBeans.get(i).getCbatch());
            vh.textViewcount.setText("数量："+arrivalHeadBeans.get(i).getIquantity());

            if(arrivalHeadBeans.get(i).getFile()!=null){
                vh.imageViewpic.setTag(i+"");
                Picasso.get().load(new File(arrivalHeadBeans.get(i).getFile())).into(vh.imageViewpic);
            }
            vh.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(PutListActivity.this);
                    builder.setTitle("提示").setMessage("删除此条数据").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPreferences = getSharedPreferences("sp", Context.MODE_PRIVATE);
                            //delete sharedPreferences->scan item
                            String stringscandata="";

                            stringscandata=sharedPreferences.getString(menuBean.getMenucode()+"Scan","");
                            List<String> listcode = new ArrayList<>(Arrays.asList(stringscandata));
                            for (int j = 0; j <listcode.size() ; j++) {
                                if(listcode.get(j).contains(arrivalHeadBeans.get(i).getIrowno())){
                                    listcode.remove(j);
                                }
                            }
                            arrivalHeadBeans.remove(i);
                            functionAdapter.notifyDataSetChanged();
                            textViewtotal.setText("总计:"+arrivalHeadBeans.size()+"条");
                            //delete sharedPreferences->putlist item
                            String strings= new Gson().toJson(arrivalHeadBeans);
                            Log.i("list-->",strings);
                            sharedPreferences.edit().putString(menuBean.getMenucode()+"List",strings).commit();
                            sharedPreferences.edit().putString(menuBean.getMenucode()+"Scan",listcode.toString()).commit();



                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
                }
            });


        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
        class  VH extends RecyclerView.ViewHolder{
            TextView textViewnumber,textViewccode,textViewline,textViewcposcode,textViewmaterial,textViewbatch,textViewcount;
            LinearLayout linearLayout;
            ImageView imageViewpic;
            public VH(@NonNull View itemView) {
                super(itemView);
                linearLayout=itemView.findViewById(R.id.l_input);
                textViewnumber=itemView.findViewById(R.id.tv_number);
                textViewccode=itemView.findViewById(R.id.tv_ccode);
                textViewline=itemView.findViewById(R.id.tv_line);
                textViewcposcode=itemView.findViewById(R.id.tv_cposcode);
                textViewmaterial=itemView.findViewById(R.id.tv_material);
                textViewbatch=itemView.findViewById(R.id.tv_batch);
                textViewcount=itemView.findViewById(R.id.tv_count);
                imageViewpic=itemView.findViewById(R.id.iv_pic);


            }
        }
    }


}
