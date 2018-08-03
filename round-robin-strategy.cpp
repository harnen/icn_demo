/* -*- Mode:C++; c-file-style:"gnu"; indent-tabs-mode:nil; -*- */
/**
 * Copyright (c) 2014-2016,  Regents of the University of California,
 *                           Arizona Board of Regents,
 *                           Colorado State University,
 *                           University Pierre & Marie Curie, Sorbonne University,
 *                           Washington University in St. Louis,
 *                           Beijing Institute of Technology,
 *                           The University of Memphis.
 *
 * This file is part of NFD (Named Data Networking Forwarding Daemon).
 * See AUTHORS.md for complete list of NFD authors and contributors.
 *
 * NFD is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * NFD is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * NFD, e.g., in COPYING.md file.  If not, see <http://www.gnu.org/licenses/>.
 */

#include "round-robin-strategy.hpp"
#include "algorithm.hpp"

#include <iostream>

namespace nfd {
namespace fw {

RoundRobinStrategyBase::RoundRobinStrategyBase(Forwarder& forwarder)
  : Strategy(forwarder)
{
//const fib::Entry& fibEntry = this->lookupFib(*pitEntry);
//const fib::NextHopList& nexthops = fibEntry.getNextHops();
//fib::NextHopList::const_iterator it = nexthops.begin();
}

void
RoundRobinStrategyBase::afterReceiveInterest(const Face& inFace, const Interest& interest,
                                            const shared_ptr<pit::Entry>& pitEntry)
{
//main difference is within this method

  if (hasPendingOutRecords(*pitEntry)) {
    // not a new Interest, waiting for data on this Interest, don't forward
    return;
  }

//But...we want the interests to be forwarded to a new node each time, right? 


//  const fib::Entry& fibEntry = this->lookupFib(*pitEntry);
//  const fib::NextHopList& nexthops = fibEntry.getNextHops();

//Does this strategy need a for loop? No. The "it" iterator needs to be tracked and iterated,
//but only if the new interest has not been forwarded earlier (see below), and not to the same
//face.
//  for (fib::NextHopList::const_iterator it = nexthops.begin(); it != nexthops.end(); ++it) {
//  "it" is iterated from the last time
    const fib::Entry& fibEntry = this->lookupFib(*pitEntry);
    const fib::NextHopList& nexthops = fibEntry.getNextHops();
    int hop_no = static_cast<int>(nexthops.size());
    fib::NextHopList::const_iterator it = nexthops.begin();

//    Face& outFace = it->getFace();
//    if (!wouldViolateScope(inFace, interest, outFace) &&
//        canForwardToLegacy(*pitEntry, outFace)) {
//	int out_face= outFace.getId();
//        std::cout << out_face << std::endl;

//    make sure it is iterated here and that it does not pass nexthops.end()
//    it only needs to be reset to nexthops.begin() if it is equal to nexthops.end
      if (it <= nexthops.end()){
	// while loop is used to iterate the fib::NextHopList::const_iterator it up to the 
	//current NextHop, indicated by int iter. fib::NextHopList::const_iterator it couldn't
	//be iterated by itself for a specific number of times due to the type mismatch between
	//fib::NextHopList::const_iterator and int.
	for(int i=0; i<iter%hop_no; ++i){
	  ++it;
	}
	std::cout << "Number of hop selected from FIB:" << iter%hop_no << std::endl;
	++iter;
	std::cout << "Strategy used" << iter << "times." << std::endl;

      }
      else{
        const fib::NextHopList& nexthops = fibEntry.getNextHops();
        it = nexthops.begin();
      }

      Face& outFace = it->getFace();
      if (!wouldViolateScope(inFace, interest, outFace) &&
        canForwardToLegacy(*pitEntry, outFace)) {
        int out_face= outFace.getId();
        std::cout << "Face ID selected:" << out_face << std::endl;

	this->sendInterest(pitEntry, outFace, interest);
	return;
      } //if
//  } //for


//decide that a pending Interest cannot be forwarded

//This shall not be called if the pending Interest has been forwarded earlier, 
//and does not need to be resent now:
  this->rejectPendingInterest(pitEntry);
  return;
}

NFD_REGISTER_STRATEGY(RoundRobinStrategy);

RoundRobinStrategy::RoundRobinStrategy(Forwarder& forwarder, const Name& name)
  : RoundRobinStrategyBase(forwarder)
{
//  const fib::Entry& fibEntry = this->lookupFib(*pitEntry);
//  const fib::NextHopList& nexthops = fibEntry.getNextHops();
//  fib::NextHopList::const_iterator it = nexthops.begin();

  ParsedInstanceName parsed = parseInstanceName(name);
  if (!parsed.parameters.empty()) {
    BOOST_THROW_EXCEPTION(std::invalid_argument("RoundRobinStrategy does not accept parameters"));
  }
  if (parsed.version && *parsed.version != getStrategyName()[-1].toVersion()) {
    BOOST_THROW_EXCEPTION(std::invalid_argument(
      "RoundRobinStrategy does not support version " + std::to_string(*parsed.version)));
  }
  this->setInstanceName(makeInstanceName(name, getStrategyName()));
}

const Name&
RoundRobinStrategy::getStrategyName()
{
  static Name strategyName("/localhost/nfd/strategy/round-robin/%FD%00");
  return strategyName;
}

} // namespace fw
} // namespace nfd
