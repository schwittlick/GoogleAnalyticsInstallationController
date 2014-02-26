// special library which enables the steppers to slowly accelerate and decelerate
#include <AccelStepper.h>

// one tube has an instructionlength of maximum 150. this is important for RAM issues
#define instructionLength 150
// the amount of connected motors
#define nrOfMotors 2
// after the instructions were compressed
#define finalInstructionLength 50

AccelStepper steppers[ nrOfMotors ];

// this is where the sent instructions are going to be saved temporarily
char stringInputData[ instructionLength ];
// the degree which represents a 180° turn
int degreeHalfTurn = 720;
// the degree by which one motor is rotated in order to adjust small differenced inbetween tubes
int adjustSpinDegree = 50;
// keeps track of the current instruction index of each motor
int currentIndex[ nrOfMotors ];
// stores the final instructions
int finalInstructions[ nrOfMotors ][ finalInstructionLength ];
int finalInstructionsLenths[ nrOfMotors ];
bool hasInstructions[ nrOfMotors ];

char newData = -1;
byte index = 0;

bool running = false;

/**
 *
*/
void setup() {
  Serial.begin( 9600 );

  setupSteppers();
}

/**
 *
*/
void loop() {
  readInput();

  if ( running ) {
    rotate();
  }

  for ( int i = 0; i < nrOfMotors; i++ ) {
    steppers[ i ].run();
  }
}

/**
 *
*/
void readInput() {

  if ( newSerialDataAvailable() ) {
    // if new serial data is available, its being all loaded into the array
    newData = Serial.read();
    if (!isEndOfInstruction( newData ) ) {
      stringInputData[index] = newData;
      index++;
    } else {
      // after all data from the serial port was loaded, analyze it. there are
      // special prefixes to identify different commands:
      // 'r' -> the instructions following this prefix are rotation commands for the motors
      // 'm' -> the instructions following this prefix specify the new motor rotation speed
      // 'd' -> the instructions following this prefix specify the degree of an 180° spin at the bottom element
      // 'a' -> the instructions following this prefix specify the acceleration speed of the motors
      // 's' -> the instruction after this prefix specifies the motor nr which should be adjusted by some degrees

      //Serial.print("ReadInput function...\nPrefix is: ");
      //Serial.println(stringInputData[0]);
      char prefix = stringInputData[0];
      Serial.println( prefix );
      if (isRotationInstruction( prefix ) ) {
      // Serial.println("Input data received.");
      opt( stringInputData );
      }

      else if (isMotorSpeedInstruction( prefix ) ) {
      // Serial.println("Motor speed command received");
      changeMotorSpeed(stringInputData);
      }

      else if (isRotationDegree( prefix )) {
      // Serial.println("Rotation degree command received");
      changeRotationDegree(stringInputData);
      }

      else if (isAcceleration( prefix )) {
      // Serial.println("acceleration command received");
      changeAcceleration(stringInputData);
      }

      else if (isDebugSpin( prefix )) {
      // Serial.println(stringInputData[1] - '0');
      doDebugSpin(stringInputData[1] - '0');
      }
      
      else {
        Serial.println("Couldn't process prefix. Failed.");
      }

      cleanUpTemoraryArray();

      index = 0;
    }
  }
}

void doDebugSpin(int motorNr) {

  if (steppers[motorNr].distanceToGo() == 0) {
    steppers[motorNr].moveTo(steppers[motorNr].currentPosition() + adjustSpinDegree);
  }
}

