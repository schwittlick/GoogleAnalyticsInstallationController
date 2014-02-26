void changeRotationDegree(char* inputdata){
  char rotVal[4];
  memcpy(rotVal, &inputdata[1], 3);
  long rot = atol(rotVal);
  degreeHalfTurn = rot;
  Serial.println("Changed Rotation degre");
  Serial.println(rot);
}

void changeMotorSpeed(char* inputdata){
  char speedValue[4];
  memcpy(speedValue, &inputdata[1], 3);
  long speed = atol(speedValue);
  speed *= 10;
  
  for( int i=0; i<nrOfMotors; i++) {
    //steppers[i].setMaxSpeed(speed);
    //steppers[i].setSpeed(speed);
  }
  
  //stepper1.setMaxSpeed(speed);
  //stepper1.setSpeed(speed);
  Serial.println("Changed Motor Speed");
  Serial.println(speed);
}

void changeAcceleration(char* inputdata){
  char acceleration[4];
  memcpy(acceleration, &inputdata[1], 3);
  long acc = atol(acceleration);
  acc = acc * 100;
  for( int i=0; i<nrOfMotors; i++) {
    //steppers[i].setAcceleration(acc);
    //steppers[i].setAcceleration(acc);
  }
  //stepper1.setAcceleration(acc);
  Serial.println("Acceleration");
  Serial.println(acc);
}



bool isMotorSpeedInstruction(char prefix){
  bool isMotorSpeed = false;
  if(prefix == 'm'){
    isMotorSpeed = true;
  }

  return isMotorSpeed;
}

bool isRotationInstruction(char prefix){
  bool isInstruction = false;
  if(prefix == 'r'){
    isInstruction = true;
  }

  return isInstruction;
}

bool isRotationDegree(char prefix){
  bool isRotation = false;
  if(prefix == 'd'){
    isRotation = true;
  } 

  return isRotation;
}

bool isAcceleration(char prefix){
  bool isAcceleration = false;
  if(prefix == 'a'){
    isAcceleration = true;
  } 

  return isAcceleration;
}

bool isDebugSpin(char prefix) {
  bool isDebugSpin = false;
  if(prefix == 's'){
    isDebugSpin = true;
  }
  return isDebugSpin;
}

char possibleInstructionValues[3] = {
  '1', '2', 35};
  
boolean isRotationValue(char val){
  for(int i=0; i<3; i++){
    if(val == possibleInstructionValues[i]){
      return true;
    }
  }
  return false;
}

int transformValues(char val){
  int returnValue;

  // 49 == 1 in ASCII
  if(val == '1'){
    returnValue = -1;
    //Serial.write('1');
  } 
  // 50 == 2 in ASCII
  else if(val == '2'){
    returnValue = 1;
    //Serial.write('2');
  } 
  // 35 == # in ASCII
  else if(val == 35){
    returnValue = 35;
    //Serial.write('3');
  } 
  // all other values are 0 -> do nothing
  else {
    returnValue = 0;
    //Serial.write('4');
  }
  return returnValue;
}




