#include <Wire.h>
#include <ESP8266WiFi.h> 
#include <WiFiClient.h>  
 extern "C" {
#include "user_interface.h"
}
char buf[10];
 double Accel,Gyro;
 #define      LEDR      D0        // WIFI Module LED
#define KEY1  D2
  #define     LED0      2  
  #define     LED1      D1          // Connectivity With Client #1
 // #define     LED2      D2          // Connectivity With Client #2 
  #define     MAXSC     6 
  char*       TKDssid;              // SERVER WIFI NAME
  char*       TKDpassword;          // SERVER PASSWORD
WiFiServer  TKDServer(8080);      // THE SERVER AND THE PORT NUMBER
  WiFiClient  TKDClient[MAXSC];     // THE SERVER CLIENTS (Devices)
// MPU6050 Slave Device Address
const uint8_t MPU6050SlaveAddress = 0x68;

// Select SDA and SCL pins for I2C communication 
const uint8_t scl = D6;
const uint8_t sda = D7;

// sensitivity scale factor respective to full scale setting provided in datasheet 
const uint16_t AccelScaleFactor = 16384;
const uint16_t GyroScaleFactor = 131;

// MPU6050 few configuration register addresses
const uint8_t MPU6050_REGISTER_SMPLRT_DIV   =  0x19;
const uint8_t MPU6050_REGISTER_USER_CTRL    =  0x6A;
const uint8_t MPU6050_REGISTER_PWR_MGMT_1   =  0x6B;
const uint8_t MPU6050_REGISTER_PWR_MGMT_2   =  0x6C;
const uint8_t MPU6050_REGISTER_CONFIG       =  0x1A;
const uint8_t MPU6050_REGISTER_GYRO_CONFIG  =  0x1B;
const uint8_t MPU6050_REGISTER_ACCEL_CONFIG =  0x1C;
const uint8_t MPU6050_REGISTER_FIFO_EN      =  0x23;
const uint8_t MPU6050_REGISTER_INT_ENABLE   =  0x38;
const uint8_t MPU6050_REGISTER_ACCEL_XOUT_H =  0x3B;
const uint8_t MPU6050_REGISTER_SIGNAL_PATH_RESET  = 0x68;

int16_t AccelX, AccelY, AccelZ, Temperature, GyroX, GyroY, GyroZ;

void setup() {
     pinMode(LEDR, OUTPUT);   
     pinMode(LED0, OUTPUT);   
     pinMode(KEY1, INPUT);   
     digitalWrite(LED0, HIGH);
     digitalWrite(LEDR, HIGH);
  Serial.begin(9600);
  SetWifi("TAKEONE", "12345678");
  Wire.begin(sda, scl);
  MPU6050_Init();
}

void loop() {
  double Ax, Ay, Az, T, Gx, Gy, Gz;
 
   AvailableClients();
    // Checking For Available Client Messages
    AvailableMessage();
  Read_RawValue(MPU6050SlaveAddress, MPU6050_REGISTER_ACCEL_XOUT_H);
  
  //divide each with their sensitivity scale factor
  Ax = (double)AccelX/AccelScaleFactor;
  Ay = (double)AccelY/AccelScaleFactor;
  Az = (double)AccelZ/AccelScaleFactor;
  T = (double)Temperature/340+36.53; //temperature formula
  Gx = (double)GyroX/GyroScaleFactor;
  Gy = (double)GyroY/GyroScaleFactor;
  Gz = (double)GyroZ/GyroScaleFactor;

  /*Serial.print("Ax: "); Serial.print(Ax);
  Serial.print(" Ay: "); Serial.print(Ay);
  Serial.print(" Az: "); Serial.print(Az);
  Serial.print(" T: "); Serial.print(T);
  Serial.print(" Gx: "); Serial.print(Gx);
  Serial.print(" Gy: "); Serial.print(Gy);
  Serial.print(" Gz: "); Serial.println(Gz);
  */
  Accel=pow(Ax,2)+pow(Ay,2)+pow(Az,2);
  Accel=pow(Accel,0.5);
  Gyro=pow(Gx,2)+pow(Gy,2)+pow(Gz,2);
  Gyro=pow(Gyro,0.5);
  Serial.print("Accel: "); Serial.println(Accel);
  Serial.print("Gyro: "); Serial.println(Gyro);
  if(Gyro>200)
  {
     digitalWrite(LEDR, LOW);
      Serial.print("Fall Detected");
  }
  else
     digitalWrite(LEDR, HIGH);
  delay(500);
}

void I2C_Write(uint8_t deviceAddress, uint8_t regAddress, uint8_t data){
  Wire.beginTransmission(deviceAddress);
  Wire.write(regAddress);
  Wire.write(data);
  Wire.endTransmission();
}

