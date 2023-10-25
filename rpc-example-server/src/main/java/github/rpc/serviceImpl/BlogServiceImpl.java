package github.rpc.serviceImpl;
import github.rpc.Blog;
import github.rpc.BlogService;
import github.rpc.annotation.RpcService;


@RpcService(group = "g1",version = "v1")
public class BlogServiceImpl implements BlogService {

    public Blog getBlogByid(int id) throws Exception {
        // 模拟超时
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Blog blog = new Blog();
        blog.setId(id);
        blog.setTitle("rpcVersion2");
        blog.setUserId("1");
        return blog;
    }
}
