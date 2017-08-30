/*
 * Copyright (c) 2012, The President and Fellows of Harvard College.
 * All Rights Reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *  3. Neither the name of the University nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE UNIVERSITY OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package harvard.robobees.simbeeotic.example;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import harvard.robobees.simbeeotic.SimTime;
import harvard.robobees.simbeeotic.model.SimpleBee;
import harvard.robobees.simbeeotic.model.comms.MessageListener;
import harvard.robobees.simbeeotic.model.ns.NSPacket;
import org.apache.log4j.Logger;

import javax.vecmath.Vector3f;


/**
 * @author bkate
 */
public class InertBee extends SimpleBee implements MessageListener {

    private float maxVelocity = 0.0f;                  // m/s
    private float velocitySigma = 0.0f;                // m/s
    private float headingSigma = (float)Math.PI / 16;  // rad

    private static Logger logger = Logger.getLogger(InertBee.class);


    @Override
    public void initialize() {
        super.initialize();

        setHovering(true);

        getNs().addMessageListener(this);

        if(getModelId() == 1) {
            NSPacket nsPacket = new NSPacket("Hello", getModelId(), 3, new SimTime(1100), new SimTime(0));
            getNs().addNsPacket(nsPacket);
        }
    }


    @Override
    protected void updateKinematics(SimTime time) {

        // randomly vary the heading (rotation about the Z axis)
        turn((float)getRandom().nextGaussian() * headingSigma);

        // randomly vary the velocity in the X and Z directions
        Vector3f newVel = getDesiredLinearVelocity();

        newVel.add(new Vector3f(0, 0, 0));
        // cap the velocity
        if (newVel.length() > maxVelocity) {

            newVel.normalize();
            newVel.scale(maxVelocity);
        }

//        setTruthPosition(new Vector3f(getNs().nextX(), getNs().nextY(), 5));
        setDesiredLinearVelocity(newVel);

        Vector3f pos = getTruthPosition();
        Vector3f vel = getTruthLinearVelocity();

        logger.info("ID: " + getModelId() + "  " +
                    "time: " + time.getImpreciseTime() + "  " +
                    "pos: " + pos + "  " +
                    "vel: " + vel + " ");
    }

    public void messageReceived(SimTime time, byte[] data, double rxPower) {
        String rcvdMessage = new String(data);
        logger.info("ID: " + getModelId() + "  " +
                "time: " + time.getImpreciseTime() + "  " +
                "pos: " + getTruthPosition() + "  " +
                "msg: " + rcvdMessage + " ");
    }

    @Override
    public void finish() {
    }


    @Inject(optional = true)
    public final void setMaxVelocity(@Named(value = "max-vel") final float vel) {
        this.maxVelocity = 0;
    }


    @Inject(optional = true)
    public final void setVelocitySigma(@Named(value = "vel-sigma") final float sigma) {
        this.velocitySigma = 0;
    }


    @Inject(optional = true)
    public final void setHeadingSigma(@Named(value = "heading-sigma") final float sigma) {
        this.headingSigma = 0;
    }
}
