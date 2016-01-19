package it.polimi.diceH2020.SPACE4CloudWS.test.Integration;

import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.internal.mapper.ObjectMapperType;

import it.polimi.diceH2020.SPACE4Cloud.shared.InstanceData;
import it.polimi.diceH2020.SPACE4CloudWS.stateMachine.Events;

@RunWith(SpringJUnit4ClassRunner.class)   // 1
@SpringApplicationConfiguration(classes = it.polimi.diceH2020.SPACE4CloudWS.main.SPACE4CloudWS.class)   // 2
@WebAppConfiguration   // 3
@IntegrationTest("server.port:8080")   // 4
@ActiveProfiles("test")
public class Test1 {

    @Value("${local.server.port}")   // 6
    int port;

    @Before
    public void setUp() {
        RestAssured.port = port;
    }

    @Test
    public void testApplDataFormat() {
        RestAssured.	
        when().
                get("/appldata").
        then().
                statusCode(HttpStatus.SC_OK).
                body(matchesJsonSchemaInClasspath("static/applData.json"));
       
        
    }

    @Test
    public void testPutInputData() {
		int gamma = 2400; // num cores cluster
		List<String> typeVm = Arrays.asList( "T1", "T2" );
		String provider = "Amazon";
		List<Integer> id_job = Arrays.asList( 10, 11 ); // numJobs = 2
		double[] think = { 0.5, 0.10 }; // check
		int[][] cM = { { 16, 16 }, { 32, 32 } };
		int[][] cR = { { 16, 16 }, { 16, 32 } };
		double[] eta = { 0.1, 0.3 };
		int[] hUp = { 10, 10 };
		int[] hLow = { 5, 5 };
		int[] nM = { 495, 65 };
		int[] nR = { 575, 5 };
		double[] mmax = { 36.016, 17.541 }; // maximum time to execute a single map
		double[] rmax = {  4.797, 0.499  };
		double[] mavg = { 17.196,  8.235 };
		double[] ravg = { 0.605, 0.297 };
		double[] d = { 15000, 10000 };
		double[] sH1max = { 0, 0 };
		double[] sHtypmax = { 18.058, 20.141 };
		double[] sHtypavg = { 2.024, 14.721 };
		double[] job_penalty = { 19028.2, 29562.9 };
		double[] r = { 200, 200 };
		InstanceData data = new InstanceData(gamma, typeVm, provider, id_job, think, cM, cR, eta, hUp, hLow, nM, nR, mmax, rmax, mavg,
				ravg, d, sH1max, sHtypmax, sHtypavg, job_penalty, r);
    	
	   	 RestAssured.	
	     when().
	             get("/state").
	     then().
	             statusCode(HttpStatus.SC_OK).
	             assertThat().body(Matchers.is("IDLE"));
		
    	RestAssured.
    	given().
    	       contentType("application/json; charset=UTF-16").
    	       body(data).
    	when().
    	      post("/inputdata").then().statusCode(HttpStatus.SC_OK);
    	
    	
    	
	   	 RestAssured.	
	     when().
	             get("/state").
	     then().
	             statusCode(HttpStatus.SC_OK).
	             assertThat().body(Matchers.is("CHARGED"));
	   	
    	 RestAssured.
    	 given().
	       contentType("application/json; charset=UTF-16").
	       body(Events.MIGRATE, ObjectMapperType.JACKSON_2).
         when().
                 post("/sendevent").
         then().
                 statusCode(HttpStatus.SC_OK)
                 .assertThat().body(Matchers.is("RUNNING"));
	    
   }

}

