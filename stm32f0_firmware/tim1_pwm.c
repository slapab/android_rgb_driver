#include "stm32f0xx.h"
#include "tim1_pwm.h"


void init_pwm( void ) {
	
	// TPM clock gating on: 
	
	// Select clock source as SYSCLK ( 48 MHz )
	RCC->CFGR3 |= RCC_CFGR3_USART1SW_0 ;
	// Turning clock gating for TIM1:
	RCC->APB2ENR |= RCC_APB2ENR_TIM1EN ;
	
	
	// used pin's port  clock gating on: 
	// I use PB15, PB14, PB13 as output pwm
	RCC->AHBENR |= RCC_AHBENR_GPIOBEN ;
	
	// Select alternative mode for pins:
	GPIOB->MODER |= GPIO_MODER_MODER15_1;
	GPIOB->MODER |= GPIO_MODER_MODER14_1;
	GPIOB->MODER |= GPIO_MODER_MODER13_1;
	
	// Set AF2 as TIM1_PWM for pins: 
	GPIOB->AFR[1] |= GPIO_AFRH_AFR15 & 0x20000000 ;
	GPIOB->AFR[1] |= GPIO_AFRH_AFR14 & 0x02000000 ;
	GPIOB->AFR[1] |= GPIO_AFRH_AFR13 & 0x00200000 ;
	
	
	// Set OUTPUT SPEED ( medium speed )
	GPIOB->OSPEEDR |= GPIO_OSPEEDR_OSPEEDR13_0 ;
	GPIOB->OSPEEDR |= GPIO_OSPEEDR_OSPEEDR14_0 ;
	GPIOB->OSPEEDR |= GPIO_OSPEEDR_OSPEEDR15_0;
	

	// Set PWM mode for each channel
	TIM1->CCMR1 |= TIM_CCMR1_OC1M_2 | TIM_CCMR1_OC1M_1 | TIM_CCMR1_OC1PE ;	// PWM mode on CH1
	TIM1->CCMR1 |= TIM_CCMR1_OC2M_2 | TIM_CCMR1_OC2M_1 | TIM_CCMR1_OC2PE ;	// ... on CH2 
	TIM1->CCMR2 |= TIM_CCMR2_OC3M_2 | TIM_CCMR2_OC3M_1 | TIM_CCMR2_OC3PE ;	// ... on CH3
	
	// and enable NCHs channels
	TIM1->CCER |= TIM_CCER_CC1NE | TIM_CCER_CC2NE | TIM_CCER_CC3NE ;
	
	// Set active all enabled outputs : 
	TIM1->BDTR |= TIM_BDTR_MOE | TIM_BDTR_AOE ;
	
	// Set pwm period, I assume 8bit resolution for pwm
	TIM1->ARR = 0x00FF ;
	
	// Set the initial duty: 
	TIM1->CCR1 |= 20 ;
	TIM1->CCR2 |= 50 ; 
	TIM1->CCR3 |= 190 ;
	
	// Set prescaler
	// I assume 8 bit pwm resolution. 48MHz / 256 / 70 Hz 
	TIM1->PSC = 10 ;
	
	// Update values to registers before start timer
	TIM1->EGR |= TIM_EGR_UG ;
	
	// set auto-reload preload ( buffered ) and turn on counter
	TIM1->CR1 |= TIM_CR1_ARPE | TIM_CR1_CEN ;
	
	
}
