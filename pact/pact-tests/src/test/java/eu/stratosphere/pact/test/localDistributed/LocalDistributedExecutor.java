package eu.stratosphere.pact.test.localDistributed;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.stratosphere.nephele.client.JobClient;
import eu.stratosphere.nephele.configuration.ConfigConstants;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.configuration.GlobalConfiguration;
import eu.stratosphere.nephele.instance.local.LocalTaskManagerThread;
import eu.stratosphere.nephele.jobgraph.JobGraph;
import eu.stratosphere.nephele.jobmanager.JobManager;
import eu.stratosphere.nephele.jobmanager.JobManager.ExecutionMode;
import eu.stratosphere.pact.client.minicluster.NepheleMiniCluster;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.compiler.DataStatistics;
import eu.stratosphere.pact.compiler.PactCompiler;
import eu.stratosphere.pact.compiler.plan.candidate.OptimizedPlan;
import eu.stratosphere.pact.compiler.plantranslate.NepheleJobGraphGenerator;

public class LocalDistributedExecutor  {
	private static int JOBMANAGER_RPC_PORT = 6498;
	
	public static class JobManagerThread extends Thread {
		JobManager jm;
		
		public JobManagerThread(JobManager jm) {
			this.jm = jm;
		}
		@Override
		public void run() {
			jm.runTaskLoop();
		}
	}
	
	public void run(final Plan plan, final int numTaskMgr) throws Exception {
		Configuration conf = NepheleMiniCluster.getMiniclusterDefaultConfig(JOBMANAGER_RPC_PORT, 6500,
				7501, 7533, null, true);
		GlobalConfiguration.includeConfiguration(conf);
			
		// start job manager
		JobManager jobManager;
		try {
			jobManager = new JobManager(ExecutionMode.CLUSTER);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
			return;
		}
	
		
		JobManagerThread jobManagerThread = new JobManagerThread(jobManager);
		jobManagerThread.setDaemon(true);
		jobManagerThread.start();
		
		// start the taskmanagers
		List<LocalTaskManagerThread> tms = new ArrayList<LocalTaskManagerThread>();
		for(int tm = 0; tm < numTaskMgr; tm++) {
			// The whole thing can only work if we assign different IP addresses to each TaskManager
			Configuration tmConf = new Configuration();
			tmConf.setInteger(ConfigConstants.TASK_MANAGER_IPC_PORT_KEY,
						ConfigConstants.DEFAULT_TASK_MANAGER_IPC_PORT + 100 + tm + numTaskMgr);
			tmConf.setInteger(ConfigConstants.TASK_MANAGER_DATA_PORT_KEY, ConfigConstants.DEFAULT_TASK_MANAGER_DATA_PORT+tm); // taskmanager.data.port
			GlobalConfiguration.includeConfiguration(tmConf);
			LocalTaskManagerThread t = new LocalTaskManagerThread();
			t.start();
			tms.add(t);
		}
		
		final int sleepTime = 100;
		final int maxSleep = 1000 * 2 * numTaskMgr; // we wait 2 seconds PER TaskManager.
		int slept = 0;
		// wait for all taskmanagers to register to the JM
		while(jobManager.getNumberOfTaskTrackers() < numTaskMgr) {
			Thread.sleep(sleepTime);
			if(slept >= maxSleep) {
				throw new RuntimeException("Waited for more than 2 seconds per TaskManager to register at "
						+ "the JobManager.");
			}
		}
		
		
		PactCompiler pc = new PactCompiler(new DataStatistics());
		OptimizedPlan op = pc.compile(plan);
		
		NepheleJobGraphGenerator jgg = new NepheleJobGraphGenerator();
		JobGraph jobGraph = jgg.compileJobGraph(op);
	
		try {
			JobClient jobClient = getJobClient(jobGraph);
			
			jobClient.submitJobAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public JobClient getJobClient(JobGraph jobGraph) throws Exception {
		Configuration configuration = jobGraph.getJobConfiguration();
		configuration.setString(ConfigConstants.JOB_MANAGER_IPC_ADDRESS_KEY, "localhost");
		configuration.setInteger(ConfigConstants.JOB_MANAGER_IPC_PORT_KEY, JOBMANAGER_RPC_PORT);
		return new JobClient(jobGraph, configuration);
	}
}
