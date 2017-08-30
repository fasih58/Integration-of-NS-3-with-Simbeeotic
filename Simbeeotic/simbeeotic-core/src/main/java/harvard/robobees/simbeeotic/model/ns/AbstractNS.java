/*
 * Copyrights whatever
 */

package harvard.robobees.simbeeotic.model.ns;

import com.google.inject.Inject;
import harvard.robobees.simbeeotic.ClockControl;
import harvard.robobees.simbeeotic.SimTime;
import harvard.robobees.simbeeotic.configuration.ConfigurationAnnotations.GlobalScope;
import harvard.robobees.simbeeotic.model.*;
import harvard.robobees.simbeeotic.model.comms.MessageListener;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A model that is used to read ns parameters provided in Scenario xml file
 * It will be responsible for communicating between Simbeeotic and NS-3
 *
 * @author unnamed
 */

public abstract class AbstractNS extends AbstractModel {

    private PhysicalEntity host;
    protected ClockControl clock;
    private double txRxTime = 0;

    protected Queue<NSPacket> nsPackets = new LinkedList<NSPacket>();
    private Timer sendTimer;

    private Set<MessageListener> listeners = new HashSet<MessageListener>();

    // parameters
//    private Vector3f offset = new Vector3f();
    private int sendQueueSize = 100;


    /** {@inheritDoc} */
    public void initialize() {
        super.initialize();

        final long idlePollPeriod = 100;   // ms

        // a timer that periodically measures idle energy usage
//        createTimer(new TimerCallback() {
//
//            public void fire(SimTime time) {
//
//                double idleTime = idlePollPeriod - txRxTime;
//
//                if (idleTime < 0) {
//                    txRxTime -= idlePollPeriod;
//                }
//                else {
//                    txRxTime = 0;
//                    getAggregator().addValue("energy", "radio-idle", getIdleEnergy() * (idleTime / TimeUnit.SECONDS.toMillis(1)));
//                }
//            }
//
//        }, 0, TimeUnit.MICROSECONDS, idlePollPeriod, TimeUnit.MILLISECONDS);


        if (!nsPackets.isEmpty()) {
            long sendTime = 0;
            sendTime = nsPackets.peek().getTxTime().getTime();   // Work here
            sendTime /= TimeUnit.SECONDS.toMicros(1);

            // a timer that pulls messages off the message queue and sends them at the fastest rate possible
            sendTimer = createTimer(new TimerCallback() {

                public void fire(SimTime t) {
                    // send packet
                    transmit(nsPackets.poll());

                    if (!nsPackets.isEmpty()) {
                        long sendTime = nsPackets.peek().getTxTime().getTime();
                        sendTime /= TimeUnit.SECONDS.toMicros(1);

                        // schedule timer to send next packet
                        sendTimer.reset(t, sendTime, TimeUnit.MILLISECONDS, 0, TimeUnit.MILLISECONDS);
                    } else {
                        sendTimer.cancel();
                    }
                }
            }, sendTime, TimeUnit.MILLISECONDS, 0, TimeUnit.MILLISECONDS);
        }
    }

    /** {@inheritDoc} */
    public void finish() {
    }

    /**
     * Handles the reception of an NS transmission.
     *
     * @param time The time of the transmission.
     * @param event The details of the transmission.
     */
    @EventHandler
    public final void handleNSEvent(SimTime time, NSEvent event) {
        receive(time, event.getNsPacket());
    }

    /** {@inheritDoc} */
    public void receive(SimTime time, NSPacket nsPacket) {
//        getAggregator().addValue("energy", "radio-rx", timeToRx * getRxEnergy());
    }

    /** {@inheritDoc} */
    public void transmit(NSPacket nsPacket) {
//        getAggregator().addValue("energy", "radio-tx", timeToTx * getTxEnergy());
    }


//    protected abstract double getRxEnergy();


//    protected abstract double getTxEnergy();


//    protected abstract double getIdleEnergy();


//    /** {@inheritDoc} */
//    @Override
//    public final Vector3f getPosition() {
//
//        Vector3f pos = host.getTruthPosition();
//
//        if (offset.lengthSquared() > 0) {
//
//            // transform the offset (which is in the body frame)
//            // to account for the body's current orientation
//            Vector3f off = new Vector3f(offset);
//            Transform trans = new Transform();
//
//            trans.setIdentity();
//            trans.setRotation(host.getTruthOrientation());
//
//            trans.transform(off);
//
//            // add the offset to the absolute position (in the world frame)
//            pos.add(off);
//        }
//
//        return pos;
//    }

    /**
     * Notifies all listeners that a message has been received.
     *
     * @param time The simulation time when the message was received.
     * @param nsPacket The NS packet received.
     * @param rxPower The strength of the received signal (in dBm).
     */
    protected final void notifyListeners(SimTime time, NSPacket nsPacket, double rxPower) {

        for (MessageListener l : listeners) {
            l.messageReceived(time, nsPacket.getData().getBytes(), rxPower);
        }
    }

    /**
     * Subscribes a listener to notifications of message arrivals.
     *
     * @param listener The listener to invoke when a message is received.
     */
    public final void addMessageListener(MessageListener listener) {
        listeners.add(listener);
    }


    /**
     * Unsubscribes a listener from notifications.
     *
     * @param listener The listener that is to be removed from the set of
     *                 active listeners.
     */
    public final void removeMessageListener(MessageListener listener) {
        listeners.remove(listener);
    }


    protected final PhysicalEntity getHost() {
        return host;
    }


    /**
     * {@inheritDoc}
     *
     * This implementation ensures that the host model is a {@link PhysicalEntity}.
     */
    @Override
    public void setParentModel(Model parent) {

        super.setParentModel(parent);

        if (parent instanceof PhysicalEntity) {
            setHost((PhysicalEntity)parent);
        }
    }

    // this is only optional when wired up by the standard way (parent is a model that implements PhysicalEntity)
    @Inject(optional = true)
    public final void setHost(final PhysicalEntity host) {
        this.host = host;
    }


    @Inject
    public final void setClockControl(@GlobalScope final ClockControl clock) {
        this.clock = clock;
    }


//    @Inject
//    public final void setNsPacket(@Named("nsPacket") final NSPacket nsPacket) {
//        this.nsPackets.add(nsPacket);
//        this.nsPacket = nsPacket;
//    }


    public void addNsPacket(NSPacket nsPacket) {
        this.nsPackets.add(nsPacket);
//        this.nsPacket = nsPacket;
    }
}