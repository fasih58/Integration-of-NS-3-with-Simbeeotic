/*
 * Copyrights whatever
 */

package harvard.robobees.simbeeotic.model.ns;


import harvard.robobees.simbeeotic.SimTime;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple container class for frequency bands.
 *
 * @author unnamed
 */
public class NSPacket {

    private AtomicInteger id;
    private List<Integer> path;
    private String data;
    private int fId;
    private int tId;
    private SimTime fbTx;
    private SimTime lbRx;


    public NSPacket(String msg, int fId, int tId, SimTime fbTx, SimTime lbRx) {
        this.data = msg;
        this.fId = fId;
        this.tId = tId;
        this.fbTx = fbTx;
        this.lbRx = lbRx;
    }


    /**
     * Gets the center frequency of this band.
     *
     * @return The center frequency (in MHz).
     */
    public String getData() {
        return data;
    }

    /**
     * Gets the center frequency of this band.
     *
     * @return The center frequency (in MHz).
     */
    public int getFromId() {
        return fId;
    }

    /**
     * Gets the center frequency of this band.
     *
     * @return The center frequency (in MHz).
     */
    public int getToId() {
        return tId;
    }

    /**
     * Gets the center frequency of this band.
     *
     * @return The center frequency (in MHz).
     */
    public SimTime getTxTime() {
        return fbTx;
    }

    /**
     * Gets the center frequency of this band.
     *
     * @return The center frequency (in MHz).
     */
    public SimTime getRxTime() {
        return lbRx;
    }
}
