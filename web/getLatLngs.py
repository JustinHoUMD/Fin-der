import csv 
import urllib2
import json
import ssl
"""
This script calls Google's Geolocation API to get the LatLng from the address of each court
- Reads in csv file of courts
- Outputs csv file with added lat and lng columns

Author: Justin Ho (ho.justin.c@gmail.com)
modified by Anish Khattar
"""

#Designate file names here
inputFilename = 'FinraAdvisors.json'
outputFilename = 'FinraAdvisorsTagged.json'
apiKey = "AnZ2pNnE5VfjRxjqHLx1MsiFTC4PXa8rcSYy1E1K5AKAi-UhBQ4ot39VzVKezGoX"

with open(inputFilename,'rU') as inAdvisorFile, open(outputFilename,'w') as outAdvisorFile:
	resultsDict = json.load(inAdvisorFile)
	advisorsDict = resultsDict["results"]
	for advisor in advisorsDict:
		employers = advisor["CrntEmps"]["CrntEmp"]
		currEmployer = ''
		if isinstance(employers,list):
			currEmployer = employers[0]
		else:
			currEmployer = employers

		branchOfLocs = currEmployer["BrnchOfLocs"]
		employerBranches = None

		if branchOfLocs:
			employerBranches = branchOfLocs["BrnchOfLoc"]
		else:
			employerBranches = currEmployer

		currBranch = None
		if isinstance(employerBranches,list):
			currBranch = employerBranches[0]
		else:
			currBranch = employerBranches

		if currBranch is None:
			currBranch = currEmployer

		city = currBranch["@city"]
		city = city.replace(' ',"%20");

		country = 'Somewhere'
		street = 'Somewhere'
		state = 'Somewhere'

		locationurl = "http://dev.virtualearth.net/REST/v1/Locations?"
		if "@cntry" in currBranch:
			country = currBranch["@cntry"]
			country = country.replace(' ',"%20");
		if "@state" in currBranch:
			state = currBranch["@state"]
		if "@str1" in currBranch:
			street = currBranch["@str1"]
			street = street.replace(' ',"%20");

		if '#' in street:
			street = 'Somewhere'

		locationurl += "CountryRegion="+country+"&adminDistrict="+state + "&locality="+city+"&addressLine="+street+"&key="+apiKey
		response = urllib2.urlopen(locationurl).read()
		locDict = json.loads(response)

		resources = locDict['resourceSets'][0]['resources']
		lat = -1
		lng = -1

		if len(resources) > 0:
			coordArr = resources[0]['point']['coordinates']
			lat = coordArr[0]
			lng = coordArr[1]

		parseLocDict = {}
		parseLocDict["__type"] = "GeoPoint"
		parseLocDict["latitude"] = lat
		parseLocDict["longitude"] = lng
		advisor["location"] = json.dumps(parseLocDict)
		print advisor

	json.dump(resultsDict,outAdvisorFile)

	inAdvisorFile.close()
	outAdvisorFile.close()
