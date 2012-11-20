/* 
    Simple *working* example of sending an OSC message using oscpack.
*/

#include <cstdio>
#include <cstring>
#include <cstdlib>
#include <csignal>
#include <iostream>
#include "oscpack/osc/OscOutboundPacketStream.h"
#include "oscpack/ip/UdpSocket.h"
#include "emokit/emokit.h"

#define ADDRESS "127.0.0.1"
#define PORT 7000

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

    UdpTransmitSocket transmitSocket( IpEndpointName( ADDRESS, PORT ) );
    
    char buffer[OUTPUT_BUFFER_SIZE];
	
	emokit_device* d;
	
	d = emokit_create();
	printf("Current epoc devices connected: %d\n", emokit_get_count(d, EMOKIT_VID, EMOKIT_PID));
	if(emokit_open(d, EMOKIT_VID, EMOKIT_PID, 0) != 0)
	{
		printf("CANNOT CONNECT\n");
		return 1;
	}
	
	while(1)
	{
		if(emokit_read_data(d) > 0)
		{
			emokit_get_next_frame(d);
			struct emokit_frame frame = d->current_frame;
			
			std::cout << "\r\33[2K" << "gyroX: " << (int)frame.gyroX
				<< "; gyroY: " << (int)frame.gyroY
				<< "; F3: " << frame.F3
				<< "; FC6: " << frame.FC6
				<< "; battery: " << (int)d->battery << "%";
			
			flush(std::cout);
			
			osc::OutboundPacketStream channels( buffer, OUTPUT_BUFFER_SIZE );
			osc::OutboundPacketStream gyro( buffer, OUTPUT_BUFFER_SIZE );
			osc::OutboundPacketStream info( buffer, OUTPUT_BUFFER_SIZE );
			
			channels << osc::BeginMessage( "/emokit/channels" )
				<< frame.F3 << frame.FC6 << frame.P7 << frame.T8 << frame.F7 << frame.F8 << frame.T7 << frame.P8 << frame.AF4 << frame.F4 << frame.AF3 << frame.O2 << frame.O1 << frame.FC5 << osc::EndMessage;
			transmitSocket.Send( channels.Data(), channels.Size() );
			
			gyro << osc::BeginMessage( "/emokit/gyro" ) 
				<< (int)frame.gyroX << (int)frame.gyroY << osc::EndMessage;
			transmitSocket.Send( gyro.Data(), gyro.Size() );
			
			info << osc::BeginMessage( "/emokit/info" )
				<< (int)d->battery;
				for (int i = 0; i<14 ; i++) info << (int)d->contact_quality[i];
				info << osc::EndMessage;
			transmitSocket.Send( info.Data(), info.Size() );
		}
	}

	emokit_close(d);
	emokit_delete(d);
	return 0;

}