// read all 14 register
void Read_RawValue(uint8_t deviceAddress, uint8_t regAddress){
  Wire.beginTransmission(deviceAddress);
  Wire.write(regAddress);
  Wire.endTransmission();
  Wire.requestFrom(deviceAddress, (uint8_t)14);
  AccelX = (((int16_t)Wire.read()<<8) | Wire.read());
  AccelY = (((int16_t)Wire.read()<<8) | Wire.read());
  AccelZ = (((int16_t)Wire.read()<<8) | Wire.read());
  Temperature = (((int16_t)Wire.read()<<8) | Wire.read());
  GyroX = (((int16_t)Wire.read()<<8) | Wire.read());
  GyroY = (((int16_t)Wire.read()<<8) | Wire.read());
  GyroZ = (((int16_t)Wire.read()<<8) | Wire.read());
}

//configure MPU6050
void MPU6050_Init(){
  delay(150);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_SMPLRT_DIV, 0x07);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_PWR_MGMT_1, 0x01);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_PWR_MGMT_2, 0x00);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_CONFIG, 0x00);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_GYRO_CONFIG, 0x00);//set +/-250 degree/second full scale
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_ACCEL_CONFIG, 0x00);// set +/- 2g full scale
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_FIFO_EN, 0x00);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_INT_ENABLE, 0x01);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_SIGNAL_PATH_RESET, 0x00);
  I2C_Write(MPU6050SlaveAddress, MPU6050_REGISTER_USER_CTRL, 0x00);
}
 void SetWifi(char* Name, char* Password)
  {
    // Stop Any Previous WIFI
    WiFi.disconnect();

    // Setting The Wifi Mode
    WiFi.mode(WIFI_AP_STA);
    Serial.println("WIFI Mode : AccessPoint Station");
    
    // Setting The AccessPoint Name & Password
    TKDssid      = Name;
    TKDpassword  = Password;
    
    // Starting The Access Point
    WiFi.softAP(TKDssid, TKDpassword);
    Serial.println("WIFI < " + String(TKDssid) + " > ... Started");
    
    // Wait For Few Seconds
    delay(1000);
    
    // Getting Server IP
    IPAddress IP = WiFi.softAPIP();
    
    // Printing The Server IP Address
    Serial.print("AccessPoint IP : ");
    Serial.println(IP);

    // Printing MAC Address
    Serial.print("AccessPoint MC : ");
    Serial.println(String(WiFi.softAPmacAddress()));

    // Starting Server
    TKDServer.begin();
    TKDServer.setNoDelay(true);
    Serial.println("Server Started");
  }

//====================================================================================

  void AvailableClients()
  {   
    if (TKDServer.hasClient())
    {
      // Read LED0 Switch To Low If High.
      //if(digitalRead(LED0) == HIGH) digitalWrite(LED0, LOW);
      
      // Light Up LED1
    //  digitalWrite(LED1, HIGH);
      
      for(uint8_t i = 0; i < MAXSC; i++)
      {
        //find free/disconnected spot
        if (!TKDClient[i] || !TKDClient[i].connected())
        {
          // Checks If Previously The Client Is Taken
          if(TKDClient[i])
          {
            TKDClient[i].stop();
          }

          // Checks If Clients Connected To The Server
          if(TKDClient[i] = TKDServer.available())
          {
            Serial.println("New Client: " + String(i));
          }

          // Continue Scanning
          continue;
        }
      }
      
      //no free/disconnected spot so reject
      WiFiClient TKDClient = TKDServer.available();
      TKDClient.stop();
    }
    /*else
    {
      // This LED Blinks If No Clients Where Available
      digitalWrite(LED0, HIGH);
      delay(250);
      digitalWrite(LED0, LOW);
      delay(250);
    }
    */
  }

//====================================================================================

  void AvailableMessage()
  {
    //check clients for data
    /*if(TKDClient[0].available())
    {
          digitalWrite(LED0, HIGH);
          while(TKDClient[0].available())
          {
            String Message = TKDClient[0].readStringUntil('\r');
            TKDClient[0].flush();
            Serial.println(Message);//"Client No " + String(i) + " - " +
          }
          digitalWrite(LED0, LOW);
    }
    */
    
    for(uint8_t i = 0; i < MAXSC; i++)
    {
        //TKDClient[i].write('*');
        
       if(Gyro>200 || !((digitalRead(KEY1)) ))
          TKDClient[i].write("Fall Detected ");
      // else
       //    TKDClient[i].write("No Fall");
       //TKDClient[i].write('#');
           
      if (TKDClient[i] && TKDClient[i].connected() && TKDClient[i].available())
      {
          
          digitalWrite(LED0, LOW);
          while(TKDClient[i].available())
          {
            String Message = TKDClient[i].readStringUntil('\n');
            
            //TKDClient[i].flush();
            digitalWrite(LED0, HIGH);    
            Serial.print(Message);//"Client No " + String(i) + " - " +
          }
          
      }
    }
  }

