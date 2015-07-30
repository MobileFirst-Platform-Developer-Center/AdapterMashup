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

var busyIndicator = null;
var citiesList = null;

function wlCommonInit(){
	busyIndicator = new WL.BusyIndicator("AppBody");
	$('#citiesList').change(citySelectionChange);
	getCitiesList();
}

function getCitiesList() {
	busyIndicator.show();
	
	var resourceRequest = new WLResourceRequest("/adapters/SQLAdapter/getCitiesWeather", WLResourceRequest.GET, 30000);
	resourceRequest.send().then(
		getCitiesListSuccess,
		getCitiesListFailure
	);
}

function getCitiesListSuccess(response) {
	if (response.responseJSON.resultSet.length == 0)
		getCitiesListFailure();
	else {
		citiesList = response.responseJSON.resultSet;
		fillCitiesList();
	}
}

function getCitiesListFailure(response) {
	WL.Logger.debug("CityWeather::getCitiesListFailure");
	busyIndicator.hide();
	WL.SimpleDialog.show("CityWeather",
			"Can't get cities list. Check database connection", [ {
				text : 'Reload app',
				handler : WL.Client.reloadApp
			} ]);
}

function fillCitiesList(){
	$('#citiesList').empty();
	for (var i = 0; i < citiesList.length; i++) {
		var elem = $("<option/>").html(citiesList[i].city);		
		$('#citiesList').append(elem);
	}
	busyIndicator.hide();
	citySelectionChange();
}

function citySelectionChange() {
	var index = $('#citiesList').prop("selectedIndex");
	var citySumm = citiesList[index].summary;
	var cityWeather = citiesList[index].weather;
	$('#info').html(cityWeather + "<br>" + citySumm.slice(0, 200) + "...");
}
