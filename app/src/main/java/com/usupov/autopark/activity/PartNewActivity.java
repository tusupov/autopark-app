package com.usupov.autopark.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.usupov.autopark.R;
import com.usupov.autopark.config.CategoryRestURIConstants;
import com.usupov.autopark.http.HttpHandler;
import com.usupov.autopark.json.Car;
import com.usupov.autopark.json.Part;
import com.usupov.autopark.model.CarCategory;
import com.usupov.autopark.model.CarModel;
import com.usupov.autopark.model.CategoryPartModel;
import com.usupov.autopark.model.PartModel;
import com.usupov.autopark.service.SpeachRecogn;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PartNewActivity extends BasicActivity {

    static final int REQ_CODE_SPEECH_INPUT = 100;
    private static EditText numberPart;
    private static int carId;
    private final int RESULT_SPEECH = 200;
    private final int RESULT_CODE_ADD_PART = 300;

    private static int leftmargin = 20;
    private ProgressBar pbPart;
    private CarCategory one0;
    String carName;
    MyTask mt;
    private LinearLayout linearLayoutCatalog;
    private  ListView listViewParts;
    private EditText editTextArticle;
    private DrawerLayout drawerLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        if (!getIntent().hasExtra("carName")) {
            LayoutInflater inflater = (LayoutInflater) this.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View contenView = inflater.inflate(R.layout.activity_part_new, null, false);
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerLayout.addView(contenView, 0);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setCheckedItem(R.id.nav_part_add);

            List<CarModel> carList = Car.getCarList(getApplicationContext());
            if (carList == null || carList.isEmpty()) {
                Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(),
                        android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                startActivity(new Intent(PartNewActivity.this, CarListActivity.class), bundle);
                finish();
            }
            else if (carList.size()==1) {
                CarModel car = carList.get(0);
                car.setFullName();
                carId = car.getId();
                carName = car.getFullName();
            }
            else {
                carName = "";
                carId = 0;
            }
        }
        else {
            setContentView(R.layout.activity_part_new);
            carName = getIntent().getExtras().getString("carName");
            carId = getIntent().getExtras().getInt("carId");
            initToolbar();
        }

        initArticleEditText();
        initVoiceBtn();
        linearLayoutCatalog = (LinearLayout) findViewById(R.id.lvMain);
        listViewParts = (ListView) findViewById(R.id.lvParts);

        one0 = new CarCategory("", 0, 0);
        TextView carNameText = (TextView) findViewById(R.id.part_car_name);
        carNameText.setText(carName);
        pbPart = (ProgressBar) findViewById(R.id.pbParst);
        mt = new MyTask();
        mt.execute();


    }
    class MyTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            String jSonStringCategory = getJSONStringCategory(carId);
            if (jSonStringCategory==null)
                return false;
            go(one0, jSonStringCategory);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            super.onPostExecute(ok);
            pbPart.setVisibility(View.GONE);
            if (!ok) {
                Toast.makeText(PartNewActivity.this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
                return;
            }
            LinearLayout lvMain = (LinearLayout) findViewById(R.id.lvMain);
            one0.setLinearLayout(lvMain);
            dfs(one0, true);
        }
    }
    public void initArticleEditText() {
        editTextArticle = (EditText)findViewById(R.id.edittext_article_number);
        editTextArticle.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
//        editTextArticle.setRawInputType(Configuration.KEYBOARD_12KEY);

        editTextArticle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (editTextArticle.getText()==null || editTextArticle.getText().length()==0)
                    editTextArticle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_voice_black, 0);
                else
                    editTextArticle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_close, 0);
                String article = editTextArticle.getText()+"".toUpperCase();
                final List<PartModel> startsWithParts = Part.searchStartWith(carId, article.trim(), getApplicationContext());
                if (startsWithParts != null && startsWithParts.size() > 0) {
                    linearLayoutCatalog.setVisibility(View.GONE);
                    listViewParts.setVisibility(View.VISIBLE);
                    ArrayList<String> artciles = new ArrayList<>();
                    for (PartModel partModel : startsWithParts) {
                        artciles.add(partModel.getArticle());
                    }
                    System.out.println(artciles);
                    String[]names = new String[artciles.size()];
                    for (int i = 0; i < names.length; i++) {
                        names[i] = artciles.get(i);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, names);

                    listViewParts.setAdapter(adapter);

                    listViewParts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(PartNewActivity.this, PartFoundActivity.class);
                            Gson g = new Gson();
                            intent.putExtra("part", g.toJson(startsWithParts.get(position), PartModel.class));
                            intent.putExtra("car_full_name", carName);
                            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getBaseContext(),
                                    android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
                            startActivityForResult(intent, RESULT_CODE_ADD_PART, bundle);
                        }
                    });
                }
                else {
                    linearLayoutCatalog.setVisibility(View.VISIBLE);
                    listViewParts.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_part_new);
        toolbar.setTitle(getString(R.string.app_part_car));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    private void initVoiceBtn() {

        final EditText edt = (EditText) findViewById(R.id.edittext_article_number);
        edt.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {

                    if(event.getRawX() >= (edt.getRight() - edt.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (edt.getText()==null || edt.getText().length()==0) {
                            Intent intent = new Intent(PartNewActivity.this, RecognizerSampleActivity.class);
                            startActivityForResult(intent, RESULT_SPEECH);
                        }
                        else {
                            edt.setText("");
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }
    private String getJSONStringCategory (int carId) {
        String url = String.format(CategoryRestURIConstants.GET_TREE, carId);
        HttpHandler handler = new HttpHandler();
        String result = handler.doHttpGet(url, this).getBodyString();
        return result;
    }

    private void go(CarCategory parentCat, String result) {
        try {
            if (result==null)
                return;
            JSONArray array = new JSONArray(result);
            if (array==null || array.length()==0)
                return;
            for (int i = 0; i < array.length(); i++) {
                int id = array.getJSONObject(i).getInt("id");
                String name = array.getJSONObject(i).getString("name");
                String children = array.getJSONObject(i).getString("child");
                int percent = array.getJSONObject(i).getInt("percent");
                CarCategory curCategory = new CarCategory(name, id, percent);
                go(curCategory, children);
                parentCat.getChildren().add(curCategory);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data
                            .getStringArrayListExtra("all_results");
                    EditText edt = (EditText) findViewById(R.id.edittext_article_number);
                    String edt_text = SpeachRecogn.vinSpeach(text, this).toUpperCase();
                    edt_text = SpeachRecogn.partToNormal(edt_text);
                    if (edt_text.length() > 12)
                        edt_text = edt_text.substring(0, 12);
                    edt.setText(edt_text);
                }
                break;
            }
            case RESULT_CODE_ADD_PART: {
                if (resultCode==RESULT_OK) {
                    finish();
                    startActivity(getIntent());
                }
                break;
            }
        }
    }
    private void dfs(final CarCategory item, boolean first) {
        if (item.isRealised())
            return;

        if (!item.hasChildren()) {

        }
        else {
            final LayoutInflater inflater = getLayoutInflater();
            for (final CarCategory child : item.getChildren()) {
                final View childView = inflater.inflate(R.layout.row_part, item.getLinearLayout(), false);
                TextView image = (TextView) childView.findViewById(R.id.partItemImage);
                if (first) {
//                    ImageView image = (ImageView)childView.findViewById(R.id.partItemImage);
//                    image.setImageResource(R.drawable.ic_action_do_photo);
                    if (child.getPercent() > 0)
                        image.setText(child.getPercent() + "%");
                    else
                        image.setText("");
                }
                else
                    image.setVisibility(View.INVISIBLE);

                final TextView text = (TextView) childView.findViewById(R.id.partItemName);
                text.setText(child.getName());
//                childView.setVisibility(View.VISIBLE);
                item.getLinearLayout().addView(childView);
                child.setView(childView);

                final LinearLayout linearLayoutChild = new LinearLayout(this);
                child.setLinearLayout(linearLayoutChild);

                final LinearLayout.LayoutParams leftMarginParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                leftMarginParams.leftMargin = leftmargin;
                child.getLinearLayout().setVisibility(View.GONE);


                final ImageView arrowImage = (ImageView) childView.findViewById(R.id.partItemArrow);
//                if (child.hasChildren())
                arrowImage.setImageResource(R.drawable.ic_action_arrow_not_opened);


                childView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dfs(child, false);
                        child.click();
                        if (!child.hasChildren()) {

                            if (child.isFirstClick()) {
                                child.getLinearLayout().setVisibility(View.VISIBLE);
                                if (child.getCntClicks()==1) {
                                    List<CategoryPartModel> partList = Part.getCategoryPartsList(carId, child.getId(), getApplicationContext());

                                    final LinearLayout.LayoutParams leftMarginParams = new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    leftMarginParams.leftMargin = leftmargin ;

                                    for (final CategoryPartModel c : partList) {

                                        View view = inflater.inflate(R.layout.row_part, child.getLinearLayout(), false);
                                        TextView text = (TextView) view.findViewById(R.id.partItemName);
                                        text.setText(c.getTitle());
                                        TextView image = (TextView) view.findViewById(R.id.partItemImage);
                                        image.setVisibility(View.INVISIBLE);
                                        child.getLinearLayout().addView(view, leftMarginParams);

                                        text.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(PartNewActivity.this, PartFoundActivity.class);
                                                PartModel part = new PartModel();
                                                part.setCarId(carId);
                                                part.setCategoryId(child.getId());
                                                part.setTitle(c.getTitle());
                                                part.setId(c.getId());
                                                Gson g = new Gson();
                                                intent.putExtra("part", g.toJson(part, PartModel.class));
                                                intent.putExtra("car_full_name", carName);
                                                startActivityForResult(intent, RESULT_CODE_ADD_PART);
                                            }
                                        });
                                    }
                                }
                            }
                            else {
                                child.getLinearLayout().setVisibility(View.GONE);
                            }
                        }
                        if (child.isFirstClick()) {
                            child.getLinearLayout().setVisibility(View.VISIBLE);
//                            if (child.hasChildren()) {
                                //To do ic_action_arrow_opened
                                arrowImage.setImageResource(R.drawable.ic_action_arrow_opened);
                                text.setTypeface(null, Typeface.BOLD);
//                            }
                        }
                        else {
//                            if (child.hasChildren())
                                arrowImage.setImageResource(R.drawable.ic_action_arrow_not_opened);
                            child.getLinearLayout().setVisibility(View.GONE);
                            text.setTypeface(null, Typeface.NORMAL);
                        }
                    }
                });
                child.getLinearLayout().setOrientation(LinearLayout.VERTICAL);
                item.getLinearLayout().addView(child.getLinearLayout(), leftMarginParams);
            }
        }
        item.setRealised();
    }
}