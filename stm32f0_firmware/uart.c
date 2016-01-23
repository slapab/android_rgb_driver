#include "stm32f0xx.h"
#include "uart.h"

int init_uart(void){
	
	// Turn on clock gating for USART1:
	RCC->APB2ENR |= RCC_APB2ENR_USART1EN ;
	// Turn on clock gating for PORT TX and RX pins ( PORTB, 6 and 7 pins ) STM32F051R8 
	RCC->AHBENR |= RCC_AHBENR_GPIOBEN ;
	
	// Select alternative mode for pins:
	// The pins  6 and 7 at PORTB have AF0 as default (USART1 TX and RX)
	GPIOB->MODER |= (GPIO_MODER_MODER6_1);
	GPIOB->MODER |= (GPIO_MODER_MODER7_1);
	
	
	
	// ***
	//	For now I don't set any changes at push-pull/open-drain and do not turn on pull-up/down
	//  and I do not change speed ( is low speed ) 
	
	
	// Set baud rate ( for precalcualated values check uart.h )
	// oversampling by 16 only: 
	
	USART1->BRR = BAUD19200;
	
	// 8bit data, 1 stop bit, non parity check
	
	// Set interrupt after receive byte, and turn on transmiter and reciver an whole module USART1.
	USART1->CR1 |= USART_CR1_TE | USART_CR1_RE | USART_CR1_UE | USART_CR1_RXNEIE ; 	
	
	// Set NVIC
	NVIC_ClearPendingIRQ(USART1_IRQn);
	NVIC_EnableIRQ(USART1_IRQn);
	NVIC_SetPriority(USART1_IRQn,1);
	
	return 0;
}


// Send data to uart
void uart_send( char *_data, uint8_t _size ) {
	
	uint8_t i ;
	for( i = 0; i < _size ; ++i ) {

		// Check if transmitter buffer is empty
		while( !(USART1->ISR & USART_ISR_TXE) );
		
		// Send data to transmit buffer
		USART1->TDR = _data[i] ;
	}
	
	// And send new line character:
	while( !(USART1->ISR & USART_ISR_TXE) );
	USART1->TDR = '\n' ;
	
	
}




