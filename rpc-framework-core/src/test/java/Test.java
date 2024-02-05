public class Test {
    public static void main(String[] args) throws InterruptedException {
//        System.out.println("主线程执行");
//        new Thread(()->{
//            System.out.println("子线程执行");
//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }).start();
//        Thread.sleep(3000);
//        Thread.interrupted();

        int a = 1 / -1;
        System.out.println(a);
    }
}
