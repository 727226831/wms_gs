package com.lkrh.storescontrol.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lkrh.storescontrol.R;
import com.lkrh.storescontrol.bean.ArrivalHeadBean;
import com.lkrh.storescontrol.bean.ConfirmlistBean;
import com.lkrh.storescontrol.bean.LoginBean;
import com.lkrh.storescontrol.bean.ProductBean;
import com.lkrh.storescontrol.bean.WarehouseBean;
import com.lkrh.storescontrol.databinding.ActivityProductionwarehousingBinding;
import com.lkrh.storescontrol.untils.iToast;
import com.lkrh.storescontrol.url.Request;
import com.lkrh.storescontrol.url.Untils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 生产/采购 入库
 */
public class ProductionwarehousingActivity extends BaseActivity {

    ActivityProductionwarehousingBinding binding;
    ArrivalHeadBean arrivalHeadBean;
    ConfirmlistBean confirmlistBean;
    List<String> list;
    int tag=-1;//0仓库 1料号
    private double base=0;//数量基数
    private  String cwhcode,cposition,inposition, inwhcode;
    File file;
    private  String stringScan; //扫描到的二维码
    SharedPreferences sharedPreferences;

    WarehouseBean warehouseBean;
    LoginBean.Menu menuBean;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=DataBindingUtil.setContentView(this, R.layout.activity_productionwarehousing);
        confirmlistBean=getIntent().getParcelableExtra("confirmlistBean");
        Untils.initTitle(getIntent().getStringExtra("menuname"),this);
        sharedPreferences=getSharedPreferences("sp",MODE_PRIVATE);
        menuBean=getIntent().getParcelableExtra("menubean");
        if(getIntent().getStringExtra("menuname").equals("生产入库")
           ||getIntent().getStringExtra("menuname").equals("其他入库")
                ||getIntent().getStringExtra("menuname").equals("其他出库")
        ){
            binding.lCvenabbname.setVisibility(View.GONE);
            binding.rlIquantityedit.setVisibility(View.VISIBLE);
            binding.rlIquantity.setVisibility(View.GONE);
        }else if(getIntent().getStringExtra("menuname").equals("材料出库")){
            binding.lCvenabbname.setVisibility(View.GONE);
            binding.rlIquantity.setVisibility(View.GONE);
            binding.rlIquantityedit.setVisibility(View.VISIBLE);
            if(company.equals("林肯SKF")) {
                binding.bSearch.setVisibility(View.VISIBLE);
                binding.tvTotal.setVisibility(View.VISIBLE);
            }else {
                binding.bSearch.setVisibility(View.GONE);
                binding.tvTotal.setVisibility(View.GONE);
            }

        }else if(getIntent().getStringExtra("menuname").equals("销售发货")){
            binding.lCvenabbname.setVisibility(View.GONE);
            binding.rlUpdate.setVisibility(View.GONE);
            binding.rlIquantity.setVisibility(View.GONE);
            binding.rlIquantityedit.setVisibility(View.VISIBLE);

            if(company.equals("浦东瀚氏")) {
                binding.rlCustomer.setVisibility(View.VISIBLE);

                binding.rlTransport.setVisibility(View.VISIBLE);
                binding.rlMemo.setVisibility(View.VISIBLE);
            }else {
                binding.bSearch.setVisibility(View.GONE);
                binding.tvTotal.setVisibility(View.GONE);
            }

        }else if(getIntent().getStringExtra("menuname").equals("委外发料")){
            binding.lCvenabbname.setVisibility(View.GONE);
            binding.rlUpdate.setVisibility(View.GONE);
        }else  if(getIntent().getStringExtra("menuname").equals("采购入库")){
            if(company.equals("瑞格菲克斯")){
                binding.rlIquantityedit.setVisibility(View.VISIBLE);
                binding.rlIquantity.setVisibility(View.GONE);

            }

        }else  if(getIntent().getStringExtra("menuname").equals("到货入库")){

        }else  if(getIntent().getStringExtra("menuname").equals("到货确认")){
            binding.rlUpdate.setVisibility(View.GONE);
            binding.rlCwhcode.setVisibility(View.GONE);
        }else  if(getIntent().getStringExtra("menuname").equals("产成品入库")){
            binding.lCvenabbname.setVisibility(View.GONE);

            binding.rlUpdate.setVisibility(View.GONE);
        }else  if(getIntent().getStringExtra("menuname").equals("货位调整")){
            binding.lCvenabbname.setVisibility(View.GONE);
            binding.rlCwhcode.setVisibility(View.GONE);
            binding.lCposition.setVisibility(View.VISIBLE);
            if(company.equals("瑞格菲克斯")){
                binding.rlUpdate.setVisibility(View.VISIBLE);
                binding.rlCwhcode.setVisibility(View.VISIBLE);
            }
        }else  if(getIntent().getStringExtra("menuname").equals("采购到货")){
            binding.rlCdefine10.setVisibility(View.VISIBLE);
            binding.rlUpdate.setVisibility(View.GONE);
            binding.tvCcodekey.setText("采购订单号：");
            binding.rlCwhcode.setVisibility(View.GONE);
        }else  if(getIntent().getStringExtra("menuname").equals("存货盘点")){
            binding.rlIquantityedit.setVisibility(View.VISIBLE);
            binding.rlIquantity.setVisibility(View.GONE);
        } else  if(getIntent().getStringExtra("menuname").equals("补料发料")){
            binding.rlIquantityedit.setVisibility(View.VISIBLE);
            binding.rlIquantity.setVisibility(View.GONE);
        } else  if(getIntent().getStringExtra("menuname").equals("采购退料")){
            binding.rlIquantityedit.setVisibility(View.VISIBLE);
            binding.rlIquantity.setVisibility(View.GONE);
            binding.lCpocode.setVisibility(View.VISIBLE);
        }else  if(getIntent().getStringExtra("menuname").equals("库存调拨")){
            binding.lCvenabbname.setVisibility(View.GONE);
            binding.rlUpdate.setVisibility(View.VISIBLE);
            binding.rlIquantityedit.setVisibility(View.VISIBLE);
            binding.rlIquantity.setVisibility(View.GONE);
            if(company.equals("浦东瀚氏")){
                    binding.rlTransport.setVisibility(View.VISIBLE);
                    binding.rlMemo.setVisibility(View.VISIBLE);

            }
        }else if(getIntent().getStringExtra("menuname").equals("销售出库")){
            binding.lCvenabbname.setVisibility(View.GONE);
            binding.rlIquantity.setVisibility(View.GONE);

            if(company.equals("浦东瀚氏")){
                binding.lPlate.setVisibility(View.VISIBLE);
                binding.lDock.setVisibility(View.VISIBLE);
                binding.bSearch.setVisibility(View.VISIBLE);
                binding.tvTotal.setVisibility(View.VISIBLE);
                binding.rlIquantity.setVisibility(View.VISIBLE);
            }else {
                binding.bSearch.setVisibility(View.GONE);
                binding.tvTotal.setVisibility(View.GONE);
            }



        }else if(getIntent().getStringExtra("menuname").equals("备货调拨")){
            binding.lCvenabbname.setVisibility(View.GONE);
            binding.rlUpdate.setVisibility(View.VISIBLE);
            binding.rlIquantityedit.setVisibility(View.VISIBLE);
            binding.rlIquantity.setVisibility(View.GONE);
            binding.rlTransport.setVisibility(View.VISIBLE);
            binding.rlMemo.setVisibility(View.VISIBLE);


        }





