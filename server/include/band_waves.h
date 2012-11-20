#include "emokit.h"
#include <stdlib.h>

#define EPOC_HLF 8192
#define EPOCH_SIZE 128
#define EPOCH_AVERAGE 4
#define CROP_MAX -1
#define CROP_MIN 1
#define CROP_CENTER 0

typedef struct waves
{   
    //Channel history
    double channels[14][EPOCH_SIZE];
    //Channel fourier transform 
    double filtered_channels[14][EPOCH_SIZE];
    double epoch_values[14][EPOCH_AVERAGE+1];	//contains averages over 128 values + 1 avg over the other 4
    //double final_values[14]

    double coefficients_alpha[2][8];
    double coefficients_beta[2][8];
    double coefficients_low_beta[2][8];
    
};

struct waves* make_new_waves();
void compute_band_waves(struct waves*, struct emokit_frame*);
void butterworth_beta_waves(struct waves*);
void butterworth_alpha_waves(struct waves*);
void butterworth_minbeta_waves(struct waves*);
void process_new_frame(struct waves*, struct emokit_frame*);
void average_of_squares(struct waves*);
void crop(struct waves*);
void center(struct waves*);
