// Procesor using external 8 MHz. core clock is 48MHz
//#include "MKL46Z4.h"
#include "stdint.h"
#include "main.h"
#include "tim1_pwm.h"
#include "uart.h"


// For debug purpose: 
#define _DEBUG_PURPOSES_


volatile struct BLUETOOTH_COMM_DATA bluet_data ;
// For STROBE option:
volatile uint16_t strobe_cnt = 0, 				// the strobe counter - increment by SysTick
									strobe_cnt_on = 0, 			// if strobe_cnt <= strobe_cnt_on then leds are on, otherwise are off
									strobe_cnt_top = 0;			// the top value to strobe_cnt can count ( strobe period )

// For PULSE option:
volatile uint16_t pulse_cnt = 0;					// the pulse counter - decrement by SysTick

volatile uint16_t SysTickCounter = 0;


uint16_t temp_ ;

#ifdef _DEBUG_PURPOSES_
volatile uint16_t tmp = 2;
#endif 



/// SysTick Handler!
void SysTick_Handler(void) {
	SysTickCounter++ ;
	strobe_cnt++ ;					// for strobe option
	
	// for pulse option
	pulse_cnt-- ;
	if ( pulse_cnt == 0 ) {
		pulse_cnt = bluet_data.pulse_ms ;
		// Set notify for PULSE option in main program
		bluet_data.stat |= BLUETOOTH_STAT_PULSE_NOTIFY_MASK ;
	}
			
	#ifdef _DEBUG_PURPOSES_
	
	if( SysTickCounter > 10000 ) {
		SysTickCounter = tmp = 0 ;
	}
	
	#endif
	
}



// IRQ Handler for uart ( from bluetooth data ) 
void USART1_IRQHandler( void ) {
	
	uint8_t data_r;
	
	// Check interrupt source: ( need to be receive buffer not empty )
	if (USART1->ISR & USART_ISR_RXNE) {
		
		// copy data from receive buffer
		data_r = USART1->RDR ;
		
		if ( (bluet_data.iterator < (RGB_MAX_DATA_LEN + 3 )) && 
				 !(bluet_data.stat & BLUETOOTH_STAT_READY_MASK ) )
		{
			bluet_data.buff[ bluet_data.iterator++ ] = data_r ;
			// Wait for start byte:
			if ( bluet_data.buff[0] != RGB_START_BYTE )
				bluet_data.iterator = 0 ;
			// Wait for end byte if start byte is ok
			else if ( (data_r == RGB_STOP_BYTE ) &&
								(bluet_data.iterator == (RGB_MAX_DATA_LEN + 3 ))
							)
				bluet_data.stat |= BLUETOOTH_STAT_READY_MASK ;	// notify main program that data pack was just read
						
		} 
		#ifdef _DEBUG_PURPOSES_
		// For debug purposes: 
		else {
			
			tmp++ ;
			
		}
		
		// After each receive byte clear timer
		SysTickCounter = 0;
		#endif
		
	}
}


