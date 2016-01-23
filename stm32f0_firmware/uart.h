#ifndef _UART2_H_
#define _UART2_H_

#include "stdint.h"

// STM32F051R8 have AF0 on port B for USART1 RX and TX
#define UART2_RX_PIN 7
#define UART2_TX_PIN 6

// Define BAUDRATE - values 
// For fCK = 48 MHz and oversampling = 16
#define BAUD9600 0x1388					
#define BAUD19200 0x9C4			
#define BAUD57600 0x341		
#define BAUD38400	0x4E2		
#define BAUD115200 0x1A1	

int init_uart(void);

void uart_send( char *_data, uint8_t _size ) ;


#endif
