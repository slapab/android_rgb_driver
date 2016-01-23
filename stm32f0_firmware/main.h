#ifndef _MAIN_H
#define _MAIN_H

#include "stm32f0xx.h"
#include "stdint.h"


// bluetooth - rgb led driver data:
#define RGB_START_BYTE 0x53	// 'S'
#define RGB_STOP_BYTE 0x0A 	// '\n'
#define RGB_MAX_DATA_LEN 7	// this value is specified as standard

// bluetooth - addtional options - macros for bit names
#define RGB_STROBE_OPT_BIT 1					/// If this bit is 1, then strobe option is on
#define RGB_STROBE_OPT_MASK (1 << RGB_STROBE_OPT_BIT)
#define RGB_PULSE_OPT_BIT 0						/// If this bit is 1, then pulse option is on
#define RGB_PULSE_OPT_MASK (1 << RGB_PULSE_OPT_BIT)
#define RGB_REPLY_OPT_BIT 3						/// If this bit is 1, then do not send replay
#define RGB_REPLY_OPT_MASK (1 << RGB_REPLY_OPT_BIT)

// PWM values in log scale - look-up tables:
//#define PWM_TAB_SIZE 73
//const uint8_t pwm_tab[PWM_TAB_SIZE] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 29, 31, 32, 34, 35, 37, 39, 41, 43, 45, 47, 50, 52, 55, 58, 61, 64, 67, 70, 74, 78, 82, 86, 90, 95, 100, 105, 110, 116, 122, 128, 135, 142, 149, 157, 165, 174, 183, 192, 203, 213, 224, 236, 249, 255 } ;

#define PWM_TAB_SIZE 66
const uint8_t pwm_tab[PWM_TAB_SIZE] = {0, 0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 61, 67, 74, 78, 82, 86, 90, 95, 100, 105, 110, 116, 122, 128, 135, 142, 149, 157, 165, 174, 183, 192, 203, 213, 224, 236, 249, 255} ;

//RGB_MAX_DATA_LEN bytes + 1 start byte + 1 stop byte + 1 data_length byte
struct BLUETOOTH_COMM_DATA {
	
		uint8_t buff[RGB_MAX_DATA_LEN + 3] ;	// For interrupt
		uint8_t data[RGB_MAX_DATA_LEN + 3] ;	// For analizing
		uint8_t iterator ;					// keep how many bytes was read 
		uint8_t stat ;							// bit 0 : if 1 byte was just read
	
	// For PULSE OPTION : 
		uint16_t pulse_ms ; 				// how many 1ms's wait to decrement / or increment value - pulse mode - the top value
		uint8_t min_rgb ;						// minimu from R, G, B value
		uint8_t min_cnt ;						// count how many times was add / subtract by 1
	
		// For log10 pwm - contain index to pwm_tab that represent values from R, G, B bytes in data[]
		uint8_t r_pwm ;
		uint8_t g_pwm ;
		uint8_t b_pwm ;
	
} ;

/**
	Defines to BLUETOOTH_COMM_DATA struct
*/

// if this bit is 1 that whale packet ( 10 bytes ) was read and the packet is ready to parsing
#define BLUETOOTH_STAT_READY_BIT 0
#define BLUETOOTH_STAT_READY_MASK (1 << BLUETOOTH_STAT_READY_BIT)


// 
#define BLUETOOTH_STAT_STROBE_TOGG_BIT 7
#define BLUETOOTH_STAT_STROBE_TOGG_MASK (1 << BLUETOOTH_STAT_STROBE_TOGG_BIT)

// this is for pulse direction ( up or down counting - leds intensity )
#define BLUETOOTH_STAT_PULSE_DIR_BIT 6
#define BLUETOOTH_STAT_PULSE_DIR_MASK (1 << BLUETOOTH_STAT_PULSE_DIR_BIT)


#define BLUETOOTH_STAT_PULSE_NOTIFY_BIT 5
#define BLUETOOTH_STAT_PULSE_NOTIFY_MASK (1 << BLUETOOTH_STAT_PULSE_NOTIFY_BIT)


#define BLUETOOTH_STROBE_FREQ_BYTE 6
#define BLUETOOTH_INTENSITY_RED 2
#define BLUETOOTH_INTENSITY_GREEN 3
#define BLUETOOTH_INTENSITY_BLUE 4

void bluet_analyze( void ) ;
__INLINE void rgb_channel_on( void ) ;
__INLINE void rgb_channel_off( void ) ;

uint8_t get_min (void) ;

uint8_t get_index ( uint8_t ) ;

#endif
