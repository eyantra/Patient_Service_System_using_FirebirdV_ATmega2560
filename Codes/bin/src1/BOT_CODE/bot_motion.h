/*
 * =====================================================================================
 *
 *       Filename:  bot_motion.h
 *
 *				 Date:  31st March, 2010
 *
 *        Version:  2.1
 *       Revision:  2.1
 *       Compiler:  gcc-avr
 *
 *        Authors:  Pritish Kamath, pritish.kamath@iitb.ac.in
 *						  Rohit Saraf	 , rohitsaraf@iitb.ac.in
 *						  Ashish Mathew , ashishmathew@iitb.ac.in
 *						  Vivek Madan	 , vivekmadan@iitb.ac.in
 *
 *        Company:  IIT Bombay
 *		  Copyright:  ERTS Lab, IIT Bombay
 *
 * =====================================================================================
 */

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#define SetBit(x,b) ((x)|=(b))
#define GetBit(x,b) ((x)&(b))
#define ResetBit(x,b) ((x)&=(~(b)))

#include <math.h> //included to support power function
#include "lcd.h"

#define FCPU 11059200ul //defined here to make sure that program works properly

#define W_THRESHOLD 0x0f
#define W_THRESHOLD_STOP 0x08
#define ROTATE_THRESHOLD 0x0f //0x41

#define LEFT_SENSOR 3
#define CENTER_SENSOR 2
#define RIGHT_SENSOR 1
#define FRONT_IR_SENSOR 6

#define CONT_BLACK 5

#define IDLE				1
#define PROCESSING		2
#define BLOCKED			0


void port_init();
void timer5_init();
void velocity(unsigned char, unsigned char);
void motors_delay();

static volatile unsigned char ShaftCountLeft = 0;
static volatile unsigned char ShaftCountRight = 0;
unsigned char ADC_Conversion(unsigned char);
unsigned char ADC_Value;
unsigned char flag = 0;
unsigned char Left_white_line = 0;
unsigned char Center_white_line = 0;
unsigned char Right_white_line = 0;
unsigned char Front_IR_Sensor=0;

static volatile char status = IDLE;

/**
 * Function to configure LCD port
 *all the LCD pin's direction set as output
 *all the LCD pins are set to logic 0 except PORTC 7
 */
void lcd_port_config (void)
{
 DDRC = DDRC | 0xF7; //all the LCD pin's direction set as output
 PORTC = PORTC & 0x80; // all the LCD pins are set to logic 0 except PORTC 7
}

/**
 * Left shaft encoder init.
 */
void left_position_encoder_interrupt_init(void)
{
	cli();
	SetBit(EICRB,_BV(ISC41)); //The falling edge between two samples of INTn generates an interrupt request.
	SetBit(EIMSK,_BV(INT4)); //INT4 enable
	sei();
}

/**
 * Right shaft encoder init.
 */
void right_position_encoder_interrupt_init(void)
{
	cli();
	SetBit(EICRB,_BV(ISC51)); //The falling edge between two samples of INTn generates an interrupt request.
	SetBit(EIMSK,_BV(INT5)); //INT5 enable
	sei();
}


/**
 * Interrupt handler for left shaft count change.
 */
ISR(INT4_vect)
{
	ShaftCountLeft++;
}


/**
 * Interrupt handler for right shaft count change.
 */
ISR(INT5_vect)
{
	ShaftCountRight++;
}

/**
 * Reset shaft counters.
 */
void reset_shaft_counters()
{
	ShaftCountLeft = 0;
	ShaftCountRight = 0;
}


/**
 * Set ADC pin configuration
 */
void adc_pin_config (void)
{
 DDRF = 0x00; 
 PORTF = 0x00;
 DDRK = 0x00;
 PORTK = 0x00;
}

/**
 * Function to configure ports to enable robot's motion
 */
void motion_pin_config (void) 
{
 DDRA = DDRA | 0x0F;
 PORTA = PORTA & 0xF0;
 DDRL = DDRL | 0x18;   //Setting PL3 and PL4 pins as output for PWM generation
 PORTL = PORTL | 0x18; //PL3 and PL4 pins are for velocity control using PWM.
}

/**
 * Function to Initialize PORTS
 */
void port_init()
{
	lcd_port_config();
	adc_pin_config();
	motion_pin_config();	
}

