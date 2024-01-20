import com.app.domain.Post;
import com.app.domain.PostReport;
import com.app.rest.PostManagementRestApi;
import com.app.services.PostDatabaseService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.fail;

public class PostDatabaseServiceTest {

    private ApplicationContext context;
    private PostDatabaseService postDatabaseService;

    @Test
    public void createOrUpdatePostAndFetchPostReportTest1() {

        context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
        postDatabaseService = context.getBean(PostDatabaseService.class);
        Post post = new Post("DUMMMY_ID", "DUMMY_MESSAGE");
        postDatabaseService.createOrUpdatePost(post);

        PostReport postReport = postDatabaseService.getPostReport("DUMMMY_ID");
        Assert.isTrue(post.getId().equals(postReport.getId()), "ids don't match");
        Assert.isTrue( Math.abs(postReport.getAverageWordLength()-13.0) <= 0.000001);
        Assert.isTrue( postReport.getTotalNumberOfWords() == 1);
    }

    @Test
    public void createOrUpdatePostAndFetchPostReportTest2() {

        context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
        postDatabaseService = context.getBean(PostDatabaseService.class);
        Post post = new Post("id_1", "abcd ab abcdef");
        postDatabaseService.createOrUpdatePost(post);

        PostReport postReport = postDatabaseService.getPostReport("id_1");
        Assert.isTrue(post.getId().equals(postReport.getId()), "ids don't match");
        Assert.isTrue( Math.abs(postReport.getAverageWordLength()-4.0) <= 0.000001);
        Assert.isTrue( postReport.getTotalNumberOfWords() == 3);
    }

    @Test
    public void toJsonTest() {
        try {
            context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
            PostManagementRestApi postManagementRestApi = context.getBean(PostManagementRestApi.class);
            Method toJsonMethod = PostManagementRestApi.class.getDeclaredMethod("toJson", Object.class);
            toJsonMethod.setAccessible(true);

            postDatabaseService = context.getBean(PostDatabaseService.class);
            Post post = new Post("DUMMMY_ID", "DUMMY_MESSAGE");
            postDatabaseService.createOrUpdatePost(post);

            PostReport postReport = postDatabaseService.getPostReport("DUMMMY_ID");
            String jsonString = (String) toJsonMethod.invoke(postManagementRestApi, postReport);
            String actualString = "{\n" +
                    "  \"id\" : \"DUMMMY_ID\",\n" +
                    "  \"totalNumberOfWords\" : 1,\n" +
                    "  \"averageWordLength\" : 13.0\n" +
                    "}";
            Assert.isTrue(actualString.equals(jsonString));
        } catch (Exception e) {
            fail("failed with exception.");
        }
    }
}
