import github.rpc.BlogController;
import github.rpc.UserController;
import github.rpc.config.SpringConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MulitThreadingTest {
    public static void main(String[] args) {
        ApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(SpringConfig.class);
//        UserController userController = (UserController) annotationConfigApplicationContext.getBean("userController");
//        userController.start();

        // ---------测试时停用了心跳包 ---------
        final BlogController blogController = (BlogController) annotationConfigApplicationContext.getBean("blogController");
        for (int i = 0 ; i < 80 ; i++){
            final int num = i;
            new Thread(new Runnable() {
                public void run() {
                    blogController.start();
                    System.out.println(String.format("线程%d执行完毕",num));
                }
            }).start();
        }
    }
}
