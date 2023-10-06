import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
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

    @Test
    public void test02(){
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("test"));
        try {
            String s = future.get();
            System.out.println(s);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }catch (RuntimeException e){
            System.out.println("catch");
        }
    }

    @Test
    public void test03(){
        try {
            exceptionTest();
        }catch (RuntimeException e ){
            System.out.printf("catch");
            System.out.println(e);
        }
    }

    private void exceptionTest() throws RuntimeException{
        try {
            int a = 1 / 0;
        }catch (Exception e){
            throw new RuntimeException();
        }
    }

    @Test
    public void test04(){
        Map<String,String> map = new HashMap<>();
        map.put("1","1");
        map.computeIfAbsent("2",k->{
            return "3";
        });
        map.forEach((k,v)->{
            System.out.println(k);
            System.out.println(v);
        });
    }
}
