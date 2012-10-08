/* 
    Simple example of sending an OSC message using oscpack.
*/

#include <cstdio>
#include <cstring>
#include <cstdlib>
#include <csignal>
#include <iostream>
#include "../../include/oscpack/osc/OscOutboundPacketStream.h"
#include "../../include/oscpack/ip/UdpSocket.h"
#include "../../include/emokit/emokit.h"

#define ADDRESS "127.0.0.1"
#define PORT 9997

#define OUTPUT_BUFFER_SIZE 4096

void sigproc(int i)
{
	std::cout << "closing emokit and quitting" << std::endl;
	exit(0);
}

int main(int argc, char* argv[])
{
	signal(SIGINT, sigproc);
#ifndef WIN32
	signal(SIGQUIT, sigproc);
#endif

    //UdpTransmitSocket transmitSocket( IpEndpointName( ADDRESS, PORT ) );
    
    char buffer[OUTPUT_BUFFER_SIZE];


	FILE *input;
	FILE *output;
	//enum headset_type type;
  
	//char raw_frame[32];
	emokit_device* d;
	//uint8_t data[32];
	/*if (argc < 2)
	{
		fputs("Missing argument\nExpected: epocd [consumer|research|special]\n", stderr);
		return 1;
	}
  
	if(strcmp(argv[1], "research") == 0)
		type = RESEARCH_HEADSET;
	else if(strcmp(argv[1], "consumer") == 0)
		type = CONSUMER_HEADSET;
	else if(strcmp(argv[1], "special") == 0)
		type = SPECIAL_HEADSET;
	else {
		fputs("Bad headset type argument\nExpected: epocd [consumer|research|special] source [dest]\n", stderr);
		return 1;
	}
  
	epoc_init(type);*/

	d = emokit_create();
	printf("Current epoc devices connected: %d\n", emokit_get_count(d, EMOKIT_VID, EMOKIT_PID));
	if(emokit_open(d, EMOKIT_VID, EMOKIT_PID, 0) != 0)
	{
		printf("CANNOT CONNECT\n");
		return 1;
	}
	int timer = 0;
	while(1)
	{
		if(emokit_read_data(d) > 0)
		{
			emokit_get_next_frame(d);
			struct emokit_frame frame = d->current_frame;
			
			printf("\r\33[2K");	//go back to beginning of line
			printf("gyroX: %d; gyroY: %d; battery: %d%%", frame.gyroX, frame.gyroY, d->battery*100/128);
			//printf("	contact qualities: ");
			/*for(int i=0 ; i<14 ; i++){
				printf("%d ", d->contact_quality[i]);
			}*/
			
			fflush(output);
			
			/*osc::OutboundPacketStream p( buffer, OUTPUT_BUFFER_SIZE );
			p << osc::BeginBundleImmediate
			  << osc::BeginMessage( "/emokit/channels" )
			  << frame.F3 << frame.FC6 << frame.P7 << frame.T8 << frame.F7 << frame.F8 << frame.T7 << frame.P8 << frame.AF4 << frame.F4 << frame.AF3 << frame.O2 << frame.O1 << frame.FC5 << osc::EndMessage
			  << osc::BeginMessage( "/emokit/gyro" ) 
			  << frame.gyroX << frame.gyroY << osc::EndMessage
			  << osc::EndBundle;
    
			transmitSocket.Send( p.Data(), p.Size() );*/
		}
	}

	emokit_close(d);
	emokit_delete(d);
	return 0;

}

