/*
*
    COPYRIGHT LICENSE: This information contains sample code provided in source code form. You may copy, modify, and distribute
    these sample programs in any form without payment to IBM® for the purposes of developing, using, marketing or distributing
    application programs conforming to the application programming interface for the operating platform for which the sample code is written.
    Notwithstanding anything to the contrary, IBM PROVIDES THE SAMPLE SOURCE CODE ON AN "AS IS" BASIS AND IBM DISCLAIMS ALL WARRANTIES,
    EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, ANY IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
    FITNESS FOR A PARTICULAR PURPOSE, TITLE, AND ANY WARRANTY OR CONDITION OF NON-INFRINGEMENT. IBM SHALL NOT BE LIABLE FOR ANY DIRECT,
    INDIRECT, INCIDENTAL, SPECIAL OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OR OPERATION OF THE SAMPLE SOURCE CODE.
    IBM HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS OR MODIFICATIONS TO THE SAMPLE SOURCE CODE.

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
