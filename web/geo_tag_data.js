
function load(){
	Parse.initialize("P5T1msrll0rI1C7wxkRRv7vFdtQCA9fHgXzw3Pac", "Za6ha3TFbm1KaOasY8oplEPI0wuaZA7OVEDjMH1A");
	document.write("Parse Initialized !! <br/>");
	var advisors = Parse.Object.extend("FinraAdvisors");
	var query = new Parse.Query(advisors);

	$.getJSON('FinraAdvisors_latest.json', function(jsonData) {
  		var advisorsArr = jsonData['results'];
  		for(var i = 0; i < advisorsArr.length; i++){
  			document.print(advisorsArr[i]["location"]+ '<br/>');
  			/*query.equalTo("objectId", advisorsArr[i]["objectId"]);
  			query.first({
			  success: function(parseAdvisor) {
			    document.print(parseAdvisor.get('info') + '<br/>')
			  },
			  error: function(error) {
			    alert("Error: " + error.code + " " + error.message);
			  }
			});*/
		}
	});
}





