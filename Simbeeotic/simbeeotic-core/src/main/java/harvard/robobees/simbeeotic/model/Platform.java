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
package harvard.robobees.simbeeotic.model;


import harvard.robobees.simbeeotic.model.comms.AbstractRadio;
import harvard.robobees.simbeeotic.model.ns.AbstractNS;
import harvard.robobees.simbeeotic.model.sensor.AbstractSensor;

import java.util.Set;


/**
 * An interface that allows a model access to attached sensors and radio.
 *
 * @author bkate
 */
public interface Platform extends TimerFactory {


    /**
     * Gets the sensor with the given name.
     *
     * @param name The name of the sensor to retrieve.
     *
     * @return The sensor, or {@code null} if none exists for the given name.
     */
    public AbstractSensor getSensor(final String name);


    /**
     * Gets the sensor with the given name. An attempt will be made to cast
     * the sensor to the given type.
     *
     * @param name The name of the sensor to retrieve.
     * @param type The type to which the sensor is cast prior to returning.
     *
     * @return The sensor, or {@code null} if none exists for the given name.
     */
    public <T> T getSensor(final String name, Class<T> type);


    /**
     * Gets the sensor(s) of a given type attached to this model.
     *
     * @param type The type of sensor to search for.
     *
     * @return The sensor(s), or an empty set if none exists for the given type.
     */
    public <T> Set<T> getSensors(Class<T> type);


    /**
     * Gets all the sensors attached to this model.
     *
     * @return The set of sensors.
     */
    public Set<AbstractSensor> getSensors();


    /**
     * Gets the radio attached to this model.
     *
     * @return The radio, or {@code null} if none is attached.
     */
    public AbstractRadio getRadio();


    /**
     * Gets the ns attached to this model.
     *
     * @return The ns, or {@code null} if none is attached.
     */
    public AbstractNS getNs();
}
