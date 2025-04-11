package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PS4Controller;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.cameraserver.CameraServer;

import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import com.ctre.phoenix6.hardware.TalonFX;
import edu.wpi.first.wpilibj.DigitalInput;


import edu.wpi.first.wpilibj.XboxController;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to each mode, as
 * described in the TimedRobot documentation. If you change the name of this class or the package after creating this
 * project, you must also update the build.gradle file in the project.
 */
public class Robot extends TimedRobot {
  private static Robot instance;
  private Command m_autonomousCommand;
  private RobotContainer m_robotContainer;
private XboxController Driver;
private XboxController Operater;
private Timer disabledTimer;
private static final String kDefaultAuto = "Default";
private static final String kCustomAuto = "My Auto";
private static final String kopenside = "open side";
private String m_autoSelected;
private final SendableChooser<String> m_chooser = new SendableChooser<>();


//Callouts//

//Motors that outtake the Coral//
private SparkMax CoralMotorL;
private SparkMax CoralMotorR;

//private SparkMax BallMotorP;
//private TalonFX BallMotorI;


//Human source Intake//
private SparkMax CSource;

//Lift the Motor//
private TalonFX liftMotor;
//private SparkMax liftMotor;

// timer
Timer timer1 = new Timer();




private final DoubleSolenoid Endsolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 7, 6);
private final DoubleSolenoid clawsolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0,1);

 private boolean BallOut = false;
// private boolean Process   or = false;
// private boolean ground = false;
 private boolean End = false;

 private int scoreElevatorPos = 1;
private double stateStartTime = 0.0;
private boolean waitFor(double seconds) {
  return Timer.getFPGATimestamp() - stateStartTime >= seconds;
}

