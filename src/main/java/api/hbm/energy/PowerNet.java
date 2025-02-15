package api.hbm.energy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hbm.config.GeneralConfig;

import api.hbm.energy.IEnergyConnector.ConnectionPriority;
import net.minecraft.tileentity.TileEntity;

/**
 * Basic IPowerNet implementation. The behavior of this demo might change inbetween releases, but the API remains the same.
 * For more consistency please implement your own IPowerNet.
 * @author hbm
 */
public class PowerNet implements IPowerNet {
	
	private boolean valid = true;
	private HashMap<Integer, IEnergyConductor> links = new HashMap();
	private HashMap<Integer, Integer> proxies = new HashMap();
	private List<IEnergyConnector> subscribers = new ArrayList();

	public static List<PowerNet> trackingInstances = null;
	protected long totalTransfer = 0;

	@Override
	public void joinNetworks(IPowerNet network) {
		
		if(network == this)
			return; //wtf?!

		for(IEnergyConductor conductor : network.getLinks()) {
			joinLink(conductor);
		}
		network.getLinks().clear();
		
		for(IEnergyConnector connector : network.getSubscribers()) {
			this.subscribe(connector);
		}
		
		network.destroy();
	}

	@Override
	public IPowerNet joinLink(IEnergyConductor conductor) {
		
		if(conductor.getPowerNet() != null)
			conductor.getPowerNet().leaveLink(conductor);
		
		conductor.setPowerNet(this);
		int identity = conductor.getIdentity();
		this.links.put(identity, conductor);
		
		if(conductor.hasProxies()) {
			for(Integer i : conductor.getProxies()) {
				this.proxies.put(i, identity);
			}
		}
		
		return this;
	}

	@Override
	public void leaveLink(IEnergyConductor conductor) {
		conductor.setPowerNet(null);
		int identity = conductor.getIdentity();
		this.links.remove(identity);
		
		if(conductor.hasProxies()) {
			for(Integer i : conductor.getProxies()) {
				this.proxies.remove(i);
			}
		}
	}

	@Override
	public void subscribe(IEnergyConnector connector) {
		this.subscribers.add(connector);
	}

	@Override
	public void unsubscribe(IEnergyConnector connector) {
		this.subscribers.remove(connector);
	}

	@Override
	public boolean isSubscribed(IEnergyConnector connector) {
		return this.subscribers.contains(connector);
	}

	@Override
	public List<IEnergyConductor> getLinks() {
		List<IEnergyConductor> linkList = new ArrayList();
		linkList.addAll(this.links.values());
		return linkList;
	}

	public HashMap<Integer, Integer> getProxies() {
		HashMap<Integer, Integer> proxyCopy = new HashMap(proxies);
		return proxyCopy;
	}

	@Override
	public List<IEnergyConnector> getSubscribers() {
		return this.subscribers;
	}
	
	@Override
	public void destroy() {
		this.valid = false;
		this.subscribers.clear();
		
		for(IEnergyConductor link : this.links.values()) {
			link.setPowerNet(null);
		}
		
		this.links.clear();
	}
	
	@Override
	public boolean isValid() {
		return this.valid;
	}

	@Override
	public long getTotalTransfer() {
		return this.totalTransfer;
	}
	
	public long lastCleanup = System.currentTimeMillis();
	
	@Override
	public long transferPower(long power) {
		long result = 0;

		if (trackingInstances != null && !trackingInstances.isEmpty()) {
			List<PowerNet> cache = new ArrayList(trackingInstances.size());
			cache.addAll(trackingInstances);
			trackingInstances.clear();

			trackingInstances.add(this);
			result = fairTransfer(this.subscribers, power);
			trackingInstances.addAll(cache);

			cache.clear();
			cache = null;
		} else {
			trackingInstances.clear();
			trackingInstances.add(this);
			result = fairTransfer(this.subscribers, power);
		}

		return result;
	}

	public static void cleanup(List<IEnergyConnector> subscribers) {

		subscribers.removeIf(x -> 
			x == null || !(x instanceof TileEntity) || ((TileEntity)x).isInvalid() || !x.isLoaded()
		);
	}

	public static boolean shouldSend(ConnectionPriority senderPrio, ConnectionPriority p, IEnergyConnector x){
		return (x.getPriority() == p) && (!x.isStorage() || (senderPrio.compareTo(p) <= 0));
	}

