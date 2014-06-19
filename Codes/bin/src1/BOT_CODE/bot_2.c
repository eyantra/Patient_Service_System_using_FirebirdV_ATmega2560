/*
 * =====================================================================================
 *
 *       Filename:  bot_2.c
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
#include "bot_motion.h"

#ifndef BIT_MACROS
#define BIT_MACROS
#define SetBit(x,b) ((x)|=(b))
#define GetBit(x,b) ((x)&(b))
#define ResetBit(x,b) ((x)&=(~(b)))
#endif

#define NOTHING			10
#define GO_UPTO_CROSS	2
#define TURN_RIGHT		0
#define TURN_LEFT			1
#define TURN_AROUND		3
#define POLLING			5

#define ID_MASK			0xE0
#define INST_MASK			0x1F 

#define MY_ID				0x20  //Specific for each bot.

static volatile char data = 0;
static volatile char instruction = 0;

static volatile ACTION = NOTHING;

/**
 USART0 initialization for Zigbee communication.
 desired baud rate:9600
 actual baud rate:9600 (0.0%)
 char size: 8 bit
 parity: Disabled
*/

void USART_Init(void)
{
  UCSR0B = 0x00; 
  UCSR0A = 0x00;
  UCSR0C = 0x06;
  UBRR0L = 0x47; 
  UBRR0H = 0x00; 
  UCSR0B = 0x98;
}

/**
 ISR for receive complete interrupt
 Replies back to the polling/server instruction
*/
SIGNAL(SIG_USART0_RECV)
{
	data = UDR0;
	_delay_ms(10);
	if(GetBit(data,ID_MASK) == MY_ID)
	{	
		PORTJ = 0xff;	
		instruction = GetBit(data,INST_MASK);
		if(instruction == POLLING){
			PORTJ = 5;	
			UDR0 = (MY_ID | status);
		}
		else if(instruction==GO_UPTO_CROSS)
		{
			PORTJ = 1;	
			status = PROCESSING;
			ACTION = GO_UPTO_CROSS;
		}
		else if(instruction==TURN_RIGHT)
		{
			PORTJ = 2;	
			status = PROCESSING;
			ACTION = TURN_RIGHT;
		}
		else if(instruction==TURN_LEFT)
		{
			PORTJ = 3;	
			status = PROCESSING;
			ACTION = TURN_LEFT;
		}
		else if(instruction==TURN_AROUND)
		{
			PORTJ = 4;
			status = PROCESSING;
			ACTION = TURN_AROUND;
		}
	}
}

/**
	Inits the Zigbee module
*/
void init_devices_1(){
	cli();
	USART_Init();
	sei();
}

/**
	Processes the requests given by the server in an infinite loop.
   Sets the status at appropriate times (which are later sent back to the server during polling).
*/
int main()
{
	init_devices_1();
	init_devices();
	
	lcd_set_4bit();
	lcd_init();
	DDRJ = 0xff;
	PORTJ = 0xf0;	

	left_position_encoder_interrupt_init();
	right_position_encoder_interrupt_init();

	unsigned char q = 0;
	for(q = 0;q<10;q++){
		Left_white_line = ADC_Conversion(3);	//Getting data of Left WL Sensor
		Center_white_line = ADC_Conversion(2);	//Getting data of Center WL Sensor
		Right_white_line = ADC_Conversion(1);	//Getting data of Right WL Sensor
	}

	while(1)
	{
		if(ACTION == GO_UPTO_CROSS)
		{
			status = PROCESSING;
			go_upto_next_cross();
			instruction = 0;
			status = IDLE;
			ACTION = NOTHING;
		}
		else if(ACTION == TURN_RIGHT)
		{
			status = PROCESSING;
			turn_right();
			instruction = 0;
			status = IDLE;
			ACTION = NOTHING;
		}
		else if(ACTION == TURN_LEFT)
		{
			status = PROCESSING;
			turn_left();
			instruction = 0;
			status = IDLE;
			ACTION = NOTHING;
		}
		else if(ACTION == TURN_AROUND)
		{
			status = PROCESSING;
			turn_left();
			instruction = 0;
			status = IDLE;
			ACTION = NOTHING;
		}
	}
	return 0;
}
