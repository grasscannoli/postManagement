<h1>Post Management Application</h1>
<h2>What it does</h2>
<body>

This is a backend web application. This server serves two types of requests:
<ul>
<li>
First one serves the end point <code>/api/v1/posts</code>. It consumes, and the Post object it consumes has the following schema:

```json
{
  "id": "String",
  "message": "String"
}
```

</li>
<li>
Second one serves the end point <code>api/v1/posts/{id}/analysis</code>. 
It produces the PostReport object with the following schema (These are reporting metrics):

```json
{
  "id": "String",
  "totalNumberOfWords": "Integer",
  "averageWordLength": "Double"
}
```

</li>
</ul>

<p>The application is also supposed to showcase the following characteristics of a production grade system</p>
<ul>

<li>
A <code>RateLimitFilter</code> that blocks requests from a specific IP Address if it makes too many requests.
This prevents DOS attacks. There is a timeout for that IP address in case it crosses its threshold. 
Once timeout period elapses, it can resume sending its requests.
</li>

<li>

A **REST Web Application** that serves requests.
This is still outdated against the more recent GraphQL, but serves the purpose.
</li>

<li>

**Asynchronous HTTP responses**: The main REST thread is freed to serve other requests.
The client-server connection is suspended until all process is completed.
The client-server connection is then resumed and the response is returned.
</li>

<li>

**Parallelism**: All database calls are parallelized to the maximum extent in a thread safe manner.
This optimises the speed of serving a request.
</li>

<li>

**Caching**: We use container local caching. Can be extended to distributed caches as well, with the use of redis.
</li>

<li>

**SQL Database**: General relational database that can be indexed and used for quick responses.
</li>

<li>

**Encrypted Data in DB**: The messages in each post are treated to be sensitive information and are
encrypted while storing into the SQL database. While retrieving the information, it is decrypted and fetched.
This uses <code>AES-GCM 256</code>, which is an industry standard.
</li>

<li>

**Logging**: Support for both `stdout` and `postManagement.log` file logging from within the container is present. 
This is over and on top of the Tomcat logging that comes by default.   
</li>
</ul>

</body>
<h2>Setup</h2>
<body>
<p>I am working with <code>macOS Monterry 12.7.2</code>. You'll need the following setup apriori: (to replicate without errors)</p>
<ul>
<li>Java 11</li>
<li>Tomcat Server</li>
<li>MySQL Server</li>
</ul>
</body>


<h2>Tomcat Server</h2>
<h3>Setup</h3>
<body>
<p>

I have installed <code>apache-tomcat-9.0.85</code>.  
You can follow a similar installation <a href="https://www.youtube.com/watch?v=TboEjpKnzBo"> procedure </a>.  
All deployments can be managed from chrome UI this url, following auth: <code>http://localhost:8080/manager/</code>.  
Once you generate a <code>.war file</code> (via gradle, shown later), you can deploy using this page:</p>
<img width="1715" alt="image" src="https://github.com/grasscannoli/postManagement/assets/51586512/86ced5ab-83f7-4b3f-bb15-c50df4fa49d3">

<p>When the web application is successfully deployed and running, it should have started in the applications like so: (last line, postManagement service)</p>
<img width="1713" alt="image" src="https://github.com/grasscannoli/postManagement/assets/51586512/c24b17f4-c761-43ce-a79a-e857637958ff">

<p>

Ensure <code>localhost:8080</code> is the port that is used for tomcat. 
One can configure other ports, will have to go into the <code>cd apache-tomcat-9.0.85/conf</code> directory and meddle with the config. 
(Google in case you wanna do this)
</p>
</body>

<h3>Logs in Tomcat</h3>
<body>
<p>

To find the log files, <code>cd apache-tomcat-9.0.85/logs</code> (you'll have to find where you have installed tomcat
folder).  
Then you'll have to check the following files to find relevant logs:
</p>
<ul>
<li>

<code>catalina.out</code>: This file has the server level logs along with <code>stdout</code> output.
</li>

<li>

<code>localhost.\<date>.log</code>: This file has local container logs.
</li>

<li>

<code>localhost_access_log.\<date>.txt</code>: This file has access logs, which tracks all http requests to the server.
</li>
</ul>
</body>

<h3>Operation</h3>
<body>
<p>
To start and shutdown the tomcat server (not the web-app, the whole server) via cli,
go to <code>cd apache-tomcat-9.0.85/bin</code>. 
Here you can run <code>./startup.sh</code> and <code>./shutdown.sh</code> respectively, to achieve the above. 
</p>
</body>

<h2>MySQL Server</h2>
<h3>Setup</h3>
<body>
<p>

