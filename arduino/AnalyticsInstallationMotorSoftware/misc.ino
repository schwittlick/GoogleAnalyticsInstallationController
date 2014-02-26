int availableMemory() {
  int size = 2048; // Use 2048 with ATmega328
  byte *buf;

  while ((buf = (byte *) malloc(--size)) == NULL)
    ;

  free(buf);

  return size;
}

int freeRam () {
  extern int __heap_start, *__brkval;
  int v;
  return (int) &v - (__brkval == 0 ? (int) &__heap_start : (int) __brkval);
}

void setupSteppers() {
  for ( int i = 0; i < nrOfMotors; i++ ) {
    currentIndex[i] = 0;
  }

  steppers[0] = AccelStepper(1, 3, 2);
  steppers[0].setMaxSpeed(2000.0);
  steppers[0].setSpeed(2000.0);
  steppers[0].setAcceleration(1800.0);

  steppers[1] = AccelStepper(1, 5, 4);
  steppers[1].setMaxSpeed(2000.0);
  steppers[1].setSpeed(2000.0);
  steppers[1].setAcceleration(1800.0);
}

bool isEndOfInstruction( char character ) {
  if ( character == '\n' ) {
    return true;
  } else {
    return false;
  }
}

bool newSerialDataAvailable() {
  return ( Serial.available() > 0 );
}

void cleanUpTemoraryArray() {
  for (int i = 0; i < instructionLength; i++) {
    stringInputData[i] = transformValues(48);
  }
}
