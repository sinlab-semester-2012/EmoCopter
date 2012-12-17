/* Copyright (c) 2010, Daeken and Skadge
 * Copyright (c) 2011-2012, Kyle Machulis <kyle@nonpolynomial.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

#include <mcrypt.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "emokit/emokit.h"

const unsigned char F3_MASK[14] = {10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7}; 						//8-21?
const unsigned char FC6_MASK[14] = {214, 215, 200, 201, 202, 203, 204, 205, 206, 207, 192, 193, 194, 195};	//204-217?
const unsigned char P7_MASK[14] = {84, 85, 86, 87, 72, 73, 74, 75, 76, 77, 78, 79, 64, 65};					//78-91?
const unsigned char T8_MASK[14] = {160, 161, 162, 163, 164, 165, 166, 167, 152, 153, 154, 155, 156, 157};	//162-175?
const unsigned char F7_MASK[14] = {48, 49, 50, 51, 52, 53, 54, 55, 40, 41, 42, 43, 44, 45};					//50-63?
const unsigned char F8_MASK[14] = {178, 179, 180, 181, 182, 183, 168, 169, 170, 171, 172, 173, 174, 175};	//176-189?
const unsigned char T7_MASK[14] = {66, 67, 68, 69, 70, 71, 56, 57, 58, 59, 60, 61, 62, 63};					//64-77?
const unsigned char P8_MASK[14] = {158, 159, 144, 145, 146, 147, 148, 149, 150, 151, 136, 137, 138, 139};	//148-161?
const unsigned char AF4_MASK[14] = {196, 197, 198, 199, 184, 185, 186, 187, 188, 189, 190, 191, 176, 177};	//AF4?
const unsigned char F4_MASK[14] = {216, 217, 218, 219, 220, 221, 222, 223, 208, 209, 210, 211, 212, 213};	//218-231?
const unsigned char AF3_MASK[14] = {46, 47, 32, 33, 34, 35, 36, 37, 38, 39, 24, 25, 26, 27};				//AF3?
const unsigned char O2_MASK[14] = {140, 141, 142, 143, 128, 129, 130, 131, 132, 133, 134, 135, 120, 121};	//134-147?
const unsigned char O1_MASK[14] = {102, 103, 88, 89, 90, 91, 92, 93, 94, 95, 80, 81, 82, 83};				//92-105?
const unsigned char FC5_MASK[14] = {28, 29, 30, 31, 16, 17, 18, 19, 20, 21, 22, 23, 8, 9};					//22-35?

const unsigned char LEV_MASK[14] = {107,108,109,110,111,112,113,114,115,116,117,118,119,120};

EMOKIT_DECLSPEC int emokit_get_crypto_key(emokit_device* s, int dev_type) {

    int l = 16;
	s->key[0] = s->serial[l-1];
	s->key[1] = '\0';
	s->key[2] = s->serial[l-2];	
	s->key[3] = '\x54';
	s->key[4] = s->serial[l-3];
	s->key[5] = '\x10';
	s->key[6] = s->serial[l-4];
	s->key[7] = '\x42';
	s->key[8] = s->serial[l-1];
	s->key[9] = '\0';
	s->key[10] = s->serial[l-2];
	s->key[11] = '\x48';
	s->key[12] = s->serial[l-3];
	s->key[13] = '\0';
	s->key[14] = s->serial[l-4];
	s->key[15] = '\x50';
}

EMOKIT_DECLSPEC int emokit_init_crypto(emokit_device* s, int dev_type) {

	emokit_get_crypto_key(s, dev_type);

	//libmcrypt initialization
	s->td = mcrypt_module_open(MCRYPT_RIJNDAEL_128, NULL, MCRYPT_ECB, NULL);
	s->blocksize = mcrypt_enc_get_block_size(s->td); //should return a 16bits blocksize
    
	s->block_buffer = malloc(s->blocksize);

	mcrypt_generic_init( s->td, s->key, EMOKIT_KEYSIZE, NULL);
	return 0;
}

void emokit_deinit(emokit_device* s) {
	mcrypt_generic_deinit (s->td);
	mcrypt_module_close(s->td);
}

int get_level(unsigned char frame[32], const unsigned char bits[14]) {
	signed char i;
	unsigned char b,o;
	int level = 0;
    
	for (i = 13; i >= 0; --i) {
		level <<= 1;
		b = (bits[i] / 8) + 1;
		o = bits[i] % 8;
        
		level |= (frame[b] >> o) & 1;
	}
	return level;
}

EMOKIT_DECLSPEC int emokit_get_next_raw(emokit_device* s) {
	//Two blocks of 16 bytes must be read.
	if (memcpy (s->block_buffer, s->raw_frame, s->blocksize)) {
		mdecrypt_generic (s->td, s->block_buffer, s->blocksize);
		memcpy(s->raw_unenc_frame, s->block_buffer, 16);
	}
	else {
		return -1;
	}
    
	if (memcpy(s->block_buffer, s->raw_frame + s->blocksize, s->blocksize)) {
		mdecrypt_generic (s->td, s->block_buffer, s->blocksize);
		memcpy(s->raw_unenc_frame + 16, s->block_buffer, 16);
	}
	else {
		return -1;
	}
	return 0;
}


EMOKIT_DECLSPEC int emokit_get_next_frame(emokit_device* s) {

	memset(s->raw_unenc_frame, 0, 32);
	
	emokit_get_next_raw(s);

	s->current_frame.F3 = get_level(s->raw_unenc_frame, F3_MASK);
	s->current_frame.FC6 = get_level(s->raw_unenc_frame, FC6_MASK);
	s->current_frame.P7 = get_level(s->raw_unenc_frame, P7_MASK);
	s->current_frame.T8 = get_level(s->raw_unenc_frame, T8_MASK);
	s->current_frame.F7 = get_level(s->raw_unenc_frame, F7_MASK);
	s->current_frame.F8 = get_level(s->raw_unenc_frame, F8_MASK);
	s->current_frame.T7 = get_level(s->raw_unenc_frame, T7_MASK);
	s->current_frame.P8 = get_level(s->raw_unenc_frame, P8_MASK);
	s->current_frame.AF4 = get_level(s->raw_unenc_frame, AF4_MASK);
	s->current_frame.F4 = get_level(s->raw_unenc_frame, F4_MASK);
	s->current_frame.AF3 = get_level(s->raw_unenc_frame, AF3_MASK);
	s->current_frame.O2 = get_level(s->raw_unenc_frame, O2_MASK);
	s->current_frame.O1 = get_level(s->raw_unenc_frame, O1_MASK);
	s->current_frame.FC5 = get_level(s->raw_unenc_frame, FC5_MASK);
    
	s->current_frame.gyroX = s->raw_unenc_frame[29] - 102;
	s->current_frame.gyroY = s->raw_unenc_frame[30] - 104;
    
    //Check if the current frame is a battery or a contact quality frame
    unsigned char f_byte = s->raw_unenc_frame[0];
    if(f_byte<128) //This is a counter frame from 0-127
    {
       //Update the contact quality
       if(f_byte<14)	//note that only the 14 first are used because of the repeating pattern
       {
           s->contact_quality[f_byte] = get_level(s->raw_unenc_frame,LEV_MASK);
       }
       s->current_counter = f_byte;

    }
    else //This is a battery byte
    {
		unsigned char battery;
		if(f_byte & 0x80) {
			if(f_byte >= 248) {
				battery = 100;
			} else {
				switch(f_byte) {
					case 247: battery = 99; break;
					case 246: battery = 97; break;
					case 245: battery = 93; break;
					case 244: battery = 89; break;
					case 243: battery = 85; break;
					case 242: battery = 82; break;
					case 241: battery = 77; break;
					case 240: battery = 72; break;
					case 239: battery = 66; break;
					case 238: battery = 62; break;
					case 237: battery = 55; break;
					case 236: battery = 46; break;
					case 235: battery = 32; break;
					case 234: battery = 20; break;
					case 233: battery = 12; break;
					case 232: battery = 5; break;
					case 231: battery = 4; break;
					case 230: battery = 3; break;
					case 229: battery = 2; break;
					case 228:
					case 227:
					case 226:
						battery = 1;
						break;
					default:
						battery = 0;
				}
			}
		}
		s->battery = battery;
        s->current_counter = 128;
    }
}

EMOKIT_DECLSPEC void emokit_delete(emokit_device* dev)
{
	emokit_deinit(dev);
	free(dev);
}
