/*
 * =====================================================================================
 *
 *       Filename:  IR.c
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

#ifndef BIT_MACROS
#define BIT_MACROS
#define SetBit(x,b) ((x)|=(b))
#define GetBit(x,b) ((x)&(b))
#define ResetBit(x,b) ((x)&=(~(b)))
#define Bit(x) (1<<(x))
#endif

#define ID_MASK			0xE0
#define INST_MASK			0x1F 

#define MY_ID				0x80  //Specific for patient polling bot.

static volatile unsigned char PATIENT_QUEUE[10]; 
static volatile unsigned char PATIENT_QUEUE_CURR_PTR = 0;
static volatile unsigned char PATIENT_QUEUE_TAIL_PTR = 0;
static volatile unsigned char PATIENT_QUEUE_SIZE = 0;

static volatile char data = 0;

static volatile unsigned char patient = 0x07;
#define CODESIZE 5
volatile unsigned char Pat[6][CODESIZE];
static volatile unsigned char got_intr = 0;
volatile unsigned char code[CODESIZE];

/**
  Number of common bits in given two bytes.
*/
unsigned char num_bits_matched(unsigned char a, unsigned char b)
{
	char i;
	unsigned char num = 0;
	for(i=0; i<8; i++)
		if(GetBit(a,Bit(i)) == GetBit(b,Bit(i))) num++;
	return num;
}

/**
  Finds the patient_id for a specific IR code.
*/
unsigned char patient_id(unsigned char code1[])
{
	unsigned char i, j;
	unsigned char room = 0, max_count = 0, curr_count;
	for(i=0; i<6; i++)
	{
		curr_count = 0;
		for(j=0; j<CODESIZE; j++)
			{
				curr_count += num_bits_matched(code1[j],Pat[i][j]);
			}
		if(max_count <= curr_count)
			{
				room = i;
				max_count = curr_count;
			}
	}
	PORTJ = max_count;
	if(max_count > CODESIZE*6)
		return room+1;
	else
		return 0x07;
}

/**
  Initialize all ports
*/
void init_ports()
{
	DDRA = 0x0F;
	PORTA = 0x00;
	DDRL = 0xff;
	PORTL = 0x00;
	PORTJ = 0x00;
	DDRJ  = 0xFF;
	PORTE = 0x00;
	DDRE  = 0x00;
}

/**
 USART0 initialization for Zigbee communication.
 desired baud rate:9600
 actual baud rate:9600 (0.0%)
 char size: 8 bit
 parity: Disabled
*/
void USART_Init(void)
{
  UCSR0B = 0x00; //disable while setting baud rate
  UCSR0A = 0x00;
  UCSR0C = 0x06;
  UBRR0L = 0x47; //set baud rate lo
  UBRR0H = 0x00; //set baud rate hi
  UCSR0B = 0x98;
}

/**
  ISR handler for Zigbee receive complete.
*/
ISR(SIG_USART0_RECV)
{
	data = UDR0;
	_delay_ms(100);
	if(GetBit(data,ID_MASK) == MY_ID)
	{
		if(patient == 0x07) UDR0 = 0x7f;			
		else UDR0 = ((patient+1)<<5);
		patient = 0x07;
	}
}

/**
  Function to read IR message from the detector
  Stores the 'IR-code' received in the code array (global).

  The code received is 'Protocol Independent'.
  Although the TV remote being used is RC-5 based, it does not assume any specific protocol, as long as the frequency of the remote matches that of the TSOP sensor.
*/
void IR_Get_Input_Vector (void)
{
	while((PINE & 0x80) == 0x80);
	_delay_us(7400);
	unsigned char Pulse_counter=0, addr = 0;

	while(Pulse_counter < 5)
	{
		_delay_us(1800);
		Pulse_counter++;
		if((PINE & 0x80) == 0x80)
		{
			addr = addr & ~(1 << (Pulse_counter-1));
		}
		else
		{
			addr = addr | (1 << (Pulse_counter-1));
		}
	}

	unsigned char last = 0;
	unsigned char bitarray[CODESIZE];
	unsigned char cc = 0;
	while(cc < CODESIZE*8){
		if(GetBit(PINE,0x80)) SetBit(bitarray[cc/8],Bit(cc%8));
		else ResetBit(bitarray[cc/8],Bit(cc%8));
		last = PINE & 0x80;
		_delay_us(300);
		cc++;
	}
	
	char j;	
	for(j=0;j<CODESIZE;j++){
			code[j] = bitarray[j];
	}
		
	char i;
	for(i=0; i<CODESIZE; i++)
	{
		PORTJ = bitarray[i];
		for(j=0; j<5; j++)
			_delay_ms(10);
	}
}

/**
  Returns the patient_ID of the last request that was received.
*/
unsigned char IR_read (){
	return patient_id(code);
}

/**
  Learns and remembers the patterns of TV remote buttons 1, 2, ... , 6.
*/
void learn(){
	char i,j;	
	for(i=0;i<6;i++){
		//asking for value i.		
		PORTJ = i+1;
		IR_Get_Input_Vector();
		for(j=0;j<5;j++){
			Pat[i][j] = code[j];
		}
		_delay_ms(1000);
	}
	PORTJ |= 0x40;
}

/**
  Initialise all the devices
*/
void IR_init_devices(){
	cli();
	init_ports();
	USART_Init();
	sei();
}

/**
  Main Function
  Performs the job of receiving TV remote signals and stores it in the variable 'patient',
  which is later communicated to the server on request.
*/
int main(void)
{
	IR_init_devices();
	learn();
	while(1){
		IR_Get_Input_Vector();
		_delay_ms(500);
		patient = patient_id(code);
		PORTJ = patient;
	}
	return 0;
}
