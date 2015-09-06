Parse.initialize("P5T1msrll0rI1C7wxkRRv7vFdtQCA9fHgXzw3Pac", "Za6ha3TFbm1KaOasY8oplEPI0wuaZA7OVEDjMH1A");
var bingApiKey = "AnZ2pNnE5VfjRxjqHLx1MsiFTC4PXa8rcSYy1E1K5AKAi-UhBQ4ot39VzVKezGoX";

$(document).ready(function() {
    $("#locationButton").click(function(){
        var cityName = $('#cityInput').val();
        var stateName = $('#stateSelect').val();
        var zipInput = $('#zipInput').val();
        var locationurl = "http://dev.virtualearth.net/REST/v1/Locations?CountryRegion=US"
        
        if(zipInput.length == 5){
        	locationurl += "&postalCode=" + zipInput;
        }else if(cityName.length > 0 && stateName != 'State'){
        	locationurl += "&adminDistrict=" + stateName + "&locality=" + cityName;
        }
        locationurl += "&key=" + bingApiKey + '&jsonp=?';
        $.getJSON(locationurl,getCallBack);


    }); 
});



function getCallBack(jsonData){
	if (jsonData.resourceSets[0].estimatedTotal > 0) {
            var loc = jsonData.resourceSets[0].resources[0].point.coordinates;
            var lat = loc[0];
            var lng = loc[1];
            getAdvisors(lat,lng);
    }
}


function getAdvisors(lat,lng){
	var radiusSelect = $('#radiusSelect option:selected').text();
	if(radiusSelect == 'Search Radius (miles)'){
		alert("Please specify radius value");
	}else{
		var Advisors = Parse.Object.extend("FinraAdvisors");
		var point = new Parse.GeoPoint({latitude: lat, longitude: lng});
		var searchRadius = parseInt(radiusSelect);

		var locQuery = new Parse.Query(Advisors);
		locQuery.withinMiles('advisorLocation',point,searchRadius);
		locQuery.find({
		  	success: loadParseDataToTable,
		  	error: function(error){
		  		alert(error.message);
		  	}
		});
	}	
}

function loadParseDataToTable(parseData){
	var NAME_INDEX = 0;
	var COMPANY_INDEX = 1;
	var LOCATION_INDEX = 2;
	var DRPS_INDEX = 3;
	var RATING_INDEX = 4;


	var currAdvisor,name, companyName,location,rating;
	var advisorData = new Array(5);
	var drpsStr = "";
	var dataTable = $('#advTable').DataTable();
	dataTable.clear();


	for(var i = 0; i < parseData.length; i++){
		var currAdvisor = parseData[i];
		var infoObj = currAdvisor.get('Info');
		// set name
		advisorData[NAME_INDEX] = infoObj['@firstNm'] + ' ' + infoObj['@lastNm'];

		// set company name
		var currEmpsObj = currAdvisor.get('CrntEmps');
		var currEmployers = currEmpsObj['CrntEmp'];

		var companyObj;
		if(isArray(currEmployers)){
			companyObj = currEmployers[0];
		}else{
			companyObj = currEmployers;
		}
		advisorData[COMPANY_INDEX] = companyObj["@orgNm"];

		// set location
		var branches = companyObj['BrnchOfLocs'];
		if(branches){
			branches = branches['BrnchOfLoc'];
			var actualBranch;
			if(isArray(branches)){
				actualBranch = branches[0];
			}else{
				actualBranch = branches;
			}
			advisorData[LOCATION_INDEX] = actualBranch['@city'] + ' ' +  actualBranch['@state'];
		}else{
			advisorData[LOCATION_INDEX] = companyObj['@city'] + ' ' +  companyObj['@state'];
		}

		// list drps
		drpsStr = "";
		var DRPS = currAdvisor.get('DRPs');
		if(DRPS){
			DRPS = DRPS['DRP'];
			for(var d in DRPS){
				if(DRPS[d] == 'Y'){
					drpsStr += d.substring(1,d.length) + ' ';
				}
			}
		}
		if(drpsStr.length == 0){
			drpsStr = "NONE";
		}
		advisorData[DRPS_INDEX] = drpsStr;

		// set rating
		rating = currAdvisor.get('UserRating');
		if(!rating){
			rating = 0;
		}else{
			advisorData[RATING_INDEX] = rating;
		}
		advisorData[RATING_INDEX] = rating;

		// add to the table using jquerry
		dataTable.row.add(advisorData).draw();
	}
	
}


function isArray(obj){
	return Object.prototype.toString.call(obj) === '[object Array]';
}