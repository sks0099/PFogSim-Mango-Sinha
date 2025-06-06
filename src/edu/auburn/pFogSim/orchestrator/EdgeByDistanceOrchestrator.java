/**
 * Edge By Distance Orchestrator
 * 
 * This orchestrator uses the centralized approach to selecting a VM. 
 * @author Shehenaz Shaik
 * @author Qian Wang
 */
package edu.auburn.pFogSim.orchestrator;

import java.util.ArrayList;
import java.util.LinkedList;
import org.cloudbus.cloudsim.Datacenter;
import edu.auburn.pFogSim.Radix.BinaryHeap;
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
public class EdgeByDistanceOrchestrator extends EdgeOrchestrator {

	private static final int MESSAGES_PER_HOST = 2;
	ArrayList<EdgeHost> hosts;
	
	
	/**
	 * 
	 * @param _policy
	 * @param _simScenario
	 */
	public EdgeByDistanceOrchestrator(String _policy, String _simScenario) {
		super(_policy, _simScenario);
	}
	
	
	/**
	 * get all the hosts in the network into one list
	 */
	@Override
	public void initialize() {
		hosts = new ArrayList<EdgeHost>();
		for (Datacenter node : SimManager.getInstance().getLocalServerManager().getDatacenterList()) {
			if (((EdgeHost) node.getHostList().get(0)).getLevel() == 1) {
				hosts.add(((EdgeHost) node.getHostList().get(0)));
			}
		}
		
		this.avgNumProspectiveHosts = hosts.size();
		this.avgNumMessages = this.avgNumProspectiveHosts * MESSAGES_PER_HOST; // For each service request (i.e. per device), each host receives resource availability request & sends response.

	}
	
	/** 
	 * @ author Qian Wang
	 * @ author Shehenaz Shaik
	 * (non-Javadoc)
	 * @see edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator#assignHost(edu.auburn.pFogSim.util.MobileDevice)
	 */
	@Override
	public void assignHost(MobileDevice mobile) {

		//DistRadix sort = new DistRadix(hosts, mobile.getLocation());//use radix sort based on distance from task
		BinaryHeap sort = new BinaryHeap(hosts.size(), mobile.getLocation(), hosts);
		LinkedList<EdgeHost> nodes = sort.sortNodes();
		//System.out.print("nodes size: " + nodes.size() + "  Device Id: " + mobile.getId() + "  WAP Id: " + mobile.getLocation().getServingWlanId());
		EdgeHost host = nodes.poll();
		/*
		 * for (int i = 0; !goodHost(host, task) && i < 10; i++) {
		 *     host = nodes.poll();
		 * }
		 */
		System.out.print("Prospective host:  ");
		while(!goodHost(host, mobile)) {
			host = nodes.poll();//find the closest node capable of handling the task
			if (host == null) {
				break;
			}
		}
		if (host != null) {
			LinkedList<NodeSim> path = ((ESBModel)SimManager.getInstance().getNetworkModel()).findPath(host, mobile);
			mobile.setPath(path);
			mobile.setHost(host);
			mobile.makeReservation();
			System.out.println("  Assigned host: " + host.getId());
		}
		else
			System.out.println("  Mobile device: "+mobile.getId()+"  WAP: "+mobile.getLocation().getServingWlanId()+"  Assigned host:  NULL");
	}
	
	
	/**
	 * @return the hosts
	 */
	public ArrayList<EdgeHost> getHosts() {
		return hosts;
	}
	
	
	/**
	 * @param hosts the hosts to set
	 */
	public void setHosts(ArrayList<EdgeHost> hosts) {
		this.hosts = hosts;
	}


}
