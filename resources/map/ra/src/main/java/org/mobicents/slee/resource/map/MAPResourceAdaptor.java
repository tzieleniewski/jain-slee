package org.mobicents.slee.resource.map;

import java.util.concurrent.ConcurrentHashMap;

import javax.slee.Address;
import javax.slee.AddressPlan;
import javax.slee.SLEEException;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ActivityHandle;
import javax.slee.resource.ActivityIsEndingException;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.FailureReason;
import javax.slee.resource.FireEventException;
import javax.slee.resource.FireableEventType;
import javax.slee.resource.IllegalEventException;
import javax.slee.resource.InvalidConfigurationException;
import javax.slee.resource.Marshaler;
import javax.slee.resource.ReceivableService;
import javax.slee.resource.ResourceAdaptor;
import javax.slee.resource.ResourceAdaptorContext;
import javax.slee.resource.SleeEndpoint;
import javax.slee.resource.UnrecognizedActivityHandleException;

import org.mobicents.protocols.ss7.map.MAPStackImpl;
import org.mobicents.protocols.ss7.map.api.MAPDialog;
import org.mobicents.protocols.ss7.map.api.MAPDialogListener;
import org.mobicents.protocols.ss7.map.api.MAPServiceListener;
import org.mobicents.protocols.ss7.map.api.MAPStack;
import org.mobicents.protocols.ss7.map.api.dialog.MAPAcceptInfo;
import org.mobicents.protocols.ss7.map.api.dialog.MAPCloseInfo;
import org.mobicents.protocols.ss7.map.api.dialog.MAPOpenInfo;
import org.mobicents.protocols.ss7.map.api.dialog.MAPProviderAbortInfo;
import org.mobicents.protocols.ss7.map.api.dialog.MAPRefuseInfo;
import org.mobicents.protocols.ss7.map.api.dialog.MAPUserAbortInfo;
import org.mobicents.protocols.ss7.map.api.service.supplementary.ProcessUnstructuredSSIndication;
import org.mobicents.protocols.ss7.map.api.service.supplementary.UnstructuredSSIndication;
import org.mobicents.protocols.ss7.sccp.SccpProvider;

/**
 * 
 * @author amit bhayani
 * 
 */
public class MAPResourceAdaptor implements ResourceAdaptor, MAPDialogListener, MAPServiceListener {

	private MAPStack mapStack = null;
	/**
	 * This is local proxy of provider.
	 */
	private MAPProviderImpl mapProviderImpl = null;
	private Tracer tracer;
	private transient SleeEndpoint sleeEndpoint = null;

	private ConcurrentHashMap<Long, MAPDialogActivityHandle> handlers = new ConcurrentHashMap<Long, MAPDialogActivityHandle>();

	private SccpProvider sccpProvider;

	private ResourceAdaptorContext resourceAdaptorContext;

	private EventIDCache eventIdCache = null;

	private transient static final Address address = new Address(AddressPlan.IP, "localhost");

	public MAPResourceAdaptor() {
		// TODO Auto-generated constructor stub
	}

	public void activityEnded(ActivityHandle activityHandle) {
		if (this.tracer.isFineEnabled()) {
			if (this.tracer.isFineEnabled()) {
				this.tracer.fine("Activity with handle " + activityHandle + " ended");
			}
		}
	}

	public void activityUnreferenced(ActivityHandle arg0) {
		// TODO Auto-generated method stub

	}

	public void administrativeRemove(ActivityHandle arg0) {
		// TODO Auto-generated method stub

	}

	public void eventProcessingFailed(ActivityHandle arg0, FireableEventType arg1, Object arg2, Address arg3,
			ReceivableService arg4, int arg5, FailureReason arg6) {
		// TODO Auto-generated method stub

	}

	public void eventProcessingSuccessful(ActivityHandle arg0, FireableEventType arg1, Object arg2, Address arg3,
			ReceivableService arg4, int arg5) {
		// TODO Auto-generated method stub

	}

	public void eventUnreferenced(ActivityHandle arg0, FireableEventType arg1, Object arg2, Address arg3,
			ReceivableService arg4, int arg5) {
		// TODO Auto-generated method stub

	}

	public Object getActivity(ActivityHandle handle) {
		return ((MAPDialogActivityHandle) handle).getMAPDialog();
	}

