import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class CompletableFutureTest {
    @Test
    public void test01(){
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "nihao";
        });
        f1.whenComplete((result,t)->{
            System.out.println(result);
            System.out.println("end");
        });
    }
}
