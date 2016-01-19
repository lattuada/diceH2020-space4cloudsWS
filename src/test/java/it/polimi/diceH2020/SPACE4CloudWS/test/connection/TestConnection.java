package it.polimi.diceH2020.SPACE4CloudWS.test.connection;

import java.util.List;

import org.aspectj.apache.bcel.util.ClassPath;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import it.polimi.diceH2020.SPACE4CloudWS.solvers.MINLPSolver;
import it.polimi.diceH2020.SPACE4CloudWS.solvers.SPNSolver;

@RunWith(SpringJUnit4ClassRunner.class)  
@SpringApplicationConfiguration(classes = it.polimi.diceH2020.SPACE4CloudWS.main.SPACE4CloudWS.class)   // 2
@ActiveProfiles("test")
public class TestConnection {
		@Autowired
		MINLPSolver milpSolver;	
		
		@Autowired
		SPNSolver spnSolver;

	    @Test
	    public void testApplDataFormat() throws Exception {

				List<String> res = milpSolver.clear();
				Assert.assertTrue(res.contains("exit-status: 0"));
				res.clear();
				res = milpSolver.getConnector().exec("ls");
				Assert.assertTrue(res.size() == 1 && res.contains("exit-status: 0"));
				res = milpSolver.getConnector().exec("mkdir AMPL");
				Assert.assertTrue(res.size() == 1 && res.contains("exit-status: 0"));
				res = milpSolver.getConnector().exec("cd AMPL");
				Assert.assertTrue(res.size() == 1 && res.contains("exit-status: 0"));
				System.out.println(milpSolver.pwd());
				res = milpSolver.getConnector().exec("cd AMPL && mkdir problems utils solve");
				Assert.assertTrue(res.size() == 1 && res.contains("exit-status: 0"));
				System.out.println(ClassPath.getClassPath());
			//	milpSolver.getConnector().sendFile("src/main/resources/static/initFiles/MILPSolver/problems/centralized.run", "/home/tueguem/AMPL/centralized.run");
				

	    }
	    
	    @Test
	    public void testSPN() throws Exception{
				List<String> res = spnSolver.pwd();
				Assert.assertTrue(res.size() == 2 && res.get(0).contains("/home/user") && res.contains("exit-status: 0"));
				res = spnSolver.getConnector().exec("rm -rf ./Experiments");
				Assert.assertTrue(res.size() == 1 && res.contains("exit-status: 0"));
				res = spnSolver.getConnector().exec("mkdir ./Experiments");
				Assert.assertTrue(res.size() == 1 && res.contains("exit-status: 0"));
				
	    	
	    	
	    	
	    }
	    
}