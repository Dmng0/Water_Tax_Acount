package kr.water.calculator;

import android.app.*;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;
import android.text.*;
import org.json.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.YearMonth;
import java.util.*;

public class MainActivity extends Activity {
    private static final int BG=0xff101417, CARD=0xff1c2429, TEXT=0xffedf4f4, MUTED=0xff9cabb2, ACCENT=0xff70deda;
    private final ArrayList<House> houses=new ArrayList<>();
    private JSONArray history=new JSONArray();
    private String rate="", fee="", month=YearMonth.now().toString();
    private boolean configured=false, ready=false;
    private SharedPreferences prefs;
    private LinearLayout root, houseList;
    private TextView summary, monthLabel, rateLabel;
    private final DecimalFormat money=new DecimalFormat("#,##0");
    private static class House {
        String id=UUID.randomUUID().toString(), name="", previous="", current="";
        TextView result;
        JSONObject json() throws JSONException { return new JSONObject().put("id",id).put("name",name).put("previous",previous).put("current",current); }
        static House read(JSONObject o) { House h=new House(); h.id=o.optString("id",h.id);h.name=o.optString("name","우리 집");h.previous=o.optString("previous");h.current=o.optString("current");return h; }
    }
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs=getSharedPreferences("water-v1",MODE_PRIVATE);
        try {
            JSONObject data=new JSONObject(prefs.getString("state","{}"));
            rate=data.optString("rate");fee=data.optString("fee");configured=data.optBoolean("configured");
            month=data.optString("month",month);
            JSONArray a=data.optJSONArray("houses");
            if(a!=null) for(int i=0;i<a.length();i++) houses.add(House.read(a.getJSONObject(i)));
            if(data.optJSONArray("history")!=null) history=data.getJSONArray("history");
        } catch(JSONException e) { toast("저장 데이터를 읽지 못했습니다. 앱을 종료한 뒤 다시 확인해 주세요."); finish(); return; }
        if(houses.isEmpty()) {House h=new House();h.name="우리 집";houses.add(h);}
        ready=true;render();
        if(!configured) root.post(()->settings(true));
    }
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(5),0,dp(5));return t;}
    private TextView title(String s,int size){TextView t=label(s,size,TEXT);t.setTypeface(null,Typeface.BOLD);return t;}
    private Button button(String text, boolean primary){Button b=new Button(this);b.setText(text);b.setTextSize(14);b.setAllCaps(false);b.setTextColor(primary?BG:ACCENT);b.setBackground(shape(primary?ACCENT:CARD,14));b.setMinHeight(dp(48));b.setPadding(dp(12),dp(8),dp(12),dp(8));return b;}
    private void gap(LinearLayout l,int n){View v=new View(this);l.addView(v,new LinearLayout.LayoutParams(1,dp(n)));}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private EditText input(String hint,String value,boolean numeric){EditText e=new EditText(this);e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setTextSize(17);e.setSingleLine(true);e.setHint(hint);e.setInputType(numeric?android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL:android.text.InputType.TYPE_CLASS_TEXT);e.setFilters(new InputFilter[]{new InputFilter.LengthFilter(numeric?16:40)});e.setText(value);e.setPadding(dp(10),dp(10),dp(10),dp(10));return e;}
    private interface Change {void set(String s);}
    private void watch(EditText e,Change c){e.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){c.set(s.toString());invalidateResults();save(false);}public void afterTextChanged(Editable x){}});}
    private JSONObject state() throws JSONException {JSONArray a=new JSONArray();for(House h:houses)a.put(h.json());return new JSONObject().put("rate",rate).put("fee",fee).put("configured",configured).put("month",month).put("houses",a).put("history",history);}
    private boolean save(boolean synchronous){if(!ready)return false;try{SharedPreferences.Editor e=prefs.edit().putString("state",state().toString());if(synchronous){boolean ok=e.commit();if(!ok)toast("저장 공간을 확인해 주세요. 기록을 저장하지 못했습니다.");return ok;}e.apply();return true;}catch(JSONException e){toast("저장 중 오류가 발생했습니다.");return false;}}
    @Override protected void onPause(){super.onPause();save(true);}
    private void render(){
        root=column();root.setBackgroundColor(BG);
        root.setPadding(dp(20),dp(8),dp(20),dp(12));
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(20),dp(8)+insets.getSystemWindowInsetTop(),dp(20),dp(12)+insets.getSystemWindowInsetBottom());return insets;});
        setContentView(root);root.requestApplyInsets();
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);
        Button hist=button("◷ 기록",false);hist.setContentDescription("계산 기록 사이드바 열기");hist.setOnClickListener(v->showHistory());bar.addView(hist);
        TextView app=title("수도계산기",21);app.setGravity(Gravity.CENTER);bar.addView(app,new LinearLayout.LayoutParams(0,dp(56),1));
        Button settings=button("요금 설정",false);settings.setOnClickListener(v->settings(false));bar.addView(settings);root.addView(bar);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout content=column();scroll.addView(content);
        gap(content,22);content.addView(label("MONTHLY WATER BILL",11,ACCENT));content.addView(title("이번 달 수도 요금",28));
        monthLabel=label(month+"  ▾",18,TEXT);monthLabel.setContentDescription("계산 월 선택");monthLabel.setPadding(0,dp(12),0,dp(12));monthLabel.setOnClickListener(v->chooseMonth());content.addView(monthLabel);
        rateLabel=label("수도세 "+rate+" 원 / 사용량 1단위  ·  관리비 "+fee+" 원",13,MUTED);content.addView(rateLabel);gap(content,14);
        houseList=column();content.addView(houseList);for(House h:houses)addCard(h);
        Button add=button("+ 집 추가",false);add.setOnClickListener(v->{House h=new House();h.name="집 "+(houses.size()+1);houses.add(h);addCard(h);invalidateResults();save(false);});content.addView(add,new LinearLayout.LayoutParams(-1,dp(52)));
        gap(content,16);content.addView(label("(이번달 − 지난달) × 수도세 + 관리비\n요금 설정은 모든 집에 공통 적용됩니다.",12,MUTED));gap(content,12);
        summary=title("계량기 수치를 입력해 주세요",18);root.addView(summary);
        Button calculate=button("계산하고 기록 저장",true);calculate.setOnClickListener(v->calculate());root.addView(calculate,new LinearLayout.LayoutParams(-1,dp(56)));
    }
    private void addCard(House h){
        LinearLayout card=column();card.setPadding(dp(16),dp(12),dp(16),dp(16));card.setBackground(shape(CARD,20));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.bottomMargin=dp(12);houseList.addView(card,cp);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);EditText name=input("집 이름",h.name,false);name.setContentDescription("집 이름");header.addView(name,new LinearLayout.LayoutParams(0,-2,1));watch(name,s->h.name=s);
        Button del=button("삭제",false);del.setTextColor(0xffffa7a7);del.setContentDescription(h.name+" 삭제");del.setOnClickListener(v->{if(houses.size()==1){toast("계산할 집은 최소 한 개가 필요합니다.");return;}new AlertDialog.Builder(this).setTitle("집을 삭제할까요?").setMessage(h.name+"의 작성 중인 입력이 삭제됩니다. 과거 계산 기록은 보관됩니다.").setNegativeButton("취소",null).setPositiveButton("삭제",(d,w)->{houses.remove(h);houseList.removeView(card);invalidateResults();save(false);}).show();});header.addView(del);card.addView(header);
        LinearLayout row=new LinearLayout(this);LinearLayout left=column(),right=column();left.addView(label("지난달 계량기 수치",12,MUTED));right.addView(label("이번달 계량기 수치",12,ACCENT));EditText prev=input("예: 120",h.previous,true),curr=input("예: 135",h.current,true);prev.setContentDescription(h.name+" 지난달 계량기 수치");curr.setContentDescription(h.name+" 이번달 계량기 수치");left.addView(prev);right.addView(curr);watch(prev,s->h.previous=s);watch(curr,s->h.current=s);row.addView(left,new LinearLayout.LayoutParams(0,-2,1));row.addView(right,new LinearLayout.LayoutParams(0,-2,1));card.addView(row);h.result=label("사용량과 요금은 계산 후 표시됩니다",14,MUTED);card.addView(h.result);
    }
    private void invalidateResults(){if(summary!=null)summary.setText("입력 변경 · 다시 계산해 주세요");for(House h:houses)if(h.result!=null)h.result.setText("사용량과 요금은 계산 후 표시됩니다");}
    private void settings(boolean first){
        LinearLayout box=column();box.setPadding(dp(24),dp(12),dp(24),dp(8));box.addView(label("사용량 1단위당 수도세 (원)",14,MUTED));EditText r=input("예: 1000",rate,true);box.addView(r);box.addView(label("집 한 곳당 월 관리비 (원)",14,MUTED));EditText f=input("예: 5000 (없으면 0)",fee,true);box.addView(f);
        AlertDialog.Builder builder=new AlertDialog.Builder(this).setTitle(first?"처음 한 번, 요금을 설정해 주세요":"요금 설정").setView(box).setPositiveButton("저장",null).setCancelable(!first);if(!first)builder.setNegativeButton("취소",null);
        AlertDialog dialog=builder.create();dialog.setOnShowListener(d->dialog.getButton(-1).setOnClickListener(v->{try{Billing.number(r.getText().toString());Billing.number(f.getText().toString());}catch(IllegalArgumentException e){toast(e.getMessage());return;}rate=r.getText().toString().trim();fee=f.getText().toString().trim();configured=true;if(save(true)){dialog.dismiss();rateLabel.setText("수도세 "+rate+" 원 / 사용량 1단위  ·  관리비 "+fee+" 원");invalidateResults();}}));dialog.show();
    }
    private void chooseMonth(){LinearLayout box=new LinearLayout(this);box.setPadding(dp(24),dp(12),dp(24),dp(12));NumberPicker y=new NumberPicker(this),m=new NumberPicker(this);y.setMinValue(2000);y.setMaxValue(2100);YearMonth ym=YearMonth.parse(month);y.setValue(ym.getYear());m.setMinValue(1);m.setMaxValue(12);m.setValue(ym.getMonthValue());box.addView(y,new LinearLayout.LayoutParams(0,-2,1));box.addView(m,new LinearLayout.LayoutParams(0,-2,1));new AlertDialog.Builder(this).setTitle("계산할 연도 · 월").setView(box).setNegativeButton("취소",null).setPositiveButton("선택",(d,w)->{y.clearFocus();m.clearFocus();month=YearMonth.of(y.getValue(),m.getValue()).toString();monthLabel.setText(month+"  ▾");invalidateResults();save(false);}).show();}
    private void calculate(){
        if(!configured){settings(true);return;}
        try{for(House h:houses){if(h.name.trim().isEmpty())throw new IllegalArgumentException("집 이름을 입력해 주세요.");try{Billing.total(h.previous,h.current,rate,fee);}catch(IllegalArgumentException e){throw new IllegalArgumentException(h.name+": "+e.getMessage());}}}catch(IllegalArgumentException e){toast(e.getMessage());return;}
        for(int i=0;i<history.length();i++)if(month.equals(history.optJSONObject(i).optString("month"))){new AlertDialog.Builder(this).setTitle("이미 계산한 달입니다").setMessage("기존 내역을 보관하고 새 계산 기록을 추가할까요?").setNegativeButton("취소",null).setPositiveButton("추가 저장",(d,w)->record()).show();return;}
        record();
    }
    private void record(){
        try{JSONArray entries=new JSONArray();BigDecimal sum=BigDecimal.ZERO;for(House h:houses){BigDecimal total=Billing.total(h.previous,h.current,rate,fee);sum=sum.add(total);entries.put(h.json().put("total",total.toPlainString()).put("usage",Billing.usage(h.previous,h.current).toPlainString()));}
            JSONObject entry=new JSONObject().put("id",UUID.randomUUID().toString()).put("month",month).put("createdAt",System.currentTimeMillis()).put("rate",rate).put("fee",fee).put("houses",entries).put("total",sum.toPlainString());
            JSONArray before=history;history=new JSONArray();history.put(entry);for(int i=0;i<before.length();i++)history.put(before.get(i));if(!save(true)){history=before;return;}
            for(House h:houses){h.result.setText("사용량 "+Billing.usage(h.previous,h.current).stripTrailingZeros().toPlainString()+"  ·  "+money.format(Billing.total(h.previous,h.current,rate,fee))+" 원");h.result.setTextColor(ACCENT);}summary.setText(houses.size()+"개 집 합계  "+money.format(sum)+" 원");toast("계산 기록을 저장했습니다.");
            View focused=getCurrentFocus();if(focused!=null)((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focused.getWindowToken(),0);
        }catch(JSONException|IllegalArgumentException e){toast("계산 기록을 만들지 못했습니다: "+e.getMessage());}
    }
    private void showHistory(){
        Dialog drawer=new Dialog(this);LinearLayout panel=column();panel.setPadding(dp(20),dp(20),dp(20),dp(20));panel.setBackgroundColor(BG);panel.addView(label("내역 누를 시 지난 달 계량기 값이 자동으로 채워집니다",11,MUTED));
        LinearLayout heading=new LinearLayout(this);heading.setGravity(Gravity.CENTER_VERTICAL);heading.addView(title("계산 기록",25),new LinearLayout.LayoutParams(0,-2,1));Button close=button("닫기",false);close.setOnClickListener(v->drawer.dismiss());heading.addView(close);panel.addView(heading);gap(panel,14);
        ScrollView scroll=new ScrollView(this);LinearLayout list=column();scroll.addView(list);panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));if(history.length()==0)list.addView(label("아직 계산 기록이 없어요.\n첫 계산을 저장하면 여기에 표시됩니다.",15,MUTED));
        for(int i=0;i<history.length();i++){JSONObject record=history.optJSONObject(i);if(record==null)continue;LinearLayout item=column();item.setPadding(dp(14),dp(12),dp(14),dp(12));item.setBackground(shape(CARD,16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(12);list.addView(item,lp);item.addView(title(record.optString("month"),18));item.addView(label(android.text.format.DateFormat.format("yyyy.MM.dd HH:mm",record.optLong("createdAt")).toString(),11,MUTED));
            JSONArray a=record.optJSONArray("houses");if(a!=null)for(int j=0;j<a.length();j++){JSONObject h=a.optJSONObject(j);if(h==null)continue;item.addView(label(h.optString("name")+"  ·  "+h.optString("previous")+" → "+h.optString("current")+"\n("+h.optString("current")+" − "+h.optString("previous")+") × "+record.optString("rate")+" + "+record.optString("fee")+"\n= "+money.format(new BigDecimal(h.optString("total","0")))+" 원",14,TEXT));}
            TextView total=title("합계 "+money.format(new BigDecimal(record.optString("total","0")))+" 원",21);total.setTextColor(ACCENT);item.addView(total);item.setContentDescription(record.optString("month")+" 계산 기록, 눌러서 지난달 수치 불러오기");item.setFocusable(true);item.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("지난달 수치를 불러올까요?").setMessage("해당 기록에 있는 집의 지난달 값을 채우고 이번달 입력을 비웁니다. 삭제한 집은 복원하며, 현재 요금 설정은 유지합니다.").setNegativeButton("취소",null).setPositiveButton("불러오기",(d,w)->{applyHistory(record);drawer.dismiss();}).show());
        }
        drawer.setContentView(panel);Window window=drawer.getWindow();if(window!=null){window.setBackgroundDrawableResource(android.R.color.transparent);window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams attrs=window.getAttributes();attrs.dimAmount=0.6f;window.setAttributes(attrs);window.setGravity(Gravity.LEFT|Gravity.TOP);window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels-dp(24),dp(380)),WindowManager.LayoutParams.MATCH_PARENT);}drawer.show();if(window!=null)window.setLayout(Math.min(getResources().getDisplayMetrics().widthPixels-dp(24),dp(380)),WindowManager.LayoutParams.MATCH_PARENT);
    }
    private void applyHistory(JSONObject record){try{JSONArray a=record.getJSONArray("houses");for(int i=0;i<a.length();i++){JSONObject saved=a.getJSONObject(i);House target=null;for(House h:houses)if(h.id.equals(saved.getString("id"))){target=h;break;}if(target==null){target=House.read(saved);houses.add(target);}target.previous=saved.getString("current");target.current="";}month=YearMonth.parse(record.getString("month")).plusMonths(1).toString();save(true);render();toast("지난달 수치를 채웠습니다. 이번달 수치를 입력해 주세요.");}catch(JSONException e){toast("기록을 불러오지 못했습니다.");}}
}
