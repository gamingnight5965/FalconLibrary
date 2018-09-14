/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.mathematics.twodim.control

import frc.team5190.lib.mathematics.twodim.geometry.Pose2d
import frc.team5190.lib.mathematics.twodim.geometry.Pose2dWithCurvature
import frc.team5190.lib.mathematics.twodim.geometry.Twist2d
import frc.team5190.lib.mathematics.twodim.trajectory.TrajectorySamplePoint
import frc.team5190.lib.mathematics.twodim.trajectory.TimedState

interface TrajectoryFollower {
    fun getSteering(robot: Pose2d, nanotime: Long = System.nanoTime()): Twist2d
    val point: TrajectorySamplePoint<TimedState<Pose2dWithCurvature>>
    val pose: Pose2d
    val isFinished: Boolean
}