public Robot() {
  instance = this;
  // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
  // autonomous chooser on the dashboard.
  
  //Controller type//  
  Driver = new XboxController(Constants.OperatorConstants.kDriverControllerPort);
  Operater = new XboxController(Constants.OperatorConstants.kOperatorControllerPort);

  //Driver = new PS4ontroller(Constants.OperatorConstants.kDriverControllerPort);
  //Operater = new PS4Controller(Constants.OperatorConstants.kOperatorControllerPort);


  CoralMotorL = new SparkMax(Constants.OperatorConstants.CoralMotorL, MotorType.kBrushless);
  CoralMotorR = new SparkMax(Constants.OperatorConstants.CoralMotorR, MotorType.kBrushless);
  //BallMotorI= new TalonFX(Constants.OperatorConstants.BallMotorI);
  //BallMotorP= new SparkMax(Constants.OperatorConstants.BallMotorP, MotorType.kBrushless);
  CSource= new SparkMax(Constants.OperatorConstants.CSource, MotorType.kBrushless);

  liftMotor= new TalonFX(Constants.OperatorConstants.liftMotor);

}

  public static Robot getInstance()
  {
    return instance;
  }

  /**
   * This function is run when the robot is first started up and should be used for any initialization code.
   */
  @Override
  public void robotInit(){

  m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
  m_chooser.addOption("My Auto", kCustomAuto);
  m_chooser.addOption("open side", kopenside);
  SmartDashboard.putData("Auto choices", m_chooser);
  // camera use
  //CameraServer.startAutomaticCapture();
  // timer
  timer1.start();
  {
    CameraServer.startAutomaticCapture();

    // Instantiate our RobotContainer.  This will perform all our button bindings, and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();



    //liftMotor = m_robotContainer.getLiftMotor();



    // Create a timer to disable motor brake a few seconds after disable.  This will let the robot stop
    // immediately when disabled, but then also let it be pushed more 
    disabledTimer = new Timer();
   // Endsolenoid.set(Value.kReverse);
   // Armsolenoid.set(Value.kReverse);
    if (isSimulation())
    {
      DriverStation.silenceJoystickConnectionWarning(true);
    }
  }
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics that you want ran
   * during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic()
  {
    // Runs the Scheduler.  This is responsible for polling buttons, adding newly-scheduled
    // commands, running already-scheduled commands, removing finished or interrupted commands,
    // and running subsystem periodic() methods.  This must be called from the robot's periodic
    // block in order for anything in the Command-based framework to work.
    CommandScheduler.getInstance().run();
  }

  /**
   * This function is called once each time the robot enters Disabled mode.
   */
  @Override
  public void disabledInit()
  {
    m_robotContainer.setMotorBrake(true);
    disabledTimer.reset();
    disabledTimer.start();
  }

  @Override
  public void disabledPeriodic()
  {
    if (disabledTimer.hasElapsed(Constants.DrivebaseConstants.WHEEL_LOCK_TIME))
    {
      m_robotContainer.setMotorBrake(true);
      disabledTimer.stop();
      disabledTimer.reset();
    }
  }

  /**
   * This autonomous runs the autonomous command selected by your {@link RobotContainer} class.
   */
  @Override
  public void autonomousInit()
  {
    m_robotContainer.setMotorBrake(true);
    String selectedAuto = m_robotContainer.getSelectedAuto();
    m_autonomousCommand = m_robotContainer.getAutonomousCommand(selectedAuto);
    // schedule the autonomous command (example)
    if (m_autonomousCommand != null)
    {
      m_autonomousCommand.schedule();
    }


    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
    timer1.reset();
  }


  /**
   * This function is called periodically during autonomous.
   */
  
   //Auto program remeber to make a program for coral
  @Override
  public void autonomousPeriodic() {
    liftMotor.set(0);
    switch (m_autoSelected) { //how much time after is how long it will run
     case kCustomAuto:
     break;
      case kDefaultAuto: //amp side
      default:
      if(timer1.get() < 1.50){ //everything off
        liftMotor.set(0);
        CoralMotorL.set(0);
        CoralMotorR.set(0);
      }else if(timer1.get() < 2.55){ //everything off
        //liftMotor.set(9);
        CoralMotorL.set(3);
        CoralMotorR.set(-3);
      }else if(timer1.get() < 3.5){ //everything off
        //liftMotor.set(0);
        CoralMotorL.set(0);
        CoralMotorR.set(0);
    break;
    }
   }

  }

  @Override
  public void teleopInit()
  {
    // This makes sure that the autonomous stops running when
    // teleop starts running. If you want the autonomous to
    // continue until interrupted by another command, remove
    // this line or comment it out.
    if (m_autonomousCommand != null)
    {
      m_autonomousCommand.cancel();
    } else
    {
      CommandScheduler.getInstance().cancelAll();
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic(){
  
   /*
    * Elevator Positon Switching Logic
    */
    //reset counter incase of hiccup
    if (Operater.getBackButtonPressed()) {
      scoreElevatorPos = 1; // Reset to L1
      stateStartTime = Timer.getFPGATimestamp(); // Record the start time
    }

    // Check if the elevator is in a specific position and if the button is pressed
    //If L1 position
    else if (Driver.getAButton() && scoreElevatorPos == 1) {
      scoreElevatorPos = 6; // Start the sequence for L2
      stateStartTime = Timer.getFPGATimestamp(); // Record the start time
  } else if (Driver.getBButton() && scoreElevatorPos == 1) {
    scoreElevatorPos = 7; // Start the sequence for L3
    stateStartTime = Timer.getFPGATimestamp(); // Record the start time
  } else if (Driver.getYButton() && scoreElevatorPos == 1) {
    scoreElevatorPos = 8; // Start the sequence for L4
    stateStartTime = Timer.getFPGATimestamp(); // Record the start time
  } 
  //If L2 Position
  else if (Driver.getXButton() && scoreElevatorPos == 2) {
    scoreElevatorPos = 9; // Start the sequence for L1
    stateStartTime = Timer.getFPGATimestamp(); // Record the start time
} else if (Driver.getBButton() && scoreElevatorPos == 2) {
  scoreElevatorPos = 10; // Start the sequence For L3
  stateStartTime = Timer.getFPGATimestamp(); // Record the start time
} else if (Driver.getYButton() && scoreElevatorPos == 2) {
  scoreElevatorPos = 11; // Start the sequence for L4
  stateStartTime = Timer.getFPGATimestamp(); // Record the start time
}
//If L3 Position
else if (Driver.getXButton() && scoreElevatorPos == 3) {
  scoreElevatorPos = 12; // Start the sequence for L1
  stateStartTime = Timer.getFPGATimestamp(); // Record the start time
} else if (Driver.getAButton() && scoreElevatorPos == 3) {
scoreElevatorPos = 13; // Start the sequence For L2
stateStartTime = Timer.getFPGATimestamp(); // Record the start time
} else if (Driver.getYButton() && scoreElevatorPos == 3) {
scoreElevatorPos = 14; // Start the sequence for L4
stateStartTime = Timer.getFPGATimestamp(); // Record the start time
}


//If in L4 Position
else if (Driver.getXButton() && scoreElevatorPos == 4) {
  scoreElevatorPos = 15; // Start the sequence for L1
  stateStartTime = Timer.getFPGATimestamp(); // Record the start time
} else if (Driver.getAButton() && scoreElevatorPos == 4) {
scoreElevatorPos = 16; // Start the sequence For L2
stateStartTime = Timer.getFPGATimestamp(); // Record the start time
} else if (Driver.getBButton() && scoreElevatorPos == 4) {
scoreElevatorPos = 17; // Start the sequence for L3
stateStartTime = Timer.getFPGATimestamp(); // Record the start time
} 


/* State Machine
 * Elevator Motor running logic
 */
scoreElevatorPos = switch (scoreElevatorPos) {
  case 6 -> { // L1 to L2
      liftMotor.set(.9);
      if (waitFor(0.7)) {
        liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 2;
      }
      yield 6;
  }
  case 7 -> { // L1 to L3
      liftMotor.set(.9);
      if (waitFor(4.0)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 3;
      }
      yield 7;
  }
  case 8 -> { // L1 to L4
      liftMotor.set(.9);
      if (waitFor(5.5)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 4;
      }
      yield 8;
  }
  case 9 -> { // L2 to L1
      liftMotor.set(-.9);
      if (waitFor(0.7)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 1;
      }
      yield 9;
  }
  case 10 -> { // L2 to L3
      liftMotor.set(.9);
      if (waitFor(1.0)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 3;
      }
      yield 10;
  }
  case 11 -> { // L2 to L4
      liftMotor.set(.9);
      if (waitFor(2.5)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 4;
      }
      yield 11;
  }
  case 12 -> { // L3 to L1
      liftMotor.set(-.9);
      if (waitFor(4.0)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 1;
      }
      yield 12;
  }
  case 13 -> { // L3 to L2
      liftMotor.set(-.9);
      if (waitFor(1.0)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 2;
      }
      yield 13;
  }
  case 14 -> { // L3 to L4
      liftMotor.set(.9);
      if (waitFor(2.5)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 4;
      }
      yield 14;
  }
  case 15 -> { // L4 to L1
      liftMotor.set(-.9);
      if (waitFor(5.5)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 1;
      }
      yield 15;
  }
  case 16 -> { // L4 to L2
      liftMotor.set(-.9);
      if (waitFor(2.5)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 2;
      }
      yield 16;
  }
  case 17 -> { // L4 to L3
      liftMotor.set(-.9);
      if (waitFor(2.5)) {
          liftMotor.set(0);
          stateStartTime = Timer.getFPGATimestamp();
          yield 3;
      }
      yield 17;
  }
  default -> { // Default case to handle unexpected values
      System.err.println("Unexpected scoreElevatorPos value: " + scoreElevatorPos);
      yield scoreElevatorPos; // Keep the current value
  }
};










      //elevator subsystem
      // // Set the motor speed based on trigger values
      if (Operater.getRightBumperButtonPressed()) {
        // Move motor forward
        liftMotor.set(-.9); // Scale speed down to 50%
    } else if (Operater.getLeftBumperButtonPressed()) {
        // Move motor backward
        liftMotor.set(.97); // Scale speed down to 50%
    } else if (Operater.getRightBumperButtonReleased() || Operater.getLeftBumperButtonReleased()) {
        // Stop motor
        liftMotor.set(0);
   }


   // Motors that push the coral to the reef
   
    // Control the motors based on bumper inputs
    if (Operater.getRightTriggerAxis() > .1) {
        //      // Spin motors forward
        CoralMotorL.set(-.2); // 80% speed forward
        CoralMotorR.set(.2); // 80% speed forward

     } else if (Operater.getLeftTriggerAxis() > .1) {
        //      // Spin motors backward
        CoralMotorL.set(.2); // 80% speed forward
        CoralMotorR.set(-.2); // 80% speed forward
     } else if (Operater.getRightTriggerAxis() < .1 && Operater.getLeftTriggerAxis() < .1) {
        // Stop motors
        CoralMotorL.set(0);
        CoralMotorR.set(0);
     }

    
    
    if (Operater.getStartButtonPressed()) {
     //      // Spin motors forward
          CSource.set(-.5); // 80% speed forward
       }  else if (Operater.getStartButtonReleased()) { 
          // Stop motors
          CSource.set(0);
       }
      
    //    public void Red 2.auto{
    //     if(Driver.getBButtonPressed()) {
        
    //    if (toplimitSwitch.get() == false){
    //      BallMotorI.set(-.4);
    //      if (!inTimerRunning) {
    //        timer.reset();
    //        timer.start();
    //        inTimerRunning = true;
    //     }
    //  }else  if (toplimitSwitch.get() == true ) {
    //    if (inTimerRunning && timer.get() < 3.0) {
    //      BallMotorI.set(-.4);
    //    }else{
    //      BallMotorI.set(.0);
    //      timer.stop();
    //      inTimerRunning = false;
   
    //    }
    //  }
    
    /*
     *  
     * 
     */

    //    if (Driver.getRightBumperPressed()) {
    //      //      // Pivets the Claw forward
    //           BallMotorP.set(.5); // 
    //              // Pivets the Claw Backwards
    //        } else if (Driver.getLeftBumperPressed()) {
    //           BallMotorP.set(-.5); // 80% speed forward
    //        } else if (Driver.getRightBumperReleased() || Driver.getLeftBumperReleased()) {
    //           // Stop motors
    //         BallMotorP.set(0);

    //  }

    //  if (Driver.getRightTriggerAxis() > .1) {
    //    //      // Spin the claw wheel motors forwards
    //         BallMotorI.set(.5); // 80% speed forward
    //      } else if (Driver.getLeftTriggerAxis() > .1) {
    //    //      // Spin the claw wheel motors backwards
    //    BallMotorI.set(-.5); // 80% speed forward
    //      } else if (Driver.getRightTriggerAxis() < .1 && Driver.getLeftTriggerAxis() < .1) {
    //         // Stop motors
    //       BallMotorI.set(0);

   

     

   if(Driver.getAButtonPressed()){

    End = !End;
}  
if (End){
  
  Endsolenoid.set(Value.kForward);
 
} else {
  Endsolenoid.set(Value.kReverse);
}
  

  
  if(Driver.getYButtonPressed()){

    BallOut = !BallOut;
}  
if (BallOut){
  
  clawsolenoid.set(Value.kForward);
 
} else {
  clawsolenoid.set(Value.kReverse);
}




    // if(Operater.getPOV() == 0)
    // { Armsolenoid.set(Value.kForward);
    // } else if (Operater.getPOV() == 180)
    // {Armsolenoid.set(Value.kReverse);
    // }
  }

  

  @Override
  public void testInit()
  {
    // Cancels all running commands at the start of test mode.
    CommandScheduler.getInstance().cancelAll();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic()
  {
  }

  /**
   * This function is called once when the robot is first started up.
   */
  @Override
  public void simulationInit()
  {
  }

  /**
   * This function is called periodically whilst in simulation.
   */
  @Override
  public void simulationPeriodic()
  {
  }
  }
