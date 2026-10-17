package kr.water.calculator;
import java.math.BigDecimal;
public class BillingTest {
    static int checks=0;
    static void total(String p,String c,String r,String f,String expected){checks++;if(Billing.total(p,c,r,f).compareTo(new BigDecimal(expected))!=0)throw new AssertionError(expected);}
    static void invalid(Runnable r){checks++;try{r.run();}catch(IllegalArgumentException e){return;}throw new AssertionError("Invalid input accepted");}
    public static void main(String[] args){
        total("100","125","1000","5000","30000");
        total("100","100","1000","5000","5000");
        total("1.1","1.2","0.1","0","0");
        total("10","10.5","1","0","1");
        total("0","0","0","0","0");
        total("0","999999999999","999999999999","0","999999999998000000000001");
        invalid(()->Billing.total("120","119","1","0"));
        for(String bad:new String[]{"","-1","1e5","NaN","1,000",".5","1.0001","1000000000000"})invalid(()->Billing.number(bad));
        invalid(()->Billing.total("0","1","-1","0"));invalid(()->Billing.total("0","1","1",""));
        System.out.println("PASS: "+checks+" billing checks");
    }
}
