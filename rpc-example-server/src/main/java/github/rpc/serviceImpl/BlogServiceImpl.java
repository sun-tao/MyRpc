package github.rpc.serviceImpl;


import github.rpc.annotation.RpcService;
import github.rpc.common.Blog;
import github.rpc.service.BlogService;

@RpcService(group = "g1",version = "v1")
public class BlogServiceImpl implements BlogService {

    public Blog getBlogByid(int id) {
        Blog blog = new Blog();
        blog.setId(id);
        blog.setTitle("rpcVersion2");
        blog.setUserId("1");
        return blog;
    }
}
