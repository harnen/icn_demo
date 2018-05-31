#/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/**
 * Copyright (c) 2013-2015 Regents of the University of California.
 *
 * This file is part of ndn-cxx library (NDN C++ library with eXperimental eXtensions).
 *
 * ndn-cxx library is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * ndn-cxx library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received copies of the GNU General Public License and GNU Lesser
 * General Public License along with ndn-cxx, e.g., in COPYING.md file.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * See AUTHORS.md for complete list of ndn-cxx authors and contributors.
 *
 * @author Alexander Afanasyev <http://lasr.cs.ucla.edu/afanasyev/index.html>
 */

// correct way to include ndn-cxx headers
// #include <ndn-cxx/face.hpp>
// #include <ndn-cxx/security/key-chain.hpp>

#include "/home/chrisys/NDN/icn_demo/ocr/app.hpp"
#include "stdio.h"
#include "face.hpp"
#include "security/key-chain.hpp"

#include <fstream>
#include <iostream>
using namespace std;

// Enclosing code in ndn simplifies coding (can also use `using namespace ndn`)
namespace ndn {
// Additional nested namespace could be used to prevent/limit name contentions
namespace examples {

class Producer : noncopyable
{
public:
  void
  run()
  {
    m_face.setInterestFilter("/example/testApp",
                             bind(&Producer::onInterest, this, _1, _2),
                             RegisterPrefixSuccessCallback(),
                             bind(&Producer::onRegisterFailed, this, _1, _2));
    m_face.processEvents();
  }

private:
  void
  onInterest(const InterestFilter& filter, const Interest& interest)
  {
    std::cout << "<< I: " << interest << std::endl;

    // Create new name, based on Interest's name
    Name dataName(interest.getName());
    dataName
      .append("testApp") // add "testApp" component to Interest name
      .appendVersion();  // add "version" component (current UNIX timestamp in milliseconds)

    static const std::string content = "HELLO KITTY";

	//Execute app
	
	FILE *stream;
	char string[ 256 ] = { 0 };
   	int term_status;
 
   	// be sure to include the full path in your command
   	// here the pipe will be opened for reading and stream will be read-only
   	stream = popen( "/root/NDN/icn_demo/ocr/app /root/NDN/icn_demo/ocr/data/input.png", "r" 
);
   	if( NULL == stream )
   	{
      		fprintf( stderr, "[popen] popen failed\n" );
   	}
 
   	// here you can read from the command you executed until it ends
	ofstream myoutput;
	myoutput.open("output");
	
   	while( NULL != fgets( string, sizeof( string ), stream ) )
   	{
      		// note the '\n' character will be stored in string[]
      		// the parentheses are used as a delimiter
      		myoutput << "[popen] fread returned" << string << "\n";
   	}
	myoutput.close();
 
   	// since fgets returned NULL there are 2 options: EOF was found or an error
   	// occured (use ferror for the latter)
   	if( feof( stream ) )
   	{
     		printf( "[popen] print-args finished normally\n" );
   	}
 
  	// even though stream is a FILE, don't use flcose
   	term_status = pclose( stream );
	if( -1 != term_status )
   	{
      		// WEXITSTATUS can be used to get the command's exit status
      		printf( "[popen-test] print-argv exit status: %d\n", WEXITSTATUS( term_status ) );
   	}
   	else
   	{
      		fprintf( stderr, "[popen-test] pclose failed\n" );
   	}
 
   	printf( "[popen] end\n" );

//Check if the "stream" file was output from this command. If yes, append it to the data packet.

    // Create Data packet
    shared_ptr<Data> data = make_shared<Data>();
    data->setName(dataName);
    data->setFreshnessPeriod(time::seconds(10));
    data->setContent(reinterpret_cast<const uint8_t*>(content.c_str()), content.size());

    // Sign Data packet with default identity
    m_keyChain.sign(*data);
    // m_keyChain.sign(data, <identityName>);
    // m_keyChain.sign(data, <certificate>);

    // Return Data packet to the requester
    std::cout << ">> D: " << *data << std::endl;
    m_face.put(*data);
  }


  void
  onRegisterFailed(const Name& prefix, const std::string& reason)
  {
    std::cerr << "ERROR: Failed to register prefix \""
              << prefix << "\" in local hub's daemon (" << reason << ")"
              << std::endl;
    m_face.shutdown();
  }

private:
  Face m_face;
  KeyChain m_keyChain;
};

} // namespace examples
} // namespace ndn

int
main(int argc, char** argv)
{
  ndn::examples::Producer producer;
  try {
    producer.run();
  }
  catch (const std::exception& e) {
    std::cerr << "ERROR: " << e.what() << std::endl;
  }
  return 0;
}