You can follow this [link](https://www.geeksforgeeks.org/how-to-install-mysql-on-macos/) to setup the mysql server. Ensure the <code>localhost:3306</code> port is what is used for the mysql server.
</p>
</body>

<h3>Use</h3>
<body>
<p>

You'll have to setup a database after logging to it via cli using <code>./mysql -u root -p</code>.  
Then you can create a database with the command <code>create database postManagement;</code>  
You must also create a user with credentials to access this data source from code. Use <code>grant all on db_example.* to 'springuser'@'%';</code> for this.  

Now repeat the same for another database <code>test</code>. This is for certain unit tests.
</p>

<p>

With this much setup, we are good to go - one can run the application and it should work.  
To verify if the database is storing as expected, one can run common queries via cli like
<code>select * from postManagement.post;</code>
<code>select * from postManagement.postReport;</code>

Sample results of these are as follows:
</p>
<img width="869" alt="image" src="https://github.com/grasscannoli/postManagement/assets/51586512/3587783e-7ce1-4b49-8af4-887118b8bf21">

</body>

<h2>Java Application</h2>
<h3>WAR file using Gradle</h3>
<body>
<p>

1. Clone the git repo locally, and via terminal, cd into the application, the <code>postManagement</code> directory.  
2. Here you'll have to do <code>./gradlew war</code> to make a war file to deploy into Tomcat.  
3. Once you run it you'll find the war file in <code>postManagement/build/libs</code> folder.   
4. Deploy it on Tomcat, via chrome, as shown earlier.
5. In case you make changes to the code and want to redeploy, ensure you delete the old war, because running the <code>./gradlew war</code> command does not replace the old one. 
</p>
</body>

<h3>Application Configuration</h3>
<body>
<p>

1. Encryption-decryption require config, you'll need to set a Base64 encoded IV and a 256 bit encryption key, both that can be generated like this:

```java
    public static String generateIvBase64Encoded() {
        byte[] ivBytes = new byte[16];
        new SecureRandom().nextBytes(ivBytes);
        return Base64.getEncoder().encodeToString(ivBytes);
    }

    public static void givenUsingApache_whenGeneratingRandomAlphanumericString_thenCorrect(int length) {
        String generatedString = RandomStringUtils.randomAlphanumeric(length);
        System.out.println(generatedString);
    }
```
2. In the <code>RateLimitFilter</code>, one can change the `MAX_REQUESTS` and `TIME_WINDOW` as per requirements. 
3. Additional config for where the sl4j logs need to be output can be done in the `resources/logback.xml` file. Can also be made asynchronous, refer Google.
</p>
</body>

<h3>System Design Flow</h3>
<body>

![image](https://github.com/grasscannoli/postManagement/assets/51586512/9cdb41ca-ebbe-433e-957b-1a57e9cdf4f8)

**Flow1**: We utilise the rate-limiter, async responses, separate workers and the SQL database in this flow.
Here the cache must be invalidated/refreshed due to dirtying of data.  

![image](https://github.com/grasscannoli/postManagement/assets/51586512/7cae5b68-5c31-4ed3-87b6-ba520c553514)

**Flow2**: We utilise the rate-limiter, async responses and the cache in this flow. 
The cache loads data from the database in case of a miss.

<img width="1352" alt="image" src="https://github.com/grasscannoli/postManagement/assets/51586512/208177c3-117a-436b-ace5-7d91333cdb53">

**Result 1**: We get a 200 ok, when a curl to createOrUpdate a Post like this is sent:

```
curl --location 'http://localhost:8080/postManagement-v1/rest/api/v1/posts' \
--header 'Content-Type: application/json' \
--data '{
    "id" : "post_1235",
    "message" : "iPhone 13 pro max is awesome!"
}'
```
<img width="1336" alt="image" src="https://github.com/grasscannoli/postManagement/assets/51586512/c18d7928-5002-472d-b6ce-f6d2888617c4">

**Result 2**: We get a 200 ok and the correct metrics for the post, when a curl to analyzePost like this is sent:

```
curl --location 'http://localhost:8080/postManagement-v1/rest/api/v1/posts/post_1235/analysis' \
--data ''
```

<img width="904" alt="image" src="https://github.com/grasscannoli/postManagement/assets/51586512/b0394824-dc05-4ba8-9f69-8c04724e1e09">

**Result 3**: We get a 429 error when we send too many requests like this^.

<img width="1351" alt="image" src="https://github.com/grasscannoli/postManagement/assets/51586512/bbb963d1-2531-4f72-be90-d2c5fbc884c5">

**Result 4**: We get a 400 error when we send an empty message, which is assumed to be illformed^. We do basic validation before creating a post.

</body>

<h3>Extensions</h3>
<body>
<p>

The ideal next steps for this mini-application are:
1. Making the functionalities slightly more complex so as to break into more microservices
2. To break intomicroservices, use GRPC calls and setup multiple Tomcat servers.
3. Use better NoSQL DBs like Mongo.
4. Move to GraphQL instead of REST as the API for the web application.
5. Move microservices to Kubernetes, with the use of Helm Charts.
6. Expose the server publicly via ingress and ngix.
</p>

</body>