int main(void) {

	uint8_t i ; 
	
	
	// Init variables: 
	bluet_data.iterator = 0;
	bluet_data.stat = 0;
	
	// Init peripherials:
	init_pwm() ;
	init_uart() ;
	
	#ifdef _DEBUG_PURPOSES_
	// Led initialization: 
	//SIM->SCGC5 |= SIM_SCGC5_PORTD_MASK ;
	//SIM->SCGC5 |= SIM_SCGC5_PORTE_MASK ;
	//PORTD->PCR[LED_GREEN] |= PORT_PCR_MUX(1) ;
	//PORTE->PCR[LED_RED] |= PORT_PCR_MUX(1) ;
	//FPTD->PDDR |= (1 << LED_GREEN) ;
	//FPTE->PDDR |= (1 << LED_RED) ;
	//FPTD->PSOR |= (1 << LED_GREEN) ; 
	//FPTE->PSOR |= (1 << LED_RED) ; 
	#endif
	
	SysTick_Config( SystemCoreClock / 1000 ) ;
	
	while(1) {
		
			
		// Data analyzing
		if ( bluet_data.stat & (1 << 0) ) {
			
			// Copy data from buffer
			for( i = 0; i < RGB_MAX_DATA_LEN + 2 ; i++ )
					bluet_data.data[i] = bluet_data.buff[i] ;
			
			// Free buffer: 
			bluet_data.iterator = 0 ;
			bluet_data.stat &= ~BLUETOOTH_STAT_READY_MASK ; 
			
			
			// Check for 'Hello Message' 
			if ( bluet_data.data[1] & ( 1 << 7 )) {
				uart_send("OK", 2);
				bluet_data.data[1] &= ~( 1 << 7 ) ;
			} else {
				// Analiyze frame
				bluet_analyze() ;
			}
			
		}
		
		
			
		// Strobe option: 
	if( strobe_cnt_top != 0 )  {
		if( strobe_cnt <= strobe_cnt_on )
		{
				// Turn on RGB channels 
				rgb_channel_on();
		}		
		else if ( ( strobe_cnt > strobe_cnt_on ) && 
							( strobe_cnt <= strobe_cnt_top )
						)
		{
				// Turn off the RGB channels
				rgb_channel_off() ;
		}
		// Clear the counter
		else 
				strobe_cnt = 0;
		
	}
			
	
	// **********
	// Check what about PULSE option: 
	if( (bluet_data.data[5] & RGB_PULSE_OPT_MASK) && 
			(bluet_data.stat & BLUETOOTH_STAT_PULSE_NOTIFY_MASK )
		) 
	{
		
		// UP: 
		if( bluet_data.stat & BLUETOOTH_STAT_PULSE_DIR_MASK ) {
			
			if ( bluet_data.data[2] != 0 )	//red
				TIM1->CCR1 = pwm_tab[ ++bluet_data.r_pwm ] ;
			
			if ( bluet_data.data[3] != 0 )	//green
				TIM1->CCR2 = pwm_tab[ ++bluet_data.g_pwm ] ;
			
			if ( bluet_data.data[4] != 0 )	//blue
				TIM1->CCR3 = pwm_tab[ ++bluet_data.b_pwm ] ;
			
			
			bluet_data.min_cnt++ ; 
			// Check if the changes had enough: 
			if ( bluet_data.min_cnt >= bluet_data.min_rgb ) {
				
				bluet_data.min_cnt = 0 ;
				// DOWN counting:
				bluet_data.stat &= ~BLUETOOTH_STAT_PULSE_DIR_MASK ;
			}
			
		}
		// DOWN:
		else {
			
			if ( bluet_data.data[2] != 0 ) 	//red
				TIM1->CCR1 = pwm_tab[ --bluet_data.r_pwm ] ;
			
			
			if ( bluet_data.data[3] != 0 )	//green
				TIM1->CCR2 = pwm_tab[ --bluet_data.g_pwm ] ;
			
			if ( bluet_data.data[4] != 0 )	//blue
				TIM1->CCR3 = pwm_tab[ --bluet_data.b_pwm ] ;
			
			
			bluet_data.min_cnt++ ; 
			// Check if the changes had enough
			if ( bluet_data.min_cnt >= bluet_data.min_rgb ) {
				
				bluet_data.min_cnt = 0 ;
				// UP counting:
				bluet_data.stat |= BLUETOOTH_STAT_PULSE_DIR_MASK ;
			}
				
		}
		
		// Clear notify from SysTick
		bluet_data.stat &= ~BLUETOOTH_STAT_PULSE_NOTIFY_MASK ;
	}
		
		
	}
	
	return 0 ;
}