/**
* Timer 5 initialised in PWM mode for velocity control
* Prescale:64
* PWM 8bit fast, TOP=0x00FF
* Timer Frequency:674.988Hz
*/
void timer5_init()
{
	TCCR5B = 0x00;			//Stop
	TCNT5H = 0xFF;			//Counter higher 8-bit value to which OCR5xH value is compared with
	TCNT5L = 0x01;			//Counter lower 8-bit value to which OCR5xH value is compared with
	OCR5AH = 0x00;			//Output compare register high value for Left Motor
	OCR5AL = 0xFF;			//Output compare register low value for Left Motor
	OCR5BH = 0x00;			//Output compare register high value for Right Motor
	OCR5BL = 0xFF;			//Output compare register low value for Right Motor
	OCR5CH = 0x00;			//Output compare register high value for Motor C1
	OCR5CL = 0xFF;			//Output compare register low value for Motor C1
	TCCR5A = 0xA9;			/*{COM5A1=1, COM5A0=0; COM5B1=1, COM5B0=0; COM5C1=1 COM5C0=0}
 					  			For Overriding normal port functionalit to OCRnA outputs.
				  	  			{WGM51=0, WGM50=1} Along With WGM52 in TCCR5B for Selecting FAST PWM 8-bit Mode*/
	TCCR5B = 0x0B;			//WGM12=1; CS12=0, CS11=1, CS10=1 (Prescaler=64)
}

/**
 * Initialize the ADC module.
 */
void adc_init()
{
	ADCSRA = 0x00;
	ADCSRB = 0x00;		//MUX5 = 0
	ADMUX = 0x20;		//Vref=5V external --- ADLAR=1 --- MUX4:0 = 0000
	ACSR = 0x80;
	ADCSRA = 0x86;		//ADEN=1 --- ADIE=1 --- ADPS2:0 = 1 1 0
}

/**
 * ADC Conversion
 */
unsigned char ADC_Conversion(unsigned char Ch) 
{
	unsigned char a;
	if(Ch>7)
	{
		ADCSRB = 0x08;
	}
	Ch = Ch & 0x07;  			
	ADMUX= 0x20| Ch;	   		
	ADCSRA = ADCSRA | 0x40;		//Set start conversion bit
	while((ADCSRA&0x10)==0);	//Wait for conversion to complete
	a=ADCH;
	ADCSRA = ADCSRA|0x10; //clear ADIF (ADC Interrupt Flag) by writing 1 to it
	ADCSRB = 0x00;
	return a;
}

/**
 * Print Sensor Values At Desired Row And Coloumn Location on LCD
 */
void print_sensor(char row, char coloumn,unsigned char channel)
{
	
	ADC_Value = ADC_Conversion(channel);
	lcd_print(row, coloumn, ADC_Value, 3);
}

/**
 * Set velocity
 */
void velocity (unsigned char left_motor, unsigned char right_motor)
{
	OCR5AL = (unsigned char)left_motor;
	OCR5BL = (unsigned char)right_motor;
}

/**
 * Function used for setting motor's direction
 */
void motion_set (unsigned char Direction)
{
 unsigned char PortARestore = 0;

 Direction &= 0x0F; 					// removing upper nibbel for the protection
 PortARestore = PORTA; 				// reading the PORTA original status
 PortARestore &= 0xF0; 				// making lower direction nibbel to 0
 PortARestore |= Direction; 		// adding lower nibbel for forward command and restoring the PORTA status
 PORTA = PortARestore; 				// executing the command
}

/**
 * Set bot direction forward.
 */
void forward (void) 
{
  motion_set (0x06);
}

/**
 * Stop the bot
 */
void stop (void)
{
  motion_set (0x00);
}

/**
 * Calls the init methods for all required devices.
 */
void init_devices (void)
{
 	cli(); 
	port_init();
	adc_init();
	timer5_init();
	sei();   
}

/**
  Prints White line sensor values on the screen
*/
void print_sensor_data()
{
		print_sensor(1,1,3);	//Prints value of White Line Sensor1
		print_sensor(1,5,2);	//Prints Value of White Line Sensor2
		print_sensor(1,9,1);	//Prints Value of White Line Sensor3
}

/**
  Reads all relevant sensor values and stores it in appropriate global variables.
*/
void read_sensors()
{
	Left_white_line = ADC_Conversion(LEFT_SENSOR);	
	Center_white_line = ADC_Conversion(CENTER_SENSOR);	
	Right_white_line = ADC_Conversion(RIGHT_SENSOR);	
	Front_IR_Sensor = ADC_Conversion(FRONT_IR_SENSOR);
}

/**
  Switches the buzzer on, to signal a block on the path.
  Changes status of the bot to BLOCKED, which is sent to the server (which in turn sends an SMS to inform the guards to take appropriate actions.)
*/
void buzzer_on (void)
{
 unsigned char port_restore = 0;
 port_restore = PINC;
 port_restore = port_restore | 0x08;
 PORTC = port_restore;
 status = BLOCKED;
}