	public ActivityHandle getActivityHandle(Object arg0) {
		Long id = ((MAPDialog) arg0).getDialogId();
		return handlers.get(id);
	}

	public Marshaler getMarshaler() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getResourceAdaptorInterface(String arg0) {
		return this.mapProviderImpl;
	}

	public void queryLiveness(ActivityHandle arg0) {
		// TODO Auto-generated method stub

	}

	public void raActive() {

		// TODO : How do we get sccpProvider Object?
		this.mapStack = new MAPStackImpl(this.sccpProvider);
		org.mobicents.protocols.ss7.map.api.MAPProvider mapProvider = this.mapStack.getMAPProvider();

		this.mapProviderImpl = new MAPProviderImpl(mapProvider);

		mapProvider.addMAPDialogListener(this);
		mapProvider.addMAPServiceListener(this);

		this.sleeEndpoint = resourceAdaptorContext.getSleeEndpoint();
	}

	public void raConfigurationUpdate(ConfigProperties arg0) {
		// TODO Auto-generated method stub

	}

	public void raConfigure(ConfigProperties arg0) {
		// TODO Auto-generated method stub

	}

	public void raInactive() {
		org.mobicents.protocols.ss7.map.api.MAPProvider mapProvider = this.mapStack.getMAPProvider();
		mapProvider.removeMAPDialogListener(this);
		mapProvider.removeMAPServiceListener(this);

	}

	public void raStopping() {
		// TODO Auto-generated method stub

	}

	public void raUnconfigure() {
		// TODO Auto-generated method stub

	}

	public void raVerifyConfiguration(ConfigProperties arg0) throws InvalidConfigurationException {
		// TODO Auto-generated method stub

	}

	public void serviceActive(ReceivableService arg0) {
		// TODO Auto-generated method stub

	}

	public void serviceInactive(ReceivableService arg0) {
		// TODO Auto-generated method stub

	}

	public void serviceStopping(ReceivableService arg0) {
		// TODO Auto-generated method stub

	}

	public void setResourceAdaptorContext(ResourceAdaptorContext raContext) {
		this.resourceAdaptorContext = raContext;
		this.tracer = resourceAdaptorContext.getTracer(MAPResourceAdaptor.class.getSimpleName());

		this.eventIdCache = new EventIDCache(this.tracer);
	}

	public void unsetResourceAdaptorContext() {
		this.resourceAdaptorContext = null;
	}

	/**
	 * MAPDialogListener methods
	 */
	public void onMAPAcceptInfo(MAPAcceptInfo mapAcceptInfo) {
		MAPDialog mapDialog = mapAcceptInfo.getMAPDialog();

		if (this.tracer.isFineEnabled()) {
			this.tracer.fine("Received MAPAcceptInfo for DialogId " + mapDialog.getDialogId());
		}

		MAPDialogActivityHandle handle = this.handlers.get(mapDialog.getDialogId());

		if (handle == null) {
			this.tracer.severe("Received MAPAcceptInfo but there is no Handler for this Dialog");
			return;
		}

		this.fireEvent("org.mobicents.protocols.ss7.map.ACCEPT_INFO", handle, mapAcceptInfo);
	}

	public void onMAPCloseInfo(MAPCloseInfo mapCloseInfo) {
		MAPDialog mapDialog = mapCloseInfo.getMAPDialog();

		if (this.tracer.isFineEnabled()) {
			this.tracer.fine("Received MAPCloseInfo for DialogId " + mapDialog.getDialogId());
		}

		MAPDialogActivityHandle handle = this.handlers.remove(mapDialog.getDialogId());

		if (handle == null) {
			this.tracer.severe("Received MAPCloseInfo but there is no Handler for this Dialog");
			return;
		}

		this.fireEvent("org.mobicents.protocols.ss7.map.ACCEPT_INFO", handle, mapCloseInfo);
	}

	public void onMAPOpenInfo(MAPOpenInfo mapOpenInfo) {
		MAPDialog mapDialog = mapOpenInfo.getMAPDialog();

		if (this.tracer.isFineEnabled()) {
			this.tracer.fine("Received MAPOpenInfo for DialogId " + mapDialog.getDialogId());
		}

		MAPDialogActivityHandle handle = new MAPDialogActivityHandle(mapDialog);

		this.handlers.put(mapDialog.getDialogId(), handle);

		this.fireEvent("org.mobicents.protocols.ss7.map.OPEN_INFO", handle, mapOpenInfo);
	}

