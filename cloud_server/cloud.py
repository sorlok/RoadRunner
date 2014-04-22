#!/usr/bin/python2

import sys
import os

#
# This is a cloud server for RoadRunner. It accepts GET/PUT requests, which shuffle tokens between the clients and the cloud.
# Modified to be more consistent in what constitutes a "message", and to use Strings for client IDs. (Also, signing is disabled by default).
#
if len(sys.argv) != 2:
  print('Usage: cloud.py [port]')
  sys.exit(1)

from twisted.internet.protocol import Factory
from twisted.protocols.basic import LineReceiver
from twisted.internet import reactor

from M2Crypto import EVP, RSA, X509
import binascii

from datetime import datetime
from calendar import timegm

#Path to the cert file.
cert_file = "mycert-private.pem"


def now():
	return timegm(datetime.utcnow().utctimetuple())

class Client(LineReceiver):

	def __init__(self, users, regions, regionsLimit):
		# Shared state
		self.users = users
		self.regions = regions
		self.regionsLimit = regionsLimit
		
		# Local state
		self.id = None
		
		#Sign only if we have a certificate (off by default)
		if os.path.isfile(cert_file):
			self.key = EVP.load_key(cert_file)
			self.key.reset_context(md='sha256')
		else:
			self.key = None


	def connectionMade(self):
		print(str(now()) + "\t" + "Client connected, waiting for request")


	# only for properly closed connections??
	def connectionLost(self, reason):
		print(str(now()) + "\t" + "Client disconnected.")
		if self.users.has_key(self.id):
			print(str(now()) + "\t" + "Deleting " + self.id + " from users")
			del self.users[self.id]
			

	def lineReceived(self, line):
		parts = line.split(" ")
		self.id = parts[1]
		regionId = parts[2]
		# regionIds = parts[2].split(",")
		
		# GET request / client wants a token
		if parts[0] == "GET":
			print(str(now()) + "\t" + "GET from %s for regionId(s) %s" % (self.id, regionId))
			
			# create region count of vehicles in region if necessary
			if not regionId in self.regions.keys():
				self.regions[regionId] = 0
				
			# Check if reservations available
			if self.regions[regionId] < self.regionsLimit[regionId]:
				# Create a token
				issued = timegm(datetime.utcnow().utctimetuple())
				expires = issued + 1000*60*60
				tokenString = "%s %s %s" % (regionId, issued, expires)
				
				# Sign the token (optional)
				sigString = None
				if self.key:
					self.key.sign_init()
					self.key.sign_update(tokenString)
					sig = self.key.sign_final()
					sigString = binascii.b2a_hex(sig)
				
				# Send token and signature back to client
				print(str(now()) + "\t" + "to %s:\nGET 200 OK\n%s\n%s\n" % (self.id, tokenString, sigString))
				self.sendLine("GET 200 OK")
				self.sendLine(tokenString)
				if sigString:
					self.sendLine(sigString)
				
				# count number of tokens issued
				self.regions[regionId] = self.regions[regionId] + 1
			else:
				# Tell client we have no more spots on the road
				print(str(now()) + "\t" + "to %s:\nGET 500 FULL\n" % (self.id))
				self.sendLine("GET 500 FULL")
		
		# PUT
		elif parts[0] == "PUT":
			print(str(now()) + "\t" + "PUT from %s for regionId(s) %s" % (self.id, regionId))
			
			# check that the token is valid
			# TODO
			
			# create region count of vehicles in region if necessary
			if not regionId in self.regions.keys():
				self.regions[regionId] = 0
			
			# decrement count of tokens issued for that region
			self.regions[regionId] = self.regions[regionId] - 1
			
			# Tell client we have received the token
			print(str(now()) + "\t" + "to %s:\nPUT 200 OK\n" % (self.id))
			self.sendLine("PUT 200 OK")
		
		# Debug-mode reset command
		elif parts[0] == "DEBUG-RESET":
			print(str(now()) + "\t" + "DEBUG-RESET from %s for regionId(s) %s" % (self.id, "ALL"))
			for rid in self.regions.keys():
				self.regions[rid] = 0
			self.sendLine("DEBUG-RESET 200 OK")
		
		else:
			print(str(now()) + "\t" + "UNKNOWN %s" % (line))

		#End every communication with an extra blank line.
		self.sendLine("")
		
		print(str(now()) + "\t" + "STATE: %s" % (self.regions))
		
		return

class ClientFactory(Factory):

	def __init__(self):
		print(str(now()) + "\t" + "Server init at UTC timestamp %d" % (now()))
		
		self.users = {} # maps ids to Client instances
		self.regions = {}  # keeps a count of tokens handed out for each region
		self.regionsLimit = {"Vassar-1":5,
		                     "Mass-1":2,
		                     "Mass-2":2,
		                     "Windsor-1":2,
		                     "Main-1":4,
		                     "Main-2":2,
		                     "Main-3":2,
		                     "Albany-1":2,
		                     "Albany-2":2,
		                     "Portland-1":1,
		                     "Stata-1":1,
		} # maximum counts / limits for each region
		
		# load key
		if os.path.isfile(cert_file):
			self.key = EVP.load_key("mycert-private.pem")
			self.key.reset_context(md='sha256')
			print("Signing enabled.")
		else:
			self.key = None
			print("Signing disabled.")
		
		# sign data
		if self.key:
			self.key.sign_init()
			self.key.sign_update("from-python")
			signature = self.key.sign_final()
			print(str(now()) + "\t" + "'from-python' test signature: " + binascii.b2a_hex(signature))


	def buildProtocol(self, addr):
		return Client(self.users, self.regions, self.regionsLimit)


port = int(sys.argv[1])
print('running on port %d' % (port))
reactor.listenTCP(port, ClientFactory())
reactor.run()