        binding.etTimes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().isEmpty()){
                    int times;
                    times = Integer.parseInt(s.toString());
                    if (times < 1) {
                        times=1;
                        binding.etTimes.setText(times+"");
                        Toast.makeText(ProductionwarehousingActivity.this, "此值必须大于1", Toast.LENGTH_LONG).show();
                    }
                    changeIquantity(times);
                }
            }
        });
        binding.etCwhcode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().isEmpty()){
                    binding.ivClear.setVisibility(View.GONE);
                }else {
                    binding.ivClear.setVisibility(View.VISIBLE);
                }

            }
        });
        binding.etCwhcode.setOnKeyListener(onKeyListener);
        binding.etUpdatecwhcode.setOnKeyListener(onKeyListener);
        binding.etBatch.setOnKeyListener(onKeyListener);
        binding.etPlate.setOnKeyListener(onKeyListener);
        binding.etDock.setOnKeyListener(onKeyListener);

        binding.ivCwhcode.setOnClickListener(onClickListener);
        binding.ivPlate.setOnClickListener(onClickListener);
        binding.ivUpdatecwhcode.setOnClickListener(onClickListener);
        binding.ivBatch.setOnClickListener(onClickListener);
        binding.ivDock.setOnClickListener(onClickListener);
        binding.bSubmit.setOnClickListener(onClickListener);
        binding.bSearch.setOnClickListener(onClickListener);
        binding.ivAdd.setOnClickListener(onClickListener);
        binding.ivMinus.setOnClickListener(onClickListener);
        binding.etIquantity.setOnKeyListener(onKeyListener);
        binding.ivTransport.setOnClickListener(onClickListener);
        binding.ivClear.setOnClickListener(onClickListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        //扫码
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() != null) {
                String code=result.getContents();
                Log.i("scan",code);

                switch (tag){
                    case 0://仓位
                        if(code.contains("$")){
                            Toast.makeText(ProductionwarehousingActivity.this,"类型错误",Toast.LENGTH_LONG).show();
                        }else {
                            binding.etCwhcode.setText(code);
                           getBarcodeValues(code,0);
                        }
                        break;
                    case 1://存货编码
                            stringScan=code;
                            binding.etBatch.setText(code);

                            getBarcodeValues(stringScan,1);
                            binding.etTimes.setText("1");

                        break;
                    case 2:
                        //调入货位
                            binding.etUpdatecwhcode.setText(code);
                           getBarcodeValues(code,2);
                        break;
                    case 3:
                        binding.etCustomer.setText(code);
                        break;
                    case 4:
                        binding.etTransport.setText(code);
                        break;
                    case 5:
                        binding.etPlate.setText(code);
                        break;
                    case 6:

                        binding.etDock.setText(code);
                        break;


                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }


    }






    View.OnKeyListener onKeyListener=new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (v.getId()) {
                    case R.id.et_cwhcode:
                        if(binding.etCwhcode.getText().toString().contains("仓库")){
                            Toast.makeText(ProductionwarehousingActivity.this,"如需" +
                                    "重新查询，请清空该项所有字符",Toast.LENGTH_LONG).show();
                            break;
                        }
                       getBarcodeValues(binding.etCwhcode.getText().toString(),0);
                        break;
                    case R.id.et_batch:
                        stringScan=binding.etBatch.getText().toString();


                         getBarcodeValues(stringScan,1);
                        binding.etTimes.setText("1");
                        break;
                    case R.id.et_updatecwhcode:
                        if(binding.etUpdatecwhcode.getText().toString().contains("仓库")){
                            Toast.makeText(ProductionwarehousingActivity.this,"如需" +
                                    "重新查询，请清空该项所有字符",Toast.LENGTH_LONG).show();
                            break;
                        }
                       getBarcodeValues(binding.etUpdatecwhcode.getText().toString(),2);
                        break;
                    case R.id.et_iquantity:
                        arrivalHeadBean.setIquantity(binding.etIquantity.getText().toString());
                        break;
                    case  R.id.et_customer:
                        arrivalHeadBean.setCustomer(binding.etCustomer.getText().toString());
                        break;


                }
            }

            return false;
        }
    };
    //0 position 1num
    private void getBarcodeValues(String code, final int type) {

        final JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("methodname","getBarcodeValues");
            jsonObject.put("usercode",usercode);
            jsonObject.put("acccode",acccode);
            jsonObject.put("menucode",menuBean.getMenucode());

            if(getIntent().getStringExtra("menuname").equals("委外发料")){
                jsonObject.put("layout","3");
            }else {
                jsonObject.put("layout","1");
            }
            jsonObject.put("barcode",code);
            if(type==1 && company.equals("浦东瀚氏") &&getIntent().getStringExtra("menuname").equals("销售出库") ){
                jsonObject.put("ccode",binding.etDock.getText().toString());
            }else {
                jsonObject.put("ccode","");
            }

            jsonObject.put("irowno","");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String obj=jsonObject.toString();
        Log.i("request-->",obj);

        Call<ResponseBody> data = Request.getRequestbody(obj);
        data.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {

                   JSONObject object=new JSONObject(response.body().string());
                   switch (type){
                       case 0:

                           warehouseBean=new Gson().fromJson(object.toString(),WarehouseBean.class);
                           if(warehouseBean.getResultcode().equals("200")) {
                               cwhcode = warehouseBean.getFormdata().getCwhcode();
                               cposition = warehouseBean.getFormdata().getCposition();
                               binding.etCwhcode.setText(warehouseBean.getFormdata().getCposition() + "\\" + warehouseBean.getFormdata().getCwhName());
                           }else {
                               Toast.makeText(ProductionwarehousingActivity.this,warehouseBean.getResultMessage(),Toast.LENGTH_LONG).show();
                           }
                           break;
                       case 1:
                           initData();
                           ProductBean productBean=new Gson().fromJson(object.toString(),ProductBean.class);
                           if(!productBean.getResultcode().equals("200")){
                               Toast.makeText(ProductionwarehousingActivity.this,productBean.getResultMessage(),Toast.LENGTH_LONG).show();
                               return;
                           }else {
                               arrivalHeadBean=productBean.getFormdata();
                           }


                            if(getIntent().getStringExtra("menuname").equals("库存调拨")){

                                if(binding.etUpdatecwhcode.getText().toString().isEmpty() ||
                                        binding.etCwhcode.getText().toString().isEmpty()){
                                    Toast.makeText(ProductionwarehousingActivity.this,"请先扫描仓位！",Toast.LENGTH_LONG).show();
                                    binding.etBatch.setText("");

                                    return;
                                }
                            }else {
                                if(binding.rlCwhcode.getVisibility()==View.VISIBLE && warehouseBean==null){
                                    Toast.makeText(ProductionwarehousingActivity.this,"请先扫描仓位！",Toast.LENGTH_LONG).show();
                                    binding.etBatch.setText("");
                                    return;
                                }
                            }
                           arrivalHeadBean.setCwhcode(cwhcode);
                           arrivalHeadBean.setCposition(cposition);
                           if(getIntent().getStringExtra("menuname").equals("委外发料")
                                   || getIntent().getStringExtra("menuname").equals("销售发货")
                                   || getIntent().getStringExtra("menuname").equals("材料出库")
                           ) {

                               arrivalHeadBean.setIrowno(confirmlistBean.getField10value());
                               arrivalHeadBean.setCcode(confirmlistBean.getField9value());
                           }

                           base=Double.parseDouble(arrivalHeadBean.getIquantity());
                           if(getIntent().getStringExtra("menuname").equals("库存调拨")){
                               arrivalHeadBean.setInposition(inposition);
                               arrivalHeadBean.setInwhcode(inwhcode);
                           }


                           binding.setBean(arrivalHeadBean);

                           if(binding.lPlate.getVisibility()==View.VISIBLE){
                               binding.tvNumber.setText(arrivalHeadBean.getIquantity());
                               arrivalHeadBean.setPlate(binding.etPlate.getText().toString());
                           }else {
                               if(binding.rlIquantity.getVisibility()==View.VISIBLE){
                                   binding.tvNumber.setText(arrivalHeadBean.getIquantity());
                                   submit();
                               }else {
                                   binding.etIquantity.setText(arrivalHeadBean.getIquantity());

                               }
                           }
                           //根据数量编辑控件是否显示来判断是否自动添加





                           break;
                       case 2:
                           warehouseBean=new Gson().fromJson(object.toString(),WarehouseBean.class);
                           inwhcode =warehouseBean.getFormdata().getCwhcode();
                           inposition=warehouseBean.getFormdata().getCposition();
                           binding.etUpdatecwhcode.setText(warehouseBean.getFormdata().getCposition()+"\\"+warehouseBean.getFormdata().getCwhName());
                           break;
                   }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }


    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int times;//数量倍数
            switch (v.getId()){

                case R.id.iv_cwhcode:
                    tag=0;
                    openScan();
                    break;
                case  R.id.iv_batch:
                    tag=1;
                    openScan();
                    break;

                case R.id.iv_updatecwhcode:
                    tag=2;
                    openScan();
                    break;
                case R.id.iv_customer:
                    tag=3;
                    openScan();
                    break;
                case R.id.iv_transport:
                    tag=4;
                    openScan();
                    break;
                case R.id.iv_plate:
                    tag=5;
                    openScan();
                    break;
                case R.id.iv_dock:
                    tag=6;
                    openScan();
                    break;
                case R.id.iv_add:
                    times=Integer.parseInt(binding.etTimes.getText().toString());
                    binding.etTimes.setText(times+1+"");
                    changeIquantity(times+1);
                    break;
                case R.id.iv_minus:
                    times=Integer.parseInt(binding.etTimes.getText().toString());
                    if(times>1) {
                        binding.etTimes.setText(times - 1+"");
                        changeIquantity(times-1);
                    }
                    break;

                case R.id.b_search:
                    Intent intent=new Intent(ProductionwarehousingActivity.this,PutListActivity.class);
                    intent.putExtra("menuname",getIntent().getStringExtra("menuname"));
                    intent.putExtra("menubean",menuBean);
                    if(binding.rlCdefine10.getVisibility()==View.VISIBLE){
                        intent.putExtra("cdefine10",binding.etCdefine10.getText().toString());
                    }
                    startActivity(intent);
                    break;
                case R.id.b_submit:

                    if(company.equals("浦东瀚氏")){
                        if(getIntent().getStringExtra("menuname").equals("库存调拨")||
                                getIntent().getStringExtra("menuname").equals("销售发货")){
                            if(binding.etTransport.getText().toString().isEmpty()){
                                Toast.makeText(ProductionwarehousingActivity.this, "承运商不能为空", Toast.LENGTH_LONG).show();
                                return;
                            }else {
                                arrivalHeadBean.setTransport(binding.etTransport.getText().toString());
                                arrivalHeadBean.setMemo(binding.etMemo.getText().toString());
                            }
                        }
                    }
                    if(binding.rlCustomer.getVisibility()==View.VISIBLE){

                        if(binding.etCustomer.getText().toString().isEmpty()){
                            Toast.makeText(ProductionwarehousingActivity.this, "客户字段不能为空", Toast.LENGTH_LONG).show();
                            return;
                        }else {
                            arrivalHeadBean.setCustomer(binding.etCustomer.getText().toString());
                        }
                    }
                    if(binding.lPlate.getVisibility()==View.VISIBLE){
                        if(binding.etPlate.getText().toString().isEmpty()){
                            Toast.makeText(ProductionwarehousingActivity.this, "车码不能为空", Toast.LENGTH_LONG).show();
                            return;
                        }else {
                            arrivalHeadBean.setPlate(binding.etPlate.getText().toString());
                        }
                    }
                   submit();


                    break;


                case R.id.iv_clear:
                    binding.etCwhcode.setText("");
                    break;
            }
        }









        private void openScan() {

            new IntentIntegrator(ProductionwarehousingActivity.this)
                    .setCaptureActivity(ScanActivity.class)
                    .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)// 扫码的类型,可选：一维码，二维码，一/二维码
                    .setPrompt("请对准二维码")// 设置提示语
                    .setCameraId(0)// 选择摄像头,可使用前置或者后置
                    .setBeepEnabled(false)// 是否开启声音,扫完码之后会"哔"的一声
                    .setBarcodeImageEnabled(true)// 扫完码之后生成二维码的图片
                    .initiateScan();// 初始化扫码

        }
    };

    private void submit() {


        if(binding.etTimes.getText().toString().isEmpty()) {
            binding.etTimes.setText("1");
            Toast.makeText(ProductionwarehousingActivity.this, "数量倍数值必须大于1", Toast.LENGTH_LONG).show();
            changeIquantity(1);
        }
        if(arrivalHeadBean!=null) {
            if(getIntent().getStringExtra("menuname").equals("补料发料")) {
                if (!confirmlistBean.getField5value().equals(stringScan)) {
                    Toast.makeText(ProductionwarehousingActivity.this, "条码不一致！", Toast.LENGTH_LONG).show();
                    return;
                }
                arrivalHeadBean.setInposition(confirmlistBean.getField9value());
            }

            if(binding.rlIquantity.getVisibility()==View.VISIBLE){
                arrivalHeadBean.setIquantity(binding.tvNumber.getText().toString());
            }else {
                arrivalHeadBean.setIquantity(binding.etIquantity.getText().toString());

            }
            if(getIntent().getStringExtra("category")!=null) {
                arrivalHeadBean.setCategory(getIntent().getStringExtra("category"));
            }
            if(getIntent().getStringExtra("menuname").equals("采购退料")) {
                if(binding.etCpocode.getText().toString().isEmpty()){
                    Toast.makeText(ProductionwarehousingActivity.this, "采购单号不能为空", Toast.LENGTH_LONG).show();
                    return;
                }else {
                   arrivalHeadBean.setCpocode(binding.etCpocode.getText().toString());
                }
            }
            SharedPreferences sharedPreferences=getSharedPreferences(menuBean.getMenucode()+"Detail",0);
            String data=sharedPreferences.getString("data","");
            List<ConfirmlistBean> list=new ArrayList<>();
            if(!data.isEmpty()){
                JsonArray arry = new JsonParser().parse(data).getAsJsonArray();
                for (JsonElement jsonElement : arry) {
                    list.add(new Gson().fromJson(jsonElement, ConfirmlistBean.class));
                }
            }

            if(company.equals("瑞格菲克斯") && getIntent().getStringExtra("menuname").equals("销售发货")
                    ||getIntent().getStringExtra("menuname").equals("材料出库")
                    ||getIntent().getStringExtra("menuname").equals("备货调拨")
            ){
                Boolean isVerification=false;
                for (int i = 0; i <list.size() ; i++) {

                    if(
                            list.get(i).getField1value().equals(arrivalHeadBean.getCInvCode())
                                    && list.get(i).getField5value().equals(arrivalHeadBean.getCbatch())
                                    && list.get(i).getField2value().equals(arrivalHeadBean.getCposition())
                    ){
                        ConfirmlistBean confirmlistBean=list.get(i);
                        isVerification=true;
                        arrivalHeadBean.setIrowno(list.get(i).getField10value());
                        arrivalHeadBean.setCcode(list.get(i).getField9value());

                        if(getIntent().getStringExtra("menuname").equals("备货调拨")){
                            updateList(confirmlistBean);
                            if(Integer.parseInt(confirmlistBean.getField8value())<1){
                                Toast.makeText(ProductionwarehousingActivity.this, "当前货物已达到需求数量！", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }else {
                            int current=Integer.parseInt(list.get(i).getField7value());
                            int total=Integer.parseInt(list.get(i).getField6value());
                            int add=Integer.parseInt(arrivalHeadBean.getIquantity());
                            if(current+add>total){
                                Toast.makeText(ProductionwarehousingActivity.this, "数量不能大于需求数量！", Toast.LENGTH_LONG).show();
                                return;

                            }else {

                                updateList(confirmlistBean);
                                if(Integer.parseInt(confirmlistBean.getField8value())==0){
                                    Toast.makeText(ProductionwarehousingActivity.this, "当前货物已达到需求数量！", Toast.LENGTH_LONG).show();
                                    finish();
                                }

                            }
                        }

                    }
                }

                if(!isVerification){
                    Toast.makeText(ProductionwarehousingActivity.this, "未能匹配发货单！", Toast.LENGTH_LONG).show();
                    return;
                }

            }

            setList();
        }
    }

    private void updateList(ConfirmlistBean confirmlistBean) {
        int current=Integer.parseInt(confirmlistBean.getField7value());
        int add=Integer.parseInt(arrivalHeadBean.getIquantity());
        current=current+add;
        confirmlistBean.setField7text("已扫数量："+current);
        confirmlistBean.setField7value(current+"");
        int undone=Integer.parseInt( confirmlistBean.getField8value());
        undone=undone-add;
        confirmlistBean.setField8value(undone+"");
        confirmlistBean.setField8text("未扫数量："+undone);
        sharedPreferences.edit().putString("data",new Gson().toJson(list)).commit();
    }


    private void changeIquantity(int times) {
        if(arrivalHeadBean!=null){

            BigDecimal i=new BigDecimal(base);
            BigDecimal time=new BigDecimal(times);

            binding.etIquantity.setText(time.multiply(i)+"");

        }
    }
    /**
     * 制造入库列表
     */
    private void setList() {

        String stringarrivalHeadBeans="";
        stringarrivalHeadBeans= sharedPreferences.getString(menuBean.getMenucode()+"List", "");



        ArrayList<ArrivalHeadBean> arrivalHeadBeans = new ArrayList<>();
        if (!stringarrivalHeadBeans.equals("")) {
            Gson gson = new Gson();
            JsonArray arry = new JsonParser().parse(stringarrivalHeadBeans).getAsJsonArray();
            for (JsonElement jsonElement : arry) {
                arrivalHeadBeans.add(gson.fromJson(jsonElement, ArrivalHeadBean.class));
            }
        }

        if (arrivalHeadBean.getCwhcode() == null) {

            if(binding.rlCwhcode.getVisibility()==View.VISIBLE){
                Toast.makeText(ProductionwarehousingActivity.this, "仓库不能为空", Toast.LENGTH_LONG).show();
                return;
            }

        }
        if(stringScan==null){
            return;
        }
        String stringscandata="";

        stringscandata= sharedPreferences.getString(menuBean.getMenucode()+"Scan", "");

        if(!company.equals("瑞格菲克斯")){
            if(stringscandata.contains(stringScan) ){
                Toast.makeText(ProductionwarehousingActivity.this, "此二维码数据已添加", Toast.LENGTH_LONG).show();
                binding.etBatch.setText("");
                return;
            }
        }




        binding.tvTotal.setText("总计："+arrivalHeadBeans.size()+"条");
        //update sharedPreferences->putlist item
        if(getIntent().getStringExtra("menuname").equals("货位调整")||
                getIntent().getStringExtra("menuname").equals("库存调拨")){
            arrivalHeadBean.setInposition(inposition);
        }
        if(getIntent().getStringExtra("menuname").equals("销售发货")){
            arrivalHeadBean.setCustomer(binding.etCustomer.getText().toString());

        }




        arrivalHeadBeans.add(arrivalHeadBean);
        String strings = new Gson().toJson(arrivalHeadBeans);

        sharedPreferences.edit().putString(menuBean.getMenucode()+"List", strings).commit();

        if(company.equals("林肯SKF")) {
            iToast.showToast(ProductionwarehousingActivity.this,"已添加至列表！",1000);
        }



        //update sharedPreferences->scan item
        List<String> listscan=new ArrayList<>();
        if(stringscandata.equals("")){
            listscan.add(stringScan);
        }else {
            listscan=new ArrayList<>(Arrays.asList(stringscandata));
            listscan.add(stringScan);
        }


        getCount();

        sharedPreferences.edit().putString(menuBean.getMenucode()+"Scan",listscan.toString()).commit();
       // initData();

    }

    private void initData() {
        arrivalHeadBean=null;
        binding.setBean(arrivalHeadBean);
        binding.etBatch.setText("");


        file=null;
        binding.etBatch.setFocusable(true);
        binding.etBatch.setFocusableInTouchMode(true);
        binding.etBatch.requestFocus();


    }



    @Override
    protected void onStart() {
        super.onStart();

        getCount();
    }
    //获取已加入清单量
    private void getCount() {
        ArrayList<ArrivalHeadBean> arrivalHeadBeans = new ArrayList<>();
        String stringarrivalHeadBeans="";

        stringarrivalHeadBeans = sharedPreferences.getString(menuBean.getMenucode()+"List", "");
        if (!stringarrivalHeadBeans.equals("")) {
            Gson gson = new Gson();
            JsonArray arry = new JsonParser().parse(stringarrivalHeadBeans).getAsJsonArray();
            for (JsonElement jsonElement : arry) {
                arrivalHeadBeans.add(gson.fromJson(jsonElement, ArrivalHeadBean.class));
            }
        }
        binding.tvTotal.setText("总计："+arrivalHeadBeans.size()+"条");
    }

}