	public static long fairTransferWithPrio(ConnectionPriority senderPrio, List<IEnergyConnector> subscribers, long power) {
		
		if(power <= 0) return 0;
		
		if(subscribers.isEmpty())
			return power;
		
		cleanup(subscribers);
		
		ConnectionPriority[] priorities = new ConnectionPriority[] {ConnectionPriority.HIGH, ConnectionPriority.NORMAL, ConnectionPriority.LOW};
		
		long totalTransfer = 0;
		
		for(ConnectionPriority p : priorities) {
			
			List<IEnergyConnector> subList = new ArrayList();
			subscribers.forEach(x -> {
				if(shouldSend(senderPrio, p, x)) {
					subList.add(x);
				}
			});
			
			if(subList.isEmpty())
				continue;
			
			List<Long> weight = new ArrayList();
			long totalReq = 0;
			
			for(IEnergyConnector con : subList) {
				long req = con.getTransferWeight();
				weight.add(req);
				totalReq += req;
			}
			
			if(totalReq == 0)
				continue;
			
			long totalGiven = 0;
			
			for(int i = 0; i < subList.size(); i++) {
				IEnergyConnector con = subList.get(i);
				long req = weight.get(i);
				double fraction = (double)req / (double)totalReq;
				
				long given = (long) Math.floor(fraction * power);
				
				totalGiven += (given - con.transferPower(given));

				if(con instanceof TileEntity) {
					TileEntity tile = (TileEntity) con;
					tile.getWorld().markChunkDirty(tile.getPos(), tile);
				}
			}
			
			power -= totalGiven;
			totalTransfer += totalGiven;
		}

		if(trackingInstances != null) {
			
			for(int i = 0; i < trackingInstances.size(); i++) {
				PowerNet net = trackingInstances.get(i);
				net.totalTransfer += totalTransfer;
			}
			
			trackingInstances.clear();
		}
		
		return power;
	}

	public static long fairTransfer(List<IEnergyConnector> subscribers, long power) {

		if (power <= 0)
			return 0;

		if (subscribers.isEmpty())
			return power;

		cleanup(subscribers);

		ConnectionPriority[] priorities = new ConnectionPriority[] { ConnectionPriority.HIGH, ConnectionPriority.NORMAL,
				ConnectionPriority.LOW };

		long totalTransfer = 0;

		for (ConnectionPriority p : priorities) {

			List<IEnergyConnector> subList = new ArrayList();
			subscribers.forEach(x -> {
				if (x.getPriority() == p) {
					subList.add(x);
				}
			});

			if (subList.isEmpty())
				continue;

			List<Long> weight = new ArrayList();
			long totalReq = 0;

			for (IEnergyConnector con : subList) {
				long req = con.getTransferWeight();
				weight.add(req);
				totalReq += req;
			}

			if (totalReq == 0)
				continue;

			long totalGiven = 0;

			for (int i = 0; i < subList.size(); i++) {
				IEnergyConnector con = subList.get(i);
				long req = weight.get(i);
				double fraction = (double) req / (double) totalReq;

				long given = (long) Math.floor(fraction * power);

				totalGiven += (given - con.transferPower(given));

				if (con instanceof TileEntity) {
					TileEntity tile = (TileEntity) con;
					tile.getWorld().markChunkDirty(tile.getPos(), tile);
				}
			}

			power -= totalGiven;
			totalTransfer += totalGiven;
		}

		if (trackingInstances != null) {

			for (int i = 0; i < trackingInstances.size(); i++) {
				PowerNet net = trackingInstances.get(i);
				net.totalTransfer += totalTransfer;
			}

			trackingInstances.clear();
		}
		return power;
	}

	@Override
	public void reevaluate() {
		
		if(!GeneralConfig.enableReEval) {
			this.destroy();
			return;
		}

		HashMap<Integer, IEnergyConductor> copy = new HashMap(links);
		HashMap<Integer, Integer> proxyCopy = new HashMap(proxies);
		
		for(IEnergyConductor link : copy.values()) {
			this.leaveLink(link);
		}
		
		for(IEnergyConductor link : copy.values()) {
			
			link.setPowerNet(null);
			link.reevaluate(copy, proxyCopy);
			
			if(link.getPowerNet() == null) {
				link.setPowerNet(new PowerNet().joinLink(link));
			}
		}
	}
}
