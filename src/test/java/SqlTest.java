import com.app.database.JdbcTemplateFactory;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlTest {

    @Test
    public void templateTest1() {
        ApplicationContext context = new ClassPathXmlApplicationContext("spring/applicationContext.xml");
        JdbcTemplateFactory jdbcTemplateFactory = context.getBean(JdbcTemplateFactory.class);
        JdbcTemplate jdbcTemplate = jdbcTemplateFactory.createTemplate("test");
        jdbcTemplate.execute("create table if not exists TestObject (id varchar(100), numBro int)");

        TestObject oldTestObject = new TestObject();
        oldTestObject.setId("id" + System.currentTimeMillis());
        oldTestObject.setNumBro(17);
        String sqlInsert = "insert into TestObject (id, numBro) values (?, ?)";
        jdbcTemplate.update(sqlInsert, oldTestObject.getId(), oldTestObject.getNumBro());

        String sqlQuery = "select * from TestObject where id = ?";
        TestObject newTestObject = jdbcTemplate.queryForObject(sqlQuery, new Object[]{oldTestObject.getId()}, new TestObjectMapper());
        Assert.isTrue(newTestObject.getId().equals(oldTestObject.getId()), "ids did not match");

        jdbcTemplate.execute("drop table TestObject");
    }

    public static class TestObject {
        private String id;
        private int numBro;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getNumBro() {
            return numBro;
        }

        public void setNumBro(int numBro) {
            this.numBro = numBro;
        }
    }

    public static class TestObjectMapper implements RowMapper<TestObject> {
        public TestObject mapRow(ResultSet rs, int rowNum) throws SQLException {
            TestObject testObject = new TestObject();
            testObject.setId(rs.getString("id"));
            testObject.setNumBro(rs.getInt("numBro"));
            return testObject;
        }
    }
}
