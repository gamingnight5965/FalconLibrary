/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright 2019, Green Hope Falcons
 */

package org.ghrobotics.lib.motors.rev

import com.revrobotics.AlternateEncoderType
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel
import com.revrobotics.ControlType
import kotlin.properties.Delegates
import org.ghrobotics.lib.mathematics.units.Ampere
import org.ghrobotics.lib.mathematics.units.SIKey
import org.ghrobotics.lib.mathematics.units.SIUnit
import org.ghrobotics.lib.mathematics.units.amps
import org.ghrobotics.lib.mathematics.units.derived.Acceleration
import org.ghrobotics.lib.mathematics.units.derived.Velocity
import org.ghrobotics.lib.mathematics.units.derived.Volt
import org.ghrobotics.lib.mathematics.units.derived.volts
import org.ghrobotics.lib.mathematics.units.inAmps
import org.ghrobotics.lib.mathematics.units.nativeunit.NativeUnitModel
import org.ghrobotics.lib.motors.AbstractFalconMotor
import org.ghrobotics.lib.motors.FalconMotor

/**
 * Creates a Spark MAX motor controller. The alternate encoder CPR is defaulted
 * to the CPR of the REV Through Bore Encoder.
 *
 * @param canSparkMax The underlying motor controller.
 * @param model The native unit model.
 * @param useAlternateEncoder Whether to use the alternate encoder or not.
 * @param alternateEncoderCPR The CPR of the alternate encoder.
 */
