/**
 * Copyright (C) 2018 Arne Peters - arne.peters@tum.de Technische Universit�t M�nchen Chair for Robotics,
 * Artificial Intelligence and Embedded Systems Fakult�t f�r Informatik / I6, Boltzmannstra�e 3, 85748
 * Garching bei M�nchen, Germany http://www6.in.tum.de All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 * following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package de.tum.in.camp.kuka.ros;

import com.kuka.connectivity.motionModel.smartServo.SmartServo;
import com.kuka.connectivity.motionModel.smartServoLIN.SmartServoLIN;
import com.kuka.roboticsAPI.motionModel.SplineMotionCP;
import com.kuka.roboticsAPI.motionModel.SplineMotionJP;

import iiwa_msgs.SetPTPCartesianSpeedLimitsRequest;
import iiwa_msgs.SetPTPJointSpeedLimitsRequest;
import iiwa_msgs.SetSmartServoJointSpeedLimitsRequest;
import iiwa_msgs.SetSmartServoLinSpeedLimitsRequest;

public class SpeedLimits {
  // overall override factor
  private static double overrideReduction = 1.0; // relative
  
  // Joint motion limits
  private static double ss_relativeJointVelocity;             // relative
  private static double ss_relativeJointAcceleration;         // relative
  private static double ss_override_joint_acceleration = 1.0; // relative, between 0.0 and 10.0
  
  // PTP Joint motion limits
  private static double ptp_relativeJointVelocity =      1.0; // relative
  private static double ptp_relativeJointAcceleration =  1.0; // relative
  
  // Cartesian PTP motion limits
  private static double ptp_maxCartesianVelocity =       1.0; // m/s
  private static double ptp_maxOrientationVelocity =     0.5; // rad/s
  private static double ptp_maxCartesianAcceleration =   0.2; // m/s^2
  private static double ptp_maxOrientationAcceleration = 0.1; // rad/s^2
  private static double ptp_maxCartesianJerk =          -1.0; // m/s^3
  private static double ptp_maxOrientationJerk =        -1.0; // rad/s^3
  
  // Cartesian SmartServo limits
  private static double[] ss_maxTranslationalVelocity = {1.0, 1.0, 1.0}; // m/s
  private static double[] ss_maxRotationalVelocity =    {0.5, 0.5, 0.5}; // rad/s
  // TODO:
  // public static double ss_maxNullSpaceVelocity = ???;
  // public static double ss_maxNullSpaceAcceleration = ???;
  
  public static void init(Configuration configuration) {
    ss_relativeJointVelocity = configuration.getDefaultRelativeJointVelocity();
    ss_relativeJointAcceleration = configuration.getDefaultRelativeJointAcceleration();
  }

  public static void setOverrideRecution(double overrideReduction) {
    SpeedLimits.overrideReduction = overrideReduction;
  }
  
  public double getOverrideReduction() {
    return overrideReduction;
  }
  
  /**
   * Set PTP joint speed limits based on ROS service request
   * @param srvReq
   */
  public static void setPTPJointSpeedLimits(SetPTPJointSpeedLimitsRequest srvReq) {
    ptp_relativeJointVelocity = srvReq.getJointRelativeVelocity();
    ptp_relativeJointAcceleration = srvReq.getJointRelativeAcceleration();
  }

  /**
   * Set Cartesian PTP speed values based on ROS service request
   * @param srvReq
   */
  public static void setPTPCartesianSpeedLimits(SetPTPCartesianSpeedLimitsRequest srvReq) {
    ptp_maxCartesianVelocity = srvReq.getMaxCartesianVelocity();
    ptp_maxOrientationVelocity = srvReq.getMaxOrientationVelocity();
    ptp_maxCartesianAcceleration = srvReq.getMaxCartesianAcceleration();
    ptp_maxOrientationAcceleration = srvReq.getMaxOrientationAcceleration();
    ptp_maxCartesianJerk = srvReq.getMaxCartesianJerk();
    ptp_maxOrientationJerk = srvReq.getMaxOrientationJerk();
  }

  /**
   * Set SmartServo joint speed values based on ROS service request
   * @param srvReq
   */
  public static void setSmartServoJointSpeedLimits(SetSmartServoJointSpeedLimitsRequest srvReq) {
    ss_relativeJointVelocity = srvReq.getJointRelativeVelocity();
    ss_relativeJointAcceleration = srvReq.getJointRelativeAcceleration();
    ss_override_joint_acceleration = srvReq.getOverrideJointAcceleration();
  }
  
  /**
   * Set SmartServo lin speed values based on ROS service request
   * @param srvReq
   */
  public static void setSmartServoLinSpeedLimits(SetSmartServoLinSpeedLimitsRequest srvReq) {
    ss_maxTranslationalVelocity = Conversions.rosVectorToArray(srvReq.getMaxCartesianVelocity().getLinear());
    ss_maxRotationalVelocity = Conversions.rosVectorToArray(srvReq.getMaxCartesianVelocity().getAngular());
  }
  
  /**
   * Applies the configured speed limits on a given SmartServo motion container
   * @param motion
   */
  public static void applySpeedLimits(SmartServo motion) {
    if (ss_relativeJointVelocity > 0) {
      motion.setJointVelocityRel(ss_relativeJointVelocity);
    }
    if (ss_relativeJointAcceleration > 0) {
      motion.setJointAccelerationRel(ss_relativeJointAcceleration);
    }
    if (ss_override_joint_acceleration > 0) {
      motion.overrideJointAcceleration(ss_override_joint_acceleration); 
    }
  }
  
  public static void applySpeedLimits(SplineMotionJP<?> motion) {
    if (ptp_relativeJointVelocity > 0) {
      motion.setJointVelocityRel(ptp_relativeJointVelocity);
    }
    if (ptp_relativeJointAcceleration > 0) {
      motion.setJointAccelerationRel(ptp_relativeJointAcceleration);
    }
  }
  
  /**
   * Applies the configured speed limits on a given Cartesian PTP motion container
   * @param cartesianMotion
   */
  public static void applySpeedLimits(SplineMotionCP<?> cartesianMotion) {
    if (ptp_maxCartesianVelocity > 0) {
      cartesianMotion.setCartVelocity(Conversions.rosTranslationToKuka(ptp_maxCartesianVelocity));
    }
    if (ptp_maxOrientationVelocity > 0) {
      cartesianMotion.setOrientationVelocity(ptp_maxOrientationVelocity);
    }
    if (ptp_maxCartesianAcceleration > 0) {
      cartesianMotion.setCartAcceleration(Conversions.rosTranslationToKuka(ptp_maxCartesianAcceleration));
    }
    if (ptp_maxOrientationAcceleration > 0) {
      cartesianMotion.setOrientationAcceleration(ptp_maxOrientationAcceleration);
    }
    if (ptp_maxCartesianJerk > 0) {
      cartesianMotion.setCartJerk(Conversions.rosTranslationToKuka(ptp_maxCartesianJerk));
    }
    if (ptp_maxOrientationJerk > 0) {
      cartesianMotion.setOrientationJerk(ptp_maxOrientationJerk);
    }
  }
  
  /**
   * Applies the configured speed limits on a given SmartServoLIN motion container
   * @param linMotion
   */
  public static void applySpeedLimits(SmartServoLIN linMotion) {
    if (ss_maxTranslationalVelocity[0] > 0 || ss_maxTranslationalVelocity[1] > 0 || ss_maxTranslationalVelocity[2] > 0) {
      linMotion.setMaxTranslationVelocity(new double[]{
          Conversions.rosTranslationToKuka(ss_maxTranslationalVelocity[0]),
          Conversions.rosTranslationToKuka(ss_maxTranslationalVelocity[1]),
          Conversions.rosTranslationToKuka(ss_maxTranslationalVelocity[2])
      });
    }
    if (ss_maxRotationalVelocity[0] > 0 || ss_maxRotationalVelocity[1] > 0 || ss_maxRotationalVelocity[2] > 0) {
      linMotion.setMaxTranslationVelocity(new double[]{
          ss_maxRotationalVelocity[0],
          ss_maxRotationalVelocity[1],
          ss_maxRotationalVelocity[2]
      });
    }

    // linMotion.setMaxTranslationAcceleration(value);
    // linMotion.setMaxNullSpaceAcceleration(value);
    // linMotion.setMaxNullSpaceVelocity(value);
    // linMotion.setMaxOrientationAcceleration(value);
  }
}
