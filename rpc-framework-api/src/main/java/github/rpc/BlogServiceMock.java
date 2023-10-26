package github.rpc;

public class BlogServiceMock implements BlogService{
    @Override
    public Blog getBlogByid(int id) throws Exception {
        return new Blog(666,"1","mock");
    }
}