void opt( char input[] ) {
  Serial.println("OPT()");
  // temporary instructions
  int isolatedLength = 120;
  int isolatedInstructions[nrOfMotors][isolatedLength]; // [] - tube count []- instruction length

  // set all indizes in the array to 0 ( important, since there's random stuff allocated )
  for (int i = 0; i < nrOfMotors; i++) {
    for (int j = 0; j < isolatedLength; j++) {
      isolatedInstructions[i][j] = 0;
    }
  }

  // transform input chars to to isolated rotation values
  int tubeIndex = 0;
  int indexWithinTube = 0;
  bool finished = false;
  int lengths[nrOfMotors];
  for (int i = 1; i < 1000; i++) {
    if ( finished == false ) {
      if ( isRotationValue(stringInputData[i] ) ) {
        isolatedInstructions[tubeIndex][indexWithinTube] = transformValues( stringInputData[i] ); // -1 or 1
        Serial.print(isolatedInstructions[tubeIndex][indexWithinTube]);
        Serial.print(" ");
        indexWithinTube++;
      }
      else if (stringInputData[i] == 'r') {

        if (tubeIndex == 0) {
          lengths[tubeIndex] = i - 1;
        } else {
          lengths[tubeIndex] = i - 1 - lengths[tubeIndex - 1];
        }

        tubeIndex++;
        // i++;
        indexWithinTube = 0;
        Serial.println();


      }
      else if ( stringInputData[i] == 'q') {
        Serial.print("STOP at ");
        Serial.println(i);
        finished = true;
        lengths[tubeIndex] = i - 1 - lengths[tubeIndex - 1];
      }
      else {
        isolatedInstructions[tubeIndex][indexWithinTube] = transformValues(48); // 0
      }


    }
  }

  Serial.println("IsolatedInstructions:");
  for (int i = 0; i < nrOfMotors; i++) {
    for (int j = 0; j < lengths[i]; j++) {

      Serial.print(isolatedInstructions[i][j]);
      Serial.print(" ");

    }
    //Serial.print(count);
    Serial.println();
  }

  Serial.print("Available memory: ");
  Serial.println(availableMemory());
  Serial.print("Available memory: ");
  Serial.println(freeRam());
  //

  for (int j = 0; j < nrOfMotors; j++ ) {
    int inputLength = lengths[j];
    int outputLength = calculateOptimizedArrayLength(isolatedLength, isolatedInstructions[j]);
    finalInstructionsLenths[j] = outputLength;
    Serial.println("outputLength");
    Serial.println(outputLength);
    int currentDirection = isolatedInstructions[j][0];
    int currentCounter = 1;
    int optimizedIndex = 0;
    for (int i = 0; i < inputLength - 1; i++) {
      int c = isolatedInstructions[j][i];
      int c1 = isolatedInstructions[j][i + 1];
      if ( c == c1 ) {
        currentCounter++;
      }
      else {
        int optimized = currentCounter * currentDirection;
        finalInstructions[j][optimizedIndex] = optimized;
        //Serial.println("different");
        //Serial.println(finalInstructions[j][optimizedIndex]);
        optimizedIndex++;
        currentDirection *= -1;
        currentCounter = 1;
      }

      if (i == inputLength - 2) {
        int optimized = currentCounter * currentDirection;
        finalInstructions[j][optimizedIndex] = optimized;
        //Serial.println("i == inputLength - 2");
        //Serial.println(finalInstructions[j][optimizedIndex]);
        optimizedIndex++;
      }
    }
    running = true;
    hasInstructions[j] = true;
  }

  //
  Serial.println("finalInstructions:");
  for (int i = 0; i < nrOfMotors; i++) {
    for (int j = 0; j < finalInstructionsLenths[i] - 1; j++) {
      Serial.print(finalInstructions[i][j]);
      Serial.print(" ");

    }
    Serial.println();
  }
  Serial.println("finished OPT()");
}

int calculateOptimizedArrayLength(int length, int arr[]) {
  int returnLength = 0;
  for (int i = 0; i < length - 1; i++) {
    int c = arr[i];
    int c1 = arr[i + 1];
    if ( c == c1 ) {
    }
    else {
      returnLength++;
    }
    if (i == length - 2) {
      returnLength++;
    }
  }
  return returnLength;
}

void rotate() {

  bool allDone[] = {false, false};
  for ( int j = 0; j < nrOfMotors; j++ ) {
    if ( currentIndex[ j ] >= finalInstructionsLenths[ j ] - 1 ) {

      allDone[j] = true;
      //Serial.print("set ");
      //Serial.print(j);
      //Serial.println( "allDone[j] = true; ");

    }

  }

  if ( allDone[0] == true && allDone[1] == true ) {
    running = false;
    //Serial.println("running = false");
    Serial.write( "done processing instructions" );
    Serial.write( '\n' );
    for ( int j = 0; j < nrOfMotors; j++ ) {
      currentIndex[ j ] = 0;
      for ( int i = 0; i < finalInstructionsLenths[ j ] - 1; i++ ) {
        //finalInstructions[ j ][ i ] = transformValues( 48 );
      }
    }
  }



  for (int j = 0; j < nrOfMotors; j++) {
    if ( allDone[j] == false ) {
      if (steppers[j].distanceToGo() == 0) {
        Serial.print("rotating motor : ");
        Serial.println(j);
        int calc = finalInstructions[j][currentIndex[j]] * degreeHalfTurn;
        int moveToPos = steppers[j].currentPosition() - calc;
        //Serial.print(" by ");
        //Serial.print(finalInstructions[j][currentIndex[j]]);
        //Serial.print(" index was ");
        //Serial.println(currentIndex[j]);
        steppers[j].moveTo(moveToPos);
        currentIndex[j]++;
      }
    }
  }
}


