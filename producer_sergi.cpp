/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
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
#include <ndn-cxx/face.hpp>
#include <ndn-cxx/security/key-chain.hpp>
#include <fstream>
//#include "../src/face.hpp"
//#include "../src/security/key-chain.hpp"

#include <iostream>

#include "ocr.h"
#include "tess.h"
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
    m_face.setInterestFilter("/exec/OCR",
                             bind(&Producer::onInterest, this, _1, _2),
                             RegisterPrefixSuccessCallback(),
                             bind(&Producer::onRegisterFailed, this, _1, _2));
    m_face.processEvents();
  }

private:

  void
  onData(const Interest& interest, const Data& data)
  {
   std::cout << "Data received " << data.getName().get(2) <<std::endl;
//   data.getContent();
   std::ofstream myFile;
   std::string filename = "./data/"+data.getName().get(2).toUri();
   myFile.open(filename, std::ofstream::binary | std::ofstream::out);
   myFile.write(reinterpret_cast<const char *>(data.getContent().value()),data.getContent().value_size());
   //myFile.write(data.getContent().value());
   myFile.close();
   std::string res = run_tess(filename.c_str());
   //Interest result(Name("/"+data.getName().get(0).toUri()+"/result/"+res));
   std::map<std::string, Name>::iterator it = pendingInterest.find(data.getName().get(2).toUri()) ;
   if(it!=pendingInterest.end()){
//   	m_face.expressInterest(result,
//		   bind(&Producer::onData, this, _1, _2),
//		   bind(&Producer::onNack, this, _1, _2),
//		   bind(&Producer::onTimeout, this, _1));
           //std::cout << "Image processed :"<<res<<std::endl;
    Name dataName(it->second);
    // Create Data packet
    shared_ptr<Data> data = make_shared<Data>();
    data->setName(dataName);
    data->setFreshnessPeriod(time::seconds(10));
    data->setContent(reinterpret_cast<const uint8_t*>(res.c_str()), res.size());

    // Sign Data packet with default identity
    m_keyChain.sign(*data);
    // m_keyChain.sign(data, <identityName>);
    // m_keyChain.sign(data, <certificate>);

    // Return Data packet to the requester
    std::cout << ">> D: " << *data << std::endl;
    m_face.put(*data);

   } else {
	   std::cout << "Error pending interest not found" << std::endl;
   }
  }

  void
  onNack(const Interest& interest, const lp::Nack& nack)
  {
      std::cout << "received Nack with reason " << nack.getReason()
                  << " for interest " << interest << std::endl;
  }

  void
  onTimeout(const Interest& interest)
  {
      std::cout << "Timeout " << interest << std::endl;
  }

  void
  onInterest(const InterestFilter& filter, const Interest& interest)
  {
    std::cout << "<< I: " << interest << std::endl;

    Interest imgInterest(Name("/pic/"+interest.getName().get(2).toUri()+"/"+interest.getName().get(3).toUri()));
    imgInterest.setInterestLifetime(20_s); // 10 seconds
    imgInterest.setMustBeFresh(true);
    m_face.expressInterest(imgInterest,
		    bind(&Producer::onData, this,  _1, _2),
		    bind(&Producer::onNack, this, _1, _2),
		    bind(&Producer::onTimeout, this, _1));

    pendingInterest.insert(std::make_pair(interest.getName().get(3).toUri(),interest.getName()));
  
    //std::cout << interest.getName().get(2) <<" "<<interest.getName().get(3) << std::endl;

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
  std::map<std::string, Name> pendingInterest;
};

} // namespace examples
} // namespace ndn

int
main(int argc, char** argv)
{
  ndn::examples::Producer producer;
  try {
    producer.run();
    printf("executing...");
  }
  catch (const std::exception& e) {
    std::cerr << "ERROR: " << e.what() << std::endl;
  }
  return 0;
}