class FalconMAX<K : SIKey>(
    val canSparkMax: CANSparkMax,
    private val model: NativeUnitModel<K>,
    useAlternateEncoder: Boolean = false,
    alternateEncoderCPR: Int = 8192
) : AbstractFalconMotor<K>() {

    /**
     * Creates a Spark MAX motor controller. The alternate encoder CPR is defaulted
     * to the CPR of the REV Through Bore Encoder.
     *
     * @param id The ID of the motor controller.
     * @param model The native unit model.
     * @param useAlternateEncoder Whether to use the alternate encoder or not.
     * @param alternateEncoderCPR The CPR of the alternate encoder.
     */
    constructor(
        id: Int,
        type: CANSparkMaxLowLevel.MotorType,
        model: NativeUnitModel<K>,
        useAlternateEncoder: Boolean = false,
        alternateEncoderCPR: Int = 8196
    ) : this(
        CANSparkMax(id, type), model, useAlternateEncoder, alternateEncoderCPR
    )

    /**
     * The PID controller for the Spark MAX.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val controller: CANPIDController = canSparkMax.pidController

    /**
     * The encoder for the Spark MAX.
     */
    override val encoder = FalconMAXEncoder(
        if (useAlternateEncoder) canSparkMax.getAlternateEncoder(
            AlternateEncoderType.kQuadrature,
            alternateEncoderCPR
        ) else canSparkMax.encoder, model
    )

    /**
     * Constructor that sets the feedback device and enables voltage compensation.
     */
    init {
//         controller.setFeedbackDevice(encoder.canEncoder)
        CANSensorShim.configCANEncoderonCanPIDController(controller, encoder.canEncoder)
        canSparkMax.enableVoltageCompensation(12.0)
    }

    /**
     * Returns the voltage across the motor windings.
     */
    override val voltageOutput: SIUnit<Volt>
        get() = (canSparkMax.appliedOutput * canSparkMax.busVoltage).volts

    /**
     * Returns the current drawn by the motor.
     */
    override val drawnCurrent: SIUnit<Ampere>
        get() = canSparkMax.outputCurrent.amps

    /**
     * Whether the output of the motor is inverted or not. This has
     * no effect on slave motors.
     */
    override var outputInverted: Boolean by Delegates.observable(false) { _, _, newValue ->
        canSparkMax.inverted = newValue
    }

    /**
     * Configures brake mode for the motor controller.
     */
    override var brakeMode: Boolean by Delegates.observable(false) { _, _, newValue ->
        canSparkMax.idleMode = if (newValue) CANSparkMax.IdleMode.kBrake else CANSparkMax.IdleMode.kCoast
    }

    /**
     * Configures voltage compensation for the motor controller.
     */
    override var voltageCompSaturation: SIUnit<Volt> by Delegates.observable(12.0.volts) { _, _, newValue ->
        canSparkMax.enableVoltageCompensation(newValue.value)
    }

    /**
     * Configures the motion profile cruise velocity for Smart Motion.
     */
    override var motionProfileCruiseVelocity: SIUnit<Velocity<K>> by Delegates.observable(SIUnit(0.0)) { _, _, newValue ->
        controller.setSmartMotionMaxVelocity(model.toNativeUnitVelocity(newValue).value * 60.0, 0)
    }

    /**
     * Configures the max acceleration for the motion profile generated by Smart Motion.
     */
    override var motionProfileAcceleration: SIUnit<Acceleration<K>> by Delegates.observable(SIUnit(0.0)) { _, _, newValue ->
        controller.setSmartMotionMaxAccel(model.toNativeUnitAcceleration(newValue).value * 60.0, 0)
    }

    /**
     * Configures the forward soft limit and enables it.
     */
    override var softLimitForward: SIUnit<K> by Delegates.observable(SIUnit(0.0)) { _, _, newValue ->
        canSparkMax.setSoftLimit(CANSparkMax.SoftLimitDirection.kForward,
            model.toNativeUnitPosition(newValue).value.toFloat()
        )
        canSparkMax.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, true)
    }

    /**
     * Configures a smart current limit for the motor.
     */
    var smartCurrentLimit: SIUnit<Ampere> by Delegates.observable(SIUnit(60.0)) { _, _, newValue ->
        canSparkMax.setSmartCurrentLimit(newValue.inAmps().toInt())
    }

    /**
     * Configures the reverse soft limit and enables it.
     */
    override var softLimitReverse: SIUnit<K> by Delegates.observable(SIUnit(0.0)) { _, _, newValue ->
        canSparkMax.setSoftLimit(CANSparkMax.SoftLimitDirection.kReverse,
            model.toNativeUnitPosition(newValue).value.toFloat()
        )
        canSparkMax.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, true)
    }

    /**
     * Sets a certain voltage across the motor windings.
     *
     * @param voltage The voltage to set.
     * @param arbitraryFeedForward The arbitrary feedforward to add to the motor output.
     */
    override fun setVoltage(voltage: SIUnit<Volt>, arbitraryFeedForward: SIUnit<Volt>) {
        controller.setReference(voltage.value, ControlType.kVoltage, 0, arbitraryFeedForward.value)
    }

    /**
     * Commands a certain duty cycle to the motor.
     *
     * @param dutyCycle The duty cycle to command.
     * @param arbitraryFeedForward The arbitrary feedforward to add to the motor output.
     */
    override fun setDutyCycle(dutyCycle: Double, arbitraryFeedForward: SIUnit<Volt>) {
        controller.setReference(dutyCycle, ControlType.kDutyCycle, 0, arbitraryFeedForward.value)
    }

    /**
     * Sets the velocity setpoint of the motor controller.
     *
     * @param velocity The velocity setpoint.
     * @param arbitraryFeedForward The arbitrary feedforward to add to the motor output.
     */
    override fun setVelocity(velocity: SIUnit<Velocity<K>>, arbitraryFeedForward: SIUnit<Volt>) {
        controller.setReference(
            model.toNativeUnitVelocity(velocity).value * 60,
            ControlType.kVelocity, 0, arbitraryFeedForward.value
        )
    }

    /**
     * Sets the position setpoint of the motor controller. This uses a motion profile
     * if motion profiling is configured.
     *
     * @param position The position setpoint.
     * @param arbitraryFeedForward The arbitrary feedforward to add to the motor output.
     */
    override fun setPosition(position: SIUnit<K>, arbitraryFeedForward: SIUnit<Volt>) {
        controller.setReference(
            model.toNativeUnitPosition(position).value,
            if (useMotionProfileForPosition) ControlType.kSmartMotion else ControlType.kPosition,
            0, arbitraryFeedForward.value
        )
    }

    /**
     * Gives the motor neutral output.
     */
    override fun setNeutral() = setDutyCycle(0.0)

    /**
     * Follows the output of another motor controller.
     *
     * @param motor The other motor controller.
     */
    override fun follow(motor: FalconMotor<*>): Boolean =
        if (motor is FalconMAX<*>) {
            canSparkMax.follow(motor.canSparkMax)
            true
        } else {
            super.follow(motor)
        }
}

fun <K : SIKey> falconMAX(
    canSparkMax: CANSparkMax,
    model: NativeUnitModel<K>,
    useAlternateEncoder: Boolean = false,
    alternateEncoderCPR: Int = 8192,
    block: FalconMAX<K>.() -> Unit
) = FalconMAX(canSparkMax, model, useAlternateEncoder, alternateEncoderCPR).also(block)

fun <K : SIKey> falconMAX(
    id: Int,
    type: CANSparkMaxLowLevel.MotorType,
    model: NativeUnitModel<K>,
    useAlternateEncoder: Boolean = false,
    alternateEncoderCPR: Int = 8192,
    block: FalconMAX<K>.() -> Unit
) = FalconMAX(id, type, model, useAlternateEncoder, alternateEncoderCPR).also(block)
