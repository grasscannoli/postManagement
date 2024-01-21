import com.app.services.PostDatabaseService;
import com.app.services.encryption.DataSecurityService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Method;
import com.app.services.encryption.DataSecurityService.EncryptionMetadata;
import org.springframework.util.Assert;

import static org.junit.jupiter.api.Assertions.fail;

public class DataSecurityServiceTest {

    private ApplicationContext context;
    private DataSecurityService dataSecurityService;
    @Test
    public void initServiceTest() {
        context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
        dataSecurityService = context.getBean(DataSecurityService.class);

        try {
            Method method = DataSecurityService.class.getDeclaredMethod("getEncryptionMetadata");
            method.setAccessible(true);
            EncryptionMetadata encryptionMetadata = (EncryptionMetadata) method.invoke(dataSecurityService);
            Assert.isTrue(encryptionMetadata.getIv().equals("tpeUWwT2b02nsuIT1D+yyA=="));
            Assert.isTrue(encryptionMetadata.getKey().equals("6f1ed002ab5595859014ebf0951522d9"));
        } catch (Exception e) {
            fail("failed with exception.");
        }
    }

    @Test
    public void cryptTest() {
        context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
        dataSecurityService = context.getBean(DataSecurityService.class);

        try {
            String testText = "DUMMY_TEXT";
            String encryptedText = dataSecurityService.encryptString(testText);
            String decryptedText = dataSecurityService.decryptString(encryptedText);

            Assert.isTrue(testText.equals(decryptedText));
            Assert.isTrue(!decryptedText.equals(encryptedText));
        } catch (Exception e) {
            fail("failed with exception.");
        }
    }


}