/**
  Switches the buzzer off.
*/
void buzzer_off (void)
{
 unsigned char port_restore = 0;
 port_restore = PINC;
 port_restore = port_restore & 0xF7;
 PORTC = port_restore;
 status = BLOCKED;
}

/**
  Turn right at an intersection.
*/
void turn_right(){
	buzzer_off();
	motion_set(0x0A);
	velocity(100,100);
	_delay_ms(1000);
	while(1){
		print_sensor_data();
		read_sensors();
		if(Center_white_line < W_THRESHOLD) break;
	}
	velocity(0,0);
}

/**
  Turn left at an intersection.
*/
void turn_left(){
	buzzer_off();
	motion_set(0x05);
	velocity(100,100);
	_delay_ms(1000);
	while(1){
		print_sensor_data();
		read_sensors();
		if(Center_white_line < W_THRESHOLD) break;
	}
	velocity(0,0);
}

/**
  Go forward by a certain specified number of steps.
*/
void go_distance(unsigned char x)
{
	reset_shaft_counters();
   forward();
	velocity(100,100);
	PORTJ = 0x00;
	while(1){
		read_sensors();
		print_sensor_data();
		if( Front_IR_Sensor<0xF0)
		{
			stop();
			buzzer_on();
		}
		else
		{
			forward();
			buzzer_off();
		}
		if((ShaftCountLeft + ShaftCountRight)*5 > x*10)
			break;
	}
	velocity(0,0);
}

/**
  Go forward upto the next intersection, while following a white line.
  Uses 7-fold scheme :
  (left, center, right) - Action
			(0,1,0)			- Go Forward.
			(1,1,0)			- Turn right (slightly)
			(1,0,0)			- Turn right (hard)
			(0,1,1)			- Turn left (slightly)
			(0,0,1)			- Turn left (hard)
			(1,1,1)			- Reached the intersection
			(0,0,0)			- Recovery mode. Move in the direction of the last sensor that was on white line
*/
void go_upto_next_cross(){
	char last_on = LEFT_SENSOR;
	char black_flag = 0;
	while(1)
	{
		PORTJ = PORTJ+1;
		if(Center_white_line<W_THRESHOLD_STOP && Left_white_line<W_THRESHOLD_STOP && Right_white_line<W_THRESHOLD_STOP ){
			PORTJ = 0xAA;
		 	break;
		}

		read_sensors();

		flag=0;
		print_sensor_data();
		
		if( Front_IR_Sensor<0xF0)
		{
			stop();
			buzzer_on();
		}
		//Sensor config : 010
		else if(Left_white_line > W_THRESHOLD && Center_white_line < W_THRESHOLD && Right_white_line > W_THRESHOLD)
		{
			forward();
			velocity(150,150);
			black_flag = 0;
			buzzer_off();
		}

		//Sensor config : 110
		else if(Left_white_line < W_THRESHOLD && Center_white_line < W_THRESHOLD && Right_white_line > W_THRESHOLD)
		{
			forward();
			velocity(120,150);
			black_flag = 0;
			buzzer_off();
		}
		
		//Sensor config : 100
		else if(Left_white_line < W_THRESHOLD && Center_white_line > W_THRESHOLD && Right_white_line > W_THRESHOLD)
		{
			PORTA = 0x05;
			velocity(50,130);
			last_on = LEFT_SENSOR;
			black_flag = 0;
			buzzer_off();
		}

		//Sensor config : 011
		else if(Left_white_line > W_THRESHOLD && Center_white_line < W_THRESHOLD && Right_white_line < W_THRESHOLD)
		{
			forward();
			velocity(150,120);
			black_flag = 0;
			buzzer_off();
		}

		//Sensor config : 001
		else if(Left_white_line > W_THRESHOLD && Center_white_line > W_THRESHOLD && Right_white_line < W_THRESHOLD)
		{
			PORTA = 0x0A;
			velocity(130,50);
			last_on = RIGHT_SENSOR;
			black_flag = 0;
			buzzer_off();
		}
		//Sensor config : 000
		else
		{
			buzzer_off();
			if(black_flag >= CONT_BLACK)  {
				if(last_on == LEFT_SENSOR)
					motion_set(0x05);
				else if(last_on == RIGHT_SENSOR)
					motion_set(0x0A);
				velocity(100,100);
				while(1){
					print_sensor_data();
					read_sensors();
					if(Center_white_line < W_THRESHOLD) break;
				}
			}
			black_flag = (black_flag < CONT_BLACK)?black_flag+1:CONT_BLACK;
			forward();
			velocity(0,0);
			PORTJ = 0x99;
		}
	}
	velocity(0,0);	
	go_distance(8);
}
