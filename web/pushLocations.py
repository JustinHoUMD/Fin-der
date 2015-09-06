import json,httplib

inputFilename = 'FinraAdvisors_now.json'
START_NUM = 0

with open(inputFilename,'rU') as inAdvisorFile:
	resultsDict = json.load(inAdvisorFile)
	advisorsDict = resultsDict["results"]
	currentCnt = 0

	for advisor in advisorsDict:
		locationStr = advisor["location"]
		locationDict = json.loads(locationStr)
		advisorid = advisor["objectId"]
		requestStr = '/1/classes/FinraAdvisors/' + advisorid
		lat = locationDict["latitude"]
		lng = locationDict["longitude"]
		currentCnt = currentCnt + 1
		if currentCnt >= START_NUM:
			connection = httplib.HTTPSConnection('api.parse.com', 443)
			connection.connect()
			connection.request('PUT', requestStr, json.dumps({
			       "advisorLocation": {
			         "__type": "GeoPoint",
			         "latitude": lat,
			         "longitude": lng
			       }
		     	}), {
			       "X-Parse-Application-Id": "P5T1msrll0rI1C7wxkRRv7vFdtQCA9fHgXzw3Pac",
			       "X-Parse-REST-API-Key": "64Z2aSoydwYbBgvOVPedP7Iu3wYkkP4mUM8dr6cT",
			       "Content-Type": "application/json"
		     	})
			print currentCnt

