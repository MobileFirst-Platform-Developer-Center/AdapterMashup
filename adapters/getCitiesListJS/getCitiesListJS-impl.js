/**
* Copyright 2015 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

function getCitiesWeather(){
	var cityList = getCitiesList(); 
	for (var i = 0; i < cityList.resultSet.length; i++) {
		var yahooWeatherData = getCityWeather(cityList.resultSet[i].identifier);
		
		if (yahooWeatherData.isSuccessful)
			cityList.resultSet[i].weather = yahooWeatherData.rss.channel.item.description;
	}
	return cityList;
}

var getCitiesListStatement = WL.Server.createSQLStatement("select city, identifier, summary from weather;");
function getCitiesList() {
	return WL.Server.invokeSQLStatement({
		preparedStatement : getCitiesListStatement,
		parameters : []
	});
}

function getCityWeather(woeid){
	return WL.Server.invokeProcedure({
		adapter : 'getCityWeatherJS',
		procedure : 'getYahooWeather',
		parameters : [woeid]
	});
}