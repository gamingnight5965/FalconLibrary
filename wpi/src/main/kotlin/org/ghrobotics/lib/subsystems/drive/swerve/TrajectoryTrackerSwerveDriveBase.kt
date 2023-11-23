/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright 2019, Green Hope Falcons
 */

package org.ghrobotics.lib.subsystems.drive.swerve

import com.pathplanner.lib.controllers.PPHolonomicDriveController
import com.pathplanner.lib.path.PathConstraints
import com.pathplanner.lib.util.HolonomicPathFollowerConfig
import com.pathplanner.lib.util.ReplanningConfig
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.kinematics.SwerveDriveKinematics
import edu.wpi.first.math.kinematics.SwerveModulePosition
import edu.wpi.first.math.kinematics.SwerveModuleState
import org.ghrobotics.lib.commands.FalconSubsystem
import org.ghrobotics.lib.mathematics.units.Ampere
import org.ghrobotics.lib.mathematics.units.Meter
import org.ghrobotics.lib.mathematics.units.SIUnit
import org.ghrobotics.lib.mathematics.units.derived.LinearVelocity
import org.ghrobotics.lib.mathematics.units.derived.Radian
import org.ghrobotics.lib.mathematics.units.derived.Volt
import org.ghrobotics.lib.utils.Source

abstract class TrajectoryTrackerSwerveDriveBase : FalconSubsystem() {
    abstract val controller: PPHolonomicDriveController
    abstract val kinematics: SwerveDriveKinematics
    abstract val pathConstraints: PathConstraints
    abstract val pathFollowingConfig: HolonomicPathFollowerConfig

    /**
     * The replanning configuration. Defaults to no replanning. override to change
     */
    val replanningConfig: ReplanningConfig = ReplanningConfig()
    /**
     * The current inputs and outputs
     */
    abstract val swerveDriveIO: SwerveDriveIO
    abstract val swerveDriveInputs: AbstractSwerveDriveInputs

    abstract fun setOutputSI(
        states: Array<SwerveModuleState>,
    )

    abstract fun setOutputSI(
        speeds: ChassisSpeeds
    )
}

interface SwerveDriveIO {

    fun <T : AbstractSwerveDriveInputs> updateInputs(inputs: T)
    fun setModuleStates(states: Array<SwerveModuleState>)
    fun setNeutral()

    val positions: Array<SwerveModulePosition>

    val states: Array<SwerveModuleState>

    val gyro: Source<Rotation2d>
}

interface AbstractSwerveDriveInputs {
    var leftFrontVoltage: SIUnit<Volt>
    var rightFrontVoltage: SIUnit<Volt>
    var rightBackVoltage: SIUnit<Volt>
    var leftBackVoltage: SIUnit<Volt>

    var leftFrontCurrent: SIUnit<Ampere>
    var rightFrontCurrent: SIUnit<Ampere>
    var rightBackCurrent: SIUnit<Ampere>
    var leftBackCurrent: SIUnit<Ampere>

    var leftFrontPosition: SIUnit<Meter>
    var rightFrontPosition: SIUnit<Meter>
    var rightBackPosition: SIUnit<Meter>
    var leftBackPosition: SIUnit<Meter>

    var leftFrontRotation: SIUnit<Radian>
    var rightFrontRotation: SIUnit<Radian>
    var rightBackRotation: SIUnit<Radian>
    var leftBackRotation: SIUnit<Radian>

    var leftFrontVelocity: SIUnit<LinearVelocity>
    var rightFrontVelocity: SIUnit<LinearVelocity>
    var rightBackVelocity: SIUnit<LinearVelocity>
    var leftBackVelocity: SIUnit<LinearVelocity>

    var leftFrontFeedforward: SIUnit<Volt>
    var rightFrontFeedforward: SIUnit<Volt>
    var rightBackFeedforward: SIUnit<Volt>
    var leftBackFeedforward: SIUnit<Volt>

    var robotPose: Pose2d

    var chassisSpeeds: ChassisSpeeds

    var gyroRaw: SIUnit<Radian>
}
