import com.app.rest.PostManagementRestApi;
import com.app.services.PostDatabaseService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SpringTest {

    @Test
    public void springBootStrapTest1() {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
        PostDatabaseService postDatabaseService = context.getBean(PostDatabaseService.class);
        PostManagementRestApi postManagementRestApi = context.getBean(PostManagementRestApi.class);
        assertNotNull(postDatabaseService);
        assertNotNull(postManagementRestApi);
    }
}
