import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CompletableFutureTest {
    @Test
    public void test01() throws ExecutionException, InterruptedException {
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

        CompletableFuture<String> f2 = CompletableFuture.completedFuture("hello");
        String s1 = f2.get();
        String s2 = f2.get();
        System.out.println(s1);
        System.out.println(s2);
    }
}