void bluet_analyze( void ) {
	
	uint16_t temp ; 
	
	//At first replay: "OK\n" if bit 3 in OPTION byte is 0 
	// 1 means to skip replay procedure
	if ( !( bluet_data.data[5] & ( 1 << RGB_REPLY_OPT_BIT )) )
		uart_send("OK", 2) ;
	
	// Copy rgb intensity value to duty registers: 
	TIM1->CCR1 = (uint16_t)bluet_data.data[2] ; 
	TIM1->CCR2 = (uint16_t)bluet_data.data[3] ; 
	TIM1->CCR3 = (uint16_t)bluet_data.data[4] ; 
	
	// *******************************************
	// 				Check additional options:
	// *******************************************
	
	// Check for strobe option:
	// Calculate a variable counter to value to serve strobe function
	if( (bluet_data.data[5] & RGB_STROBE_OPT_MASK) && 
			(bluet_data.data[ BLUETOOTH_STROBE_FREQ_BYTE ] != 0 ) && 
			( (bluet_data.data[ BLUETOOTH_INTENSITY_RED] != 0 ) || 
			(bluet_data.data[ BLUETOOTH_INTENSITY_GREEN] != 0 ) ||
			(bluet_data.data[ BLUETOOTH_INTENSITY_BLUE] != 0 ) )
			
		) {
		
		// Multiple by ten to calculate decimal value and by 0.5 to get real eg. 1Hz not 0.5Hz
		// 1000 is numer of ticks in SysTict to get 1s
		//strobe_cnt_top = (1000 * 5 ) / bluet_data.data[ BLUETOOTH_STROBE_FREQ_BYTE ] ;			// get the period/2 of user frequency
		
		// Multiple by ten to calculate decimal value
			strobe_cnt_top = (1000 * 10 ) / bluet_data.data[ BLUETOOTH_STROBE_FREQ_BYTE ] ;		// get period of user frequency with respect to SycTick
			
		// round up
		if ( (strobe_cnt_top%10) >= 5 )
			strobe_cnt_top = (strobe_cnt_top/10)+1;
		// round down
		else 
			strobe_cnt_top /= 10;
		
			
			// calculate led on time about x% of period: 
			if ( bluet_data.data[BLUETOOTH_STROBE_FREQ_BYTE] == 1 ) 
				strobe_cnt_on = strobe_cnt_top / 10 ;
			else if ( bluet_data.data[BLUETOOTH_STROBE_FREQ_BYTE] <= 7 ) 
				strobe_cnt_on = strobe_cnt_top / 5 ;
			else if ( bluet_data.data[BLUETOOTH_STROBE_FREQ_BYTE] <= 12 ) 
				strobe_cnt_on = strobe_cnt_top / 3 ;
			else 
				strobe_cnt_on = strobe_cnt_top / 2 ;
		
	}	else {
		// strobe option is disabled : 
		strobe_cnt_top = 0;
			strobe_cnt_on = 0;
		// Turn on RGB channels
		rgb_channel_on();
	}
	
	
	// Check what about PULSE option: 
	// If option is enabled 
	if( bluet_data.data[5] & RGB_PULSE_OPT_MASK )  {
		
		// Calculate time from bouetooth data: 
		temp = ((uint16_t)bluet_data.data[7]<<8) | (uint16_t)bluet_data.data[8] ;
		
		// Calculate the minimu value of intensity Red, Green, Blue
		bluet_data.min_rgb = get_min() ;
		bluet_data.min_cnt = 0;						// Clear the pulse option counter 

		// If is at least one intensity grether than 0
		if ( bluet_data.min_rgb != 0 ) {
			
			// FOR PWM LOG10 mode
			// Convert the mimimum RGB value to index, and replace it in variable
			bluet_data.min_rgb = get_index( bluet_data.min_rgb ) ;
			// Convert RGB values to index
			bluet_data.r_pwm = ( bluet_data.data[2] > 0 )?( get_index(bluet_data.data[2]) ) : 0 ;
			bluet_data.g_pwm = ( bluet_data.data[3] > 0 )?( get_index(bluet_data.data[3]) ) : 0 ;
			bluet_data.b_pwm = ( bluet_data.data[4] > 0 )?( get_index(bluet_data.data[4]) ) : 0 ;
			
			
			// Calcualte how many 1ms cycles wait to increment / decrement - in pulse mode
			bluet_data.pulse_ms = temp / (uint16_t)bluet_data.min_rgb ;
			
			
			// Init the pulse counter
			pulse_cnt = bluet_data.pulse_ms ;	
			// Clear notify from SysTick
			bluet_data.stat &= ~BLUETOOTH_STAT_PULSE_NOTIFY_MASK ;
			// Start from down counting
			bluet_data.stat &= ~BLUETOOTH_STAT_PULSE_DIR_MASK;
			
		} else {
			// If not, then disable pulse option:
			bluet_data.data[5] &= ~RGB_PULSE_OPT_MASK;
			
		}
		
	}
	
}

/** 
	Get the index in pwm look-up table for the minimum RGB value
	Return index with value grather or equal with minimu
*/
uint8_t get_index ( uint8_t _in ) {
	uint8_t i ; 
	
	for ( i = 0 ; i < PWM_TAB_SIZE; i++ ) {
		if ( _in > pwm_tab[i] )
			continue ;
		else 
			break ; // return i;
	}
	
	return i;
}


/**
	Return the minimum non-zero from R, G, B value. 
	If all are zeros return zeros
*/
uint8_t get_min (void) {
	uint8_t tab[4] ;
	int8_t i, j=3 ;

	tab[0] = bluet_data.data[2] ;
	tab[1] = bluet_data.data[3] ;
	tab[2] = bluet_data.data[4] ;
	
	// At first sort 
	while( --j ) {
		for( i = 0 ; i < 2; i++ ) {
				
			if( tab[i] > tab[i+1] ) {
				tab[3] = tab[i] ;
				tab[i] = tab[i+1] ;
				tab[i+1] = tab[3] ;
			}
					
		}
	}
	
	// Return non zero value
	for ( i = 0 ; i < 3 ; i++ ) {
		if ( tab[i] != 0 ) 
			return tab[i] ;
	}
	
	return 0 ;
}





/** 
	Chanels RGB in TPM0 will be turn on in PWM mode
*/
__INLINE void rgb_channel_on( void ) {
	TIM1->CCER |= TIM_CCER_CC1NE | TIM_CCER_CC2NE | TIM_CCER_CC3NE ;
}

/**
	Channel RGB in TPM0 will be turn off.
*/
__INLINE void rgb_channel_off( void ) {
	TIM1->CCER &= ~(TIM_CCER_CC1NE | TIM_CCER_CC2NE | TIM_CCER_CC3NE) ;
}
