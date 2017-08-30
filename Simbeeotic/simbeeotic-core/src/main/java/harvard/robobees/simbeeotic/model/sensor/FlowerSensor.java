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
package harvard.robobees.simbeeotic.model.sensor;


import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.linearmath.Transform;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import harvard.robobees.simbeeotic.configuration.ConfigurationAnnotations.GlobalScope;
import harvard.robobees.simbeeotic.model.Contact;
import harvard.robobees.simbeeotic.model.EntityInfo;
import harvard.robobees.simbeeotic.environment.WorldMap;
import harvard.robobees.simbeeotic.environment.WorldObject;

import javax.vecmath.Vector3f;
import java.util.Set;
import java.util.HashSet;


/**
 * A sensor that can detect if there are flowers within range of a sensor that is pointing
 * away from the body into the environment. The sensor's coverage volume is modeled as a
 * cone, and some approximations are made when determining if flowers intersect this volume.
 *
 * @author bkate
 */
public class FlowerSensor extends AbstractSensor {

    private DiscreteDynamicsWorld world;
    private WorldMap map;

    private float maxRange  = 1.0f;                // m;
    private float halfAngle = (float)Math.PI / 8;  // rad

    private static final float CONTACT_EPSILON = 0.01f;


    /**
     * Determines if there is a flower object within range and field of view of the sensor.
     *
     * @return True if a flower is detected, false otherwise.
     */
    public boolean isFlowerDetected() {
        return !senseFlowers().isEmpty();
    }


    /**
     * Determines if there is a flower object within range and field of view of the sensor.
     *
     * @return The meta-properties of the flower detected.
     */
    public Set<WorldObject> detectFlowers() {
        return senseFlowers();
    }


    private Set<WorldObject> senseFlowers() {

        // check if the sensor is actually in contact with another object.
        // if there is an object contacting the body in the vicinity of the sensor,
        // JBullet will actually see through the adjacent object to the next object. for example,
        // consider the body resting on a box that is sitting on the ground. if the sensor
        // is on the bottom of the body (which is contacting the box), the ray cast downward
        // will see through the box and hit the floor, even though the range to the closest
        // object should be 0. perhaps this is not the best strategy, but it works for most cases.
        for (Contact c : getHost().getContactPoints()) {

            Vector3f diff = new Vector3f();
            diff.sub(getOffset(), c.getBodyContactPoint());

            if (diff.length() <= CONTACT_EPSILON) {
                return new HashSet<WorldObject>();
            }
        }

        // we need to find the sensor's position and pointing vector
        // (in world coordinates) given the body's current orientation
        Vector3f rotatedOffset = new Vector3f(getOffset());
        Vector3f rotatedPointing = new Vector3f(getPointing());

        rotatedPointing.scale(maxRange);

        Transform trans = new Transform();

        trans.setIdentity();
        trans.setRotation(getHost().getTruthOrientation());

        trans.transform(rotatedOffset);
        trans.transform(rotatedPointing);

        Vector3f from = new Vector3f(getHost().getTruthPosition());
        Vector3f to = new Vector3f(getHost().getTruthPosition());

        from.add(rotatedOffset);
        to.add(rotatedOffset);
        to.add(rotatedPointing);

        // collide the ray with the world and see how far the sensor is from the ground
        RayCallback callback = new RayCallback(map.getTerrain().getObjectId(), maxRange);

        world.rayTest(from, to, callback);

        float range = callback.getRangeToGround();

        if (range <= maxRange) {

            // find the point on the ground that was intersected
            Vector3f groundPoint = new Vector3f();

            groundPoint.sub(to, from);
            groundPoint.normalize();
            groundPoint.scale(range);
            groundPoint.add(from, groundPoint);

            // determine the sensor's width at that range.
            double width = Math.tan(halfAngle) * range;

            // find all flowers in the sensor's area of coverage
            return map.getFlowers(groundPoint, width);
        }

        return new HashSet<WorldObject>();
    }


    @Inject
    public final void setDynamicsWorld(@GlobalScope DiscreteDynamicsWorld world) {
        this.world = world;
    }


    @Inject
    public final void setWorldMap(@GlobalScope WorldMap map) {
        this.map = map;
    }


    @Inject(optional = true)
    public final void setMaxRange(@Named("max-range") final float maxRange) {
        this.maxRange = maxRange;
    }


    /**
     * Sets the half-angle of the sensor. If the sensor is viewed as a cone, this would be
     * the angle from the center of the cone to the outer edge.
     *
     * @param angle The half-angle of the sensor's field of view.
     */
    @Inject(optional = true)
    public final void setAngle(@Named("half-angle") final float angle) {
        this.halfAngle = angle;
    }


    /**
     * A callback that handles ray intersections with objects in the world. It records
     * the minimum distance to any object.
     */
    private static class RayCallback extends CollisionWorld.RayResultCallback {

        private int groundObjectId;
        private float rayLength;
        private float groundRange = Float.POSITIVE_INFINITY;


        public RayCallback(int groundId, float length) {

            groundObjectId = groundId;
            rayLength = length;
        }

        public float addSingleResult(CollisionWorld.LocalRayResult rayResult, boolean normalInWorldSpace) {

            float dist = rayResult.hitFraction * rayLength;

            // check if we hit the ground
            if (((EntityInfo)rayResult.collisionObject.getUserPointer()).getObjectId() == groundObjectId) {
                groundRange = dist;
            }

            return rayResult.hitFraction;
        }

        public float getRangeToGround() {
            return groundRange;
        }
    }
}
