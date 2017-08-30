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


import harvard.robobees.simbeeotic.util.BoundingSphere;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.Set;


/**
 * An interface describing the base functionality of an object that has a physical
 * presence in the simulation.
 *
 * @author bkate
 */
public interface PhysicalEntity {

    public static final short COLLISION_NONE    = 0;
    public static final short COLLISION_BEE     = 1;
    public static final short COLLISION_FLOWER  = 2;
    public static final short COLLISION_HIVE    = 4;
    public static final short COLLISION_TERRAIN = 8;
    public static final short COLLISION_ALL     = Short.MAX_VALUE;


    /**
     * Initializes the physical entity (shape, mass, position, etc).
     */
    public void initialize();


    /**
     * Destroys the physical entity.
     */
    public void destroy();


    /**
     * Applies a force to the body (to take effect when time advances in the physical world). The
     * force is applied to the body's center of mass and is in effect (constant)
     * until it is cleared.
     *
     * @param F The force to be applied (Newtons, in the world frame).
     */
    public void applyForce(final Vector3f F);


    /**
     * Applies a force to the body (to take effect when time advances in the physical world). The
     * force is applied to the given offset position and is in effect (constant)
     * until it is cleared.
     *
     * @param F The force to be applied (Newtons, in the world frame).
     * @param offset The position on the body where the force will be applied (in the body frame).
     */
    public void applyForce(final Vector3f F, final Vector3f offset);


    /**
     * Applies an impulse (instantaneous force) to the body.
     * The force, effective immediately, is applied to the body's center of mass.
     *
     * @param F The force to be applied (Newtons, in the world frame).
     */
    public void applyImpulse(final Vector3f F);


    /**
     * Applies an impulse (instantaneous force) to the body.
     * The force, effective immediately, is applied to the given offset position.
     *
     * @param F The force to be applied (Newtons, in the world frame).
     * @param offset The position on the body where the force will be applied (in the body frame).
     */
    public void applyImpulse(final Vector3f F, final Vector3f offset);


    /**
     * Applies a torque (moment) about the body axes (to take effect when time advances in
     * the physical world). The torque is constantly applied until it is cleared.
     *
     * @param T The torque (in Newton-meters) to be applied about the body's center of mass.
     */
    public void applyTorque(final Vector3f T);


    /**
     * Applies a torque impulse (instantaneous moment) about the body axes. The results are
     * effective immediately.
     *
     * @param T The torque (in Newton-meters) to be applied about the body's center of mass.
     */
    public void applyTorqueImpulse(final Vector3f T);


    /**
     * Clears all forces and torques that are acting on the body. This call is useful if there
     * is a kinematic update loop in the model and the user wishes to apply the
     * necessary forces to move the body at each time step.
     */
    public void clearForces();


    /**
     * Zeroes the linear and angular velocity of the entity, and clears any forces acting on the body.
     */
    public void clearMotion();


    /**
     * Gets the identifier that is to distinguish this physical object in the virtual world.
     * This identifier should be used when sending updates to the {@link MotionRecorder}. This
     * identifier should not be confused with the model ID, which is assigned to abstract
     * entities in the simulation. A class that implements {@link Model} and {@link PhysicalEntity}
     * will end up with both identifiers.
     *
     * @return The unique physical identifier of this entity.
     */
    public int getObjectId();
    

    /**
     * Gets the truth position of the entity, (x, y, z), in meters,
     * relative to the world origin.
     *
     * @return The current truth position vector.
     */
    public Vector3f getTruthPosition();


    /**
     * Sets the truth position of the entity, (x, y, z), in meters,
     * relative to the world origin.
     *
     * @param truthPosition The current truth position vector.
     */
    public void setTruthPosition(Vector3f truthPosition);


    /**
     * Gets the truth orientation of the entity as a Quaternion.
     *
     * @return The quaternion that represents the rotation of the body.
     */
    public Quat4f getTruthOrientation();


    /**
     * Gets the truth linear velocity vector, in m/s. The velocity
     * is relative to the world coordinate frame, not the body.
     *
     * @return The current linear velocity of the entity.
     */
    public Vector3f getTruthLinearVelocity();


    /**
     * Gets the truth angular velocity about the body axes (x, y, z),
     * in rad/s, of the entity.
     *
     * @return The current angular acceleration vector of the entity.
     */
    public Vector3f getTruthAngularVelocity();


    /**
     * Gets the truth linear acceleration vector of the entity,
     * in m/s^2, in the world reference frame.
     *
     * @return The truth linear acceleration vector.
     */
    public Vector3f getTruthLinearAcceleration();


    /**
     * Gets the truth angular acceleration about the body axes (x, y, z),
     * in rad/s^2.
     *
     * @return The current angular acceleration of the entity.
     */
    public Vector3f getTruthAngularAcceleration();


    /**
     * Gets a current bounding sphere for the entity (in world coordinates), using the
     * truth position and orientation.
     *
     * @return The current bounding sphere.
     */
    public BoundingSphere getTruthBoundingSphere();


    /**
     * Gets the current set of contact points, as reported by the physics engine.
     *
     * @return The set of contacts.
     */
    public Set<Contact> getContactPoints();


    /**
     * Indicates that a model is interested in collisions involving this entity. When
     * a collision is detected, a {@link CollisionEvent} will be generated on the
     * given model.
     *
     * @param modelId The ID of the interested model.
     */
    public void addCollisionListener(int modelId);

}