	public void onMAPProviderAbortInfo(MAPProviderAbortInfo mapProviderAbortInfo) {
		MAPDialog mapDialog = mapProviderAbortInfo.getMAPDialog();

		if (this.tracer.isFineEnabled()) {
			this.tracer.fine("Received MAPCloseInfo for DialogId " + mapDialog.getDialogId());
		}

		MAPDialogActivityHandle handle = this.handlers.remove(mapDialog.getDialogId());

		if (handle == null) {
			this.tracer.severe("Received MAPProviderAbortInfo but there is no Handler for this Dialog");
			return;
		}

		this.fireEvent("org.mobicents.protocols.ss7.map.PROVIDER_ABORT_INFO", handle, mapProviderAbortInfo);

	}

	public void onMAPRefuseInfo(MAPRefuseInfo arg0) {
		// TODO Auto-generated method stub

	}

	public void onMAPUserAbortInfo(MAPUserAbortInfo mapUserAbortInfo) {
		MAPDialog mapDialog = mapUserAbortInfo.getMAPDialog();

		if (this.tracer.isFineEnabled()) {
			this.tracer.fine("Received MAPCloseInfo for DialogId " + mapDialog.getDialogId());
		}

		MAPDialogActivityHandle handle = this.handlers.remove(mapDialog.getDialogId());

		if (handle == null) {
			this.tracer.severe("Received MAPUserAbortInfo but there is no Handler for this Dialog");
			return;
		}

		this.fireEvent("org.mobicents.protocols.ss7.map.USER_ABORT_INFO", handle, mapUserAbortInfo);
	}

	/**
	 * MAPServiceListener methods
	 */
	public void onProcessUnstructuredSSIndication(ProcessUnstructuredSSIndication processUnstrSSInd) {
		MAPDialog mapDialog = processUnstrSSInd.getMAPDialog();

		if (this.tracer.isFineEnabled()) {
			this.tracer.fine("Received ProcessUnstructuredSSIndication for DialogId " + mapDialog.getDialogId());
		}

		MAPDialogActivityHandle handle = this.handlers.remove(mapDialog.getDialogId());

		if (handle == null) {
			this.tracer.severe("Received ProcessUnstructuredSSIndication but there is no Handler for this Dialog");
			return;
		}

		this.fireEvent("org.mobicents.protocols.ss7.map.PROCESS_UNSTRUCTURED_SS_REQUEST_INDICATION", handle, processUnstrSSInd);
	}

	public void onUnstructuredSSIndication(UnstructuredSSIndication unstrSSInd) {
		MAPDialog mapDialog = unstrSSInd.getMAPDialog();

		if (this.tracer.isFineEnabled()) {
			this.tracer.fine("Received UnstructuredSSIndication for DialogId " + mapDialog.getDialogId());
		}

		MAPDialogActivityHandle handle = this.handlers.remove(mapDialog.getDialogId());

		if (handle == null) {
			this.tracer.severe("Received UnstructuredSSIndication but there is no Handler for this Dialog");
			return;
		}

		this.fireEvent("org.mobicents.protocols.ss7.map.UNSTRUCTURED_SS_REQUEST_INDICATION", handle, unstrSSInd);
	}

	/**
	 * Private methods
	 */
	private void fireEvent(String eventName, ActivityHandle handle, Object event) {

		FireableEventType eventID = eventIdCache.getEventId(this.resourceAdaptorContext.getEventLookupFacility(),
				eventName);

		if (eventID == null) {
			tracer.severe("Event id for " + eventID + " is unknown, cant fire!!!");
		} else {
			try {
				sleeEndpoint.fireEvent(handle, eventID, event, address, null);
			} catch (UnrecognizedActivityHandleException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (IllegalEventException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (ActivityIsEndingException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (NullPointerException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (SLEEException e) {
				this.tracer.severe("Error while firing event", e);
			} catch (FireEventException e) {
				this.tracer.severe("Error while firing event", e);
			}
		}
	}

}