/**
 * alternate orchestrator for comparison against the puddle orchestrator
 * 
 * this orchestrator only assigns tasks to the cloud
 */

package edu.auburn.pFogSim.orchestrator;

import java.util.LinkedList;

import org.cloudbus.cloudsim.Datacenter;
import edu.auburn.pFogSim.netsim.ESBModel;
import edu.auburn.pFogSim.netsim.NodeSim;
import edu.auburn.pFogSim.util.MobileDevice;
import edu.boun.edgecloudsim.core.SimManager;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeHost;


/**
 * 
 * @author szs0117
 *
 */
public class FixedNodeOrchestrator extends EdgeOrchestrator{
	
	private static final int MESSAGES_PER_HOST = 2;
	private static final int BYTES_PER_KB = 1024;
	private static final int BITS_PER_BYTE = 8;
	private static String node = "Datacenter_1";
	EdgeHost cHost;
	
	
	/**
	 * 
	 * @param _policy
	 * @param _simScenario
	 */
	public FixedNodeOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}
	
	
	/**
	 * 
	 */
	@Override
	public void initialize() {
		try {
			cHost = (SimManager.getInstance().getLocalServerManager().findHostById(1));
			
			this.avgNumProspectiveHosts = 1;
			this.avgNumMessages = this.avgNumProspectiveHosts * MESSAGES_PER_HOST; // For each service request (i.e. per device), each host receives resource availability request & sends response.
			
			}
			catch (NullPointerException e) {
				return;
			}
	}

	
	/**
	 * set cloud
	 * modified by Qian
	 * @param Datacenter _cloud
	 */
	@Override
	public void setCloud(Datacenter _cloud) {
		if (_cloud.getName().equals(node)) {
			cloud = _cloud;
		}
	}

	
	/* (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#assignHost(edu.auburn.pFogSim.util.MobileDevice)
	 */
	@Override
	public void assignHost(MobileDevice mobile) {
		EdgeHost cloudHost = (EdgeHost) cloud.getHostList().get(0);
		cloudHost = cHost; // Shaik added
		if (goodHost(cloudHost, mobile)) {
			LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(cloudHost, mobile);
			mobile.setPath(path);
			mobile.setHost(cloudHost);
			mobile.makeReservation();
			
			//-- Shaik - Following code component is to track the expected total cost.
			///*
			double cost=0;
			NodeSim src = ((ESBModel)SimManager.getInstance().getNetworkModel()).getNetworkTopology().findNode(mobile.getLocation(), false);
			NodeSim des = ((ESBModel)(SimManager.getInstance().getNetworkModel())).getNetworkTopology().findNode(cloudHost.getLocation(), false);

			ESBModel networkModel = (ESBModel)(SimManager.getInstance().getNetworkModel());
			LinkedList<NodeSim> tpath = new LinkedList<>();
			tpath = networkModel.findPath(src, des);
			
			if (tpath == null || tpath.size() == 0) {
				EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(mobile.getLocation().getXPos(), mobile.getLocation().getYPos(), mobile.getLocation().getAltitude());
				
				//double bwCost = mobile.getBWRequirement() * k.getCostPerBW(); 
				double bwCost = (mobile.getBWRequirement()*BITS_PER_BYTE / BYTES_PER_KB) * k.getCostPerBW(); //mobile.getBWRequirement() in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified

				//double exCost = (double)mobile.getTaskLengthRequirement() / k.getTotalMips() * k.getCostPerSec();
				double exCost = mobile.getTaskLengthRequirement() / (k.getPeList().get(0).getMips()) * k.getCostPerSec(); // Shaik modified - May 07, 2019.

				cost = cost + bwCost;
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + des.getLevel() + "\tNode:\t" + des.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
				cost = cost + exCost;
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPU Time:\t" + ((double)mobile.getTaskLengthRequirement() / k.getTotalMips()) + "\tMipsCostPerSec:\t" + k.getCostPerSec());
			}
			else {
				//SimLogger.getInstance().getCentralizeLogPrinter().println("**********Path From " + src.getWlanId() + " To " + des.getWlanId() + "**********");
				for (NodeSim nodeSim: tpath) {
					EdgeHost k = SimManager.getInstance().getLocalServerManager().findHostByLoc(nodeSim.getLocation().getXPos(), nodeSim.getLocation().getYPos(), nodeSim.getLocation().getAltitude());
					//double bwCost = mobile.getBWRequirement() * k.getCostPerBW();
					double bwCost = (mobile.getBWRequirement()*BITS_PER_BYTE / BYTES_PER_KB) * k.getCostPerBW(); //mobile.getBWRequirement() in KB * 8b/B ==>Kb / 1024 = Mb; k.getCostPerBW() in $/Mb -- Shaik modified

					cost = cost + bwCost;
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Level:\t" + node.getLevel() + "\tNode:\t" + node.getWlanId() + "\tBWCost:\t" + bwCost + "\tTotalBWCost:\t" + cost);
					//SimLogger.getInstance().getCentralizeLogPrinter().println("Total data:\t" + mobile.getBWRequirement() + "\tBWCostPerSec:\t" + k.getCostPerBW());
				}				
				EdgeHost desHost = SimManager.getInstance().getLocalServerManager().findHostByLoc(des.getLocation().getXPos(), des.getLocation().getYPos(), des.getLocation().getAltitude());
				//double exCost = desHost.getCostPerSec() * ((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips());
				double exCost = mobile.getTaskLengthRequirement() / (desHost.getPeList().get(0).getMips()) * desHost.getCostPerSec(); // Shaik modified - May 07, 2019.
				cost = cost + exCost;
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Destination:\t"+ des.getWlanId() + "\tExecuteCost:\t" + exCost + "\tTotalCost:\t" + cost);
				//SimLogger.getInstance().getCentralizeLogPrinter().println("Service CPU Time:\t" + ((double)mobile.getTaskLengthRequirement() / desHost.getTotalMips()) + "\tMipsCostPerSec:\t" + desHost.getCostPerSec());
			}
			//*/						
		}
		else
			System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
	}

	
	/**
	 * @return the node
	 */
	public static String getNode() {
		return node;
	}

	
	/**
	 * @param node the node to set
	 */
	public static void setNode(String node) {
		FixedNodeOrchestrator.node = node;
	}


